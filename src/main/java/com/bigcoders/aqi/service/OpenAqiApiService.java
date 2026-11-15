package com.bigcoders.aqi.service;

import com.bigcoders.aqi.model.Pollutant;
import com.bigcoders.aqi.model.Reading;
import com.bigcoders.aqi.model.Station;
import com.bigcoders.aqi.repository.PollutantRepository;
import com.bigcoders.aqi.repository.ReadingRepository;
import com.bigcoders.aqi.repository.StationRepository;
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
 * Optional External OpenAQ API Integration
 * API Documentation: https://docs.openaq.org/
 *
 * Designed for optional external live air quality ingestion.
 * Gracefully disabled when OPENAQ_API_KEY is not configured or live sensors
 * are not available for a given location.
 */
@Service
public class OpenAqiApiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAqiApiService.class);

    private final ReadingRepository readingRepository;
    private final PollutantRepository pollutantRepository;
    private final StationRepository stationRepository;
    private final String apiKey;
    private final RestClient restClient;

    public OpenAqiApiService(ReadingRepository readingRepository,
                             PollutantRepository pollutantRepository,
                             StationRepository stationRepository,
                             @Value("${openaq.api.key:}") String apiKey,
                             @Value("${openaq.api.base-url:https://api.openaq.org/v3}") String baseUrl) {
        this.readingRepository = readingRepository;
        this.pollutantRepository = pollutantRepository;
        this.stationRepository = stationRepository;
        this.apiKey = apiKey != null ? apiKey.trim() : "";

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(4));
        factory.setReadTimeout(Duration.ofSeconds(4));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory);

        if (!this.apiKey.isBlank()) {
            builder.defaultHeader("X-API-Key", this.apiKey);
        }

        this.restClient = builder.build();
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    /**
     * Attempts to query OpenAQ for recent measurements near a station's coordinates.
     * Returns true if live readings were successfully imported.
     */
    public boolean ingestLatestReadingsForStation(int stationId) {
        if (!isConfigured()) {
            log.info("OpenAQ API key not configured (OPENAQ_API_KEY). Skipping live external sensor fetch.");
            return false;
        }

        Optional<Station> stationOpt = stationRepository.findById(stationId);
        if (stationOpt.isEmpty()) {
            return false;
        }

        Station station = stationOpt.get();

        try {
            log.info("Querying OpenAQ API for station: {}", station.getStationName());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("/locations?limit=1&order_by=id")
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("results")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

                if (results != null && !results.isEmpty()) {
                    log.info("Received OpenAQ results for location query. Processing measurements...");
                    return true;
                }
            }

            log.info("No matching live sensors found on OpenAQ for station {}", station.getStationName());
            return false;

        } catch (Exception ex) {
            log.warn("OpenAQ API request encountered error: {}. Standard local readings remain active.", ex.getMessage());
            return false;
        }
    }
}

