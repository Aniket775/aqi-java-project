package com.bigcoders.aqi.service;

import com.bigcoders.aqi.dto.PredictionResult;
import com.bigcoders.aqi.model.AqiHistory;
import com.bigcoders.aqi.model.Station;
import com.bigcoders.aqi.repository.AqiHistoryRepository;
import com.bigcoders.aqi.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AQI Prediction Engine
 * Implements a transparent, explainable linear forecasting model using
 * Ordinary Least Squares (OLS) Linear Regression on historical daily AQI records.
 *
 * Requirements:
 * - Requires at least 7 valid daily historical data points.
 * - Forecasts next 1 to 3 days.
 * - Explicit academic disclosure: trend projection, not a complex meteorological model.
 */
@Service
public class AqiPredictionService {

    private final AqiHistoryRepository aqiHistoryRepository;
    private final StationRepository stationRepository;
    private final AqiCalculationService aqiCalculationService;

    public AqiPredictionService(AqiHistoryRepository aqiHistoryRepository,
                                StationRepository stationRepository,
                                AqiCalculationService aqiCalculationService) {
        this.aqiHistoryRepository = aqiHistoryRepository;
        this.stationRepository = stationRepository;
        this.aqiCalculationService = aqiCalculationService;
    }

    /**
     * Forecasts the AQI for a given station for the next 1 to 3 days.
     */
    public PredictionResult forecast(int stationId, int forecastDays) {
        PredictionResult result = new PredictionResult();
        result.setStationId(stationId);
        result.setMethod("Ordinary Least Squares (OLS) Linear Regression Trend Analysis");
        result.setDisclaimer("Academic demonstration: This forecast is a mathematical linear trend extrapolation " +
                "based on stored historical daily AQI records. It does not replace numerical meteorological dispersion models.");

        Optional<Station> stationOpt = stationRepository.findById(stationId);
        if (stationOpt.isPresent()) {
            Station st = stationOpt.get();
            result.setStationName(st.getStationName());
            result.setCityName(st.getCityName());
        } else {
            result.setStationName("Station #" + stationId);
        }

        // Fetch historical daily points sorted chronologically
        List<AqiHistory> historyList = aqiHistoryRepository.findHistoricalPointsAsc(stationId, 30);
        result.setHistoricalDataPoints(historyList.size());

        List<PredictionResult.HistoricalPoint> historicalPoints = new ArrayList<>();
        for (AqiHistory h : historyList) {
            historicalPoints.add(new PredictionResult.HistoricalPoint(h.getRecordDate(), h.getAqiValue()));
        }
        result.setHistoricalPoints(historicalPoints);

        // Strict Requirement: At least 7 valid daily points are required
        if (historyList.size() < 7) {
            result.setSufficientData(false);
            result.setTrendDirection("Insufficient Data");
            return result;
        }

        result.setSufficientData(true);

        // Perform Linear Regression: y = m * x + c
        // x = 1, 2, ..., N (time steps)
        // y = aqi_value
        int n = historyList.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = historyList.get(i).getAqiValue().doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = (n * sumX2 - sumX * sumX);
        double slope = 0;
        double intercept = sumY / n;

        if (Math.abs(denominator) > 1e-9) {
            slope = (n * sumXY - sumX * sumY) / denominator;
            intercept = (sumY - slope * sumX) / n;
        }

        result.setSlope(BigDecimal.valueOf(slope).setScale(2, RoundingMode.HALF_UP));

        if (slope > 1.5) {
            result.setTrendDirection("Increasing (Deteriorating Air Quality)");
        } else if (slope < -1.5) {
            result.setTrendDirection("Decreasing (Improving Air Quality)");
        } else {
            result.setTrendDirection("Stable (Minor Variation)");
        }

        // Generate forecasts for upcoming days (clamped between 0 and 500)
        LocalDate lastDate = historyList.get(n - 1).getRecordDate();
        int daysToPredict = Math.min(Math.max(forecastDays, 1), 5); // default 1-3, max 5

        List<PredictionResult.PredictionDay> predictions = new ArrayList<>();
        for (int step = 1; step <= daysToPredict; step++) {
            double predictedX = n + step;
            double rawPredictedY = slope * predictedX + intercept;
            double clampedY = Math.max(0.0, Math.min(500.0, rawPredictedY));

            BigDecimal predictedAqi = BigDecimal.valueOf(clampedY).setScale(1, RoundingMode.HALF_UP);
            String category = aqiCalculationService.getCategory(predictedAqi);
            LocalDate predDate = lastDate.plusDays(step);

            predictions.add(new PredictionResult.PredictionDay(predDate, predictedAqi, category));
        }

        result.setPredictions(predictions);
        return result;
    }
}

