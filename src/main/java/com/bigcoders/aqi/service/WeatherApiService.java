package com.bigcoders.aqi.service;

import com.bigcoders.aqi.model.City;
import com.bigcoders.aqi.model.WeatherData;
import com.bigcoders.aqi.repository.CityRepository;
import com.bigcoders.aqi.repository.WeatherDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Open-Meteo Weather API Integration
 * API Documentation: https://open-meteo.com/en/docs
 * Free non-commercial weather forecast API requiring no key.
 */
@Service
public class WeatherApiService {

    private static final Logger log = LoggerFactory.getLogger(WeatherApiService.class);

    private final WeatherDataRepository weatherDataRepository;
    private final CityRepository cityRepository;
    private final RestClient restClient;

    public WeatherApiService(WeatherDataRepository weatherDataRepository,
                             CityRepository cityRepository,
                             @Value("${openmeteo.api.base-url:https://api.open-meteo.com/v1/forecast}") String baseUrl) {
        this.weatherDataRepository = weatherDataRepository;
        this.cityRepository = cityRepository;

        // Configure RestClient with 4-second timeout to ensure the UI never hangs
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(4));
        factory.setReadTimeout(Duration.ofSeconds(4));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Fetches current weather for a city from Open-Meteo and persists it.
     * Falls back gracefully to the latest cached weather on network or API failure.
     */
    public WeatherData fetchAndStoreWeather(City city) {
        if (city == null || city.getLatitude() == null || city.getLongitude() == null) {
            return getCachedOrEmpty(city);
        }

        try {
            String uri = String.format("?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,precipitation",
                    city.getLatitude().toPlainString(),
                    city.getLongitude().toPlainString());

            log.info("Requesting live weather from Open-Meteo for {} ({}, {})",
                    city.getCityName(), city.getLatitude(), city.getLongitude());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("current")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> current = (Map<String, Object>) response.get("current");

                BigDecimal temp = toBigDecimal(current.get("temperature_2m"));
                BigDecimal humidity = toBigDecimal(current.get("relative_humidity_2m"));
                BigDecimal windSpeed = toBigDecimal(current.get("wind_speed_10m"));
                BigDecimal precipitation = toBigDecimal(current.get("precipitation"));

                // Convert degrees to cardinal direction
                String windDir = degreesToDirection(toBigDecimal(current.get("wind_direction_10m")));

                WeatherData weather = new WeatherData(
                        null,
                        city.getCityId(),
                        temp,
                        humidity,
                        windSpeed,
                        windDir,
                        precipitation,
                        LocalDateTime.now(),
                        "Open-Meteo Live API"
                );
                weather.setCityName(city.getCityName());

                WeatherData saved = weatherDataRepository.save(weather);
                log.info("Live weather retrieved and stored for {}: {}°C, {}% humidity", city.getCityName(), temp, humidity);
                return saved;
            }

        } catch (Exception ex) {
            log.warn("Open-Meteo API call failed for {} ({}). Using cached weather fallback.", city.getCityName(), ex.getMessage());
        }

        return getCachedOrEmpty(city);
    }

    /**
     * Refreshes weather for all registered cities.
     */
    public void refreshAllCities() {
        List<City> cities = cityRepository.findAll();
        for (City c : cities) {
            fetchAndStoreWeather(c);
        }
    }

    public List<WeatherData> getLatestWeatherForAllCities() {
        return weatherDataRepository.findLatestPerCity();
    }

    private WeatherData getCachedOrEmpty(City city) {
        if (city == null) return null;
        Optional<WeatherData> cached = weatherDataRepository.findLatestByCity(city.getCityId());
        if (cached.isPresent()) {
            WeatherData w = cached.get();
            w.setCityName(city.getCityName());
            return w;
        }
        // Minimal default so UI renders without errors
        WeatherData fallback = new WeatherData(
                null,
                city.getCityId(),
                BigDecimal.valueOf(28.0),
                BigDecimal.valueOf(50.0),
                BigDecimal.valueOf(10.0),
                "NW",
                BigDecimal.ZERO,
                LocalDateTime.now(),
                "Unavailable / Cached"
        );
        fallback.setCityName(city.getCityName());
        return fallback;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue()).setScale(2, RoundingMode.HALF_UP);
        try {
            return new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String degreesToDirection(BigDecimal deg) {
        if (deg == null) return "N";
        double d = deg.doubleValue();
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                               "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int idx = (int) Math.round(((d % 360) / 22.5)) % 16;
        return directions[idx];
    }
}

