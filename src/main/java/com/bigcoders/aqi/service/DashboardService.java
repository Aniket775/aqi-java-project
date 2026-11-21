package com.bigcoders.aqi.service;

import com.bigcoders.aqi.dto.AqiResult;
import com.bigcoders.aqi.dto.DashboardData;
import com.bigcoders.aqi.dto.ReadingSubmission;
import com.bigcoders.aqi.model.*;
import com.bigcoders.aqi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final CityRepository cityRepository;
    private final StationRepository stationRepository;
    private final PollutantRepository pollutantRepository;
    private final ReadingRepository readingRepository;
    private final AqiHistoryRepository aqiHistoryRepository;
    private final AlertLogRepository alertLogRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final AqiCalculationService aqiCalculationService;
    private final AlertService alertService;
    private final WeatherApiService weatherApiService;

    public DashboardService(CityRepository cityRepository,
                            StationRepository stationRepository,
                            PollutantRepository pollutantRepository,
                            ReadingRepository readingRepository,
                            AqiHistoryRepository aqiHistoryRepository,
                            AlertLogRepository alertLogRepository,
                            WeatherDataRepository weatherDataRepository,
                            AqiCalculationService aqiCalculationService,
                            AlertService alertService,
                            WeatherApiService weatherApiService) {
        this.cityRepository = cityRepository;
        this.stationRepository = stationRepository;
        this.pollutantRepository = pollutantRepository;
        this.readingRepository = readingRepository;
        this.aqiHistoryRepository = aqiHistoryRepository;
        this.alertLogRepository = alertLogRepository;
        this.weatherDataRepository = weatherDataRepository;
        this.aqiCalculationService = aqiCalculationService;
        this.alertService = alertService;
        this.weatherApiService = weatherApiService;
    }

    /**
     * Builds the complete dashboard payload with real-time stats, station AQI cards,
     * recent alerts, city rankings, and weather conditions.
     */
    public DashboardData getDashboardData() {
        return getDashboardData(null);
    }

    public DashboardData getDashboardData(Integer cityId) {
        DashboardData data = new DashboardData();

        if (cityId == null) {
            data.setCityCount(cityRepository.count());
            data.setStationCount(stationRepository.countActive());
            data.setAvgAqi(aqiHistoryRepository.getOverallNationalAverage());
            data.setActiveAlerts(alertLogRepository.countUnresolved());

            data.setStationAqiList(aqiHistoryRepository.findLatestPerStation());
            data.setRecentAlerts(alertLogRepository.findRecent(15));
            data.setCityAvgAqi(aqiHistoryRepository.getCityAverages());
            data.setWeatherList(weatherDataRepository.findLatestPerCity());
        } else {
            var stationsInCity = aqiHistoryRepository.findLatestPerStation(cityId);
            data.setStationAqiList(stationsInCity);
            data.setCityCount(1L);
            data.setStationCount(stationsInCity.size());

            double avg = stationsInCity.stream()
                    .filter(s -> s.getAqiValue() != null)
                    .mapToDouble(s -> s.getAqiValue().doubleValue())
                    .average()
                    .orElse(0.0);
            data.setAvgAqi(Math.round(avg * 10.0) / 10.0);

            var allAlerts = alertLogRepository.findRecent(50);
            var cityAlerts = allAlerts.stream()
                    .filter(a -> stationsInCity.stream().anyMatch(s -> s.getStationName().equalsIgnoreCase(a.getStationName())))
                    .toList();
            data.setActiveAlerts(cityAlerts.stream().filter(a -> !a.isResolved()).count());
            data.setRecentAlerts(cityAlerts);

            data.setCityAvgAqi(aqiHistoryRepository.getCityAverages());

            var allWeather = weatherDataRepository.findLatestPerCity();
            var city = cityRepository.findById(cityId);
            if (city.isPresent()) {
                var cityWeather = allWeather.stream()
                        .filter(w -> w.getCityName().equalsIgnoreCase(city.get().getCityName()))
                        .toList();
                data.setWeatherList(cityWeather.isEmpty() ? allWeather : cityWeather);
            } else {
                data.setWeatherList(allWeather);
            }
        }

        return data;
    }

    /**
     * Processes a new reading submission within an atomic transaction (@Transactional).
     * 1. Validates and inserts the reading into MySQL.
     * 2. Computes the pollutant sub-index via AqiCalculationService.
     * 3. Checks today's readings for this station across all pollutants.
     * 4. If CPCB criteria are met (>= 3 pollutants including PM2.5 or PM10), calculates Overall AQI.
     * 5. Upserts AQI into aqi_history.
     * 6. Generates AQI threshold alerts if thresholds are exceeded.
     * 7. Returns AqiResult explaining the result and calculation details.
     */
    @Transactional(rollbackFor = Exception.class)
    public AqiResult processReadingSubmission(ReadingSubmission submission) {
        log.info("Processing reading submission: station={}, pollutant={}, value={}",
                submission.getStationId(), submission.getPollutantId(), submission.getReadingValue());

        // Validate station and pollutant existence
        Station station = stationRepository.findById(submission.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Station ID: " + submission.getStationId()));

        Pollutant pollutant = pollutantRepository.findById(submission.getPollutantId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Pollutant ID: " + submission.getPollutantId()));

        LocalDateTime ts = submission.getReadingTimestamp() != null ? submission.getReadingTimestamp() : LocalDateTime.now();
        LocalDate recordDate = ts.toLocalDate();

        // 1. Save Reading
        Reading reading = new Reading(
                null,
                submission.getStationId(),
                submission.getPollutantId(),
                submission.getReadingValue(),
                ts,
                submission.getDataQuality() != null ? submission.getDataQuality() : "Good",
                "Manual Entry"
        );
        readingRepository.save(reading);

        // 2. Calculate Sub-index for this specific reading
        BigDecimal subIndex = aqiCalculationService.calculateSubIndex(
                submission.getReadingValue(),
                pollutant.getChemicalFormula()
        );

        // 3. Aggregate all today's readings for this station to check CPCB Overall AQI rule
        List<Map<String, Object>> dailyPollutants = readingRepository.getAvgReadingsByStationAndDate(
                submission.getStationId(),
                recordDate
        );

        Map<String, BigDecimal> pollutantMap = new HashMap<>();
        for (Map<String, Object> row : dailyPollutants) {
            String formula = (String) row.get("chemical_formula");
            Number avgNum = (Number) row.get("avg_value");
            if (formula != null && avgNum != null) {
                pollutantMap.put(formula, BigDecimal.valueOf(avgNum.doubleValue()));
            }
        }
        // Ensure current reading is included
        pollutantMap.put(pollutant.getChemicalFormula(), submission.getReadingValue());

        // 4. Compute Overall AQI according to CPCB rules
        AqiResult aqiResult = aqiCalculationService.computeOverallAqi(pollutantMap);
        aqiResult.setSubIndex(subIndex);

        // 5. If sufficient data exists for Overall AQI, upsert to aqi_history
        if (aqiResult.isSufficientData()) {
            AqiHistory history = new AqiHistory(
                    null,
                    submission.getStationId(),
                    aqiResult.getOverallAqi(),
                    aqiResult.getAqiCategory(),
                    recordDate,
                    aqiResult.getDominantPollutant(),
                    "Calculated"
            );
            aqiHistoryRepository.saveOrUpdate(history);

            // 6. Evaluate AQI Threshold Alert
            Optional<AlertLog> alertOpt = alertService.evaluateAqiThresholdAlert(
                    submission.getStationId(),
                    aqiResult.getOverallAqi(),
                    aqiResult.getAqiCategory()
            );

            alertOpt.ifPresent(alert -> aqiResult.setAlertGenerated(
                    String.format("[%s Alert] %s", alert.getSeverity(), alert.getMessage())
            ));
        }

        return aqiResult;
    }
}

