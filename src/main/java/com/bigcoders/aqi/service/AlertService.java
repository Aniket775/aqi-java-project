package com.bigcoders.aqi.service;

import com.bigcoders.aqi.model.AlertLog;
import com.bigcoders.aqi.model.Station;
import com.bigcoders.aqi.repository.AlertLogRepository;
import com.bigcoders.aqi.repository.StationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertLogRepository alertLogRepository;
    private final StationRepository stationRepository;

    public AlertService(AlertLogRepository alertLogRepository, StationRepository stationRepository) {
        this.alertLogRepository = alertLogRepository;
        this.stationRepository = stationRepository;
    }

    /**
     * Checks if computed AQI exceeds safety thresholds and generates an alert.
     * Prevents duplicate alerts if an identical alert was generated recently.
     */
    public Optional<AlertLog> evaluateAqiThresholdAlert(int stationId, BigDecimal aqi, String category) {
        if (aqi == null || aqi.compareTo(BigDecimal.valueOf(100)) <= 0) {
            return Optional.empty();
        }

        String severity;
        if (aqi.compareTo(BigDecimal.valueOf(300)) > 0) {
            severity = "Critical";
        } else if (aqi.compareTo(BigDecimal.valueOf(200)) > 0) {
            severity = "High";
        } else {
            severity = "Medium";
        }

        // Prevent duplicate alerts within a 4-hour window
        if (alertLogRepository.existsRecentDuplicate(stationId, "AQI_Threshold", severity)) {
            log.info("Suppressed duplicate AQI alert for station {} with severity {}", stationId, severity);
            return Optional.empty();
        }

        String stationName = "Station #" + stationId;
        Optional<Station> stOpt = stationRepository.findById(stationId);
        if (stOpt.isPresent()) {
            stationName = stOpt.get().getStationName();
        }

        String msg = String.format("AQI at %s reached %s (%s). %s",
                stationName,
                aqi.setScale(0, java.math.RoundingMode.HALF_UP),
                category,
                severity.equals("Critical") ? "Severe health impact on population. Emergency precautions advised."
                        : severity.equals("High") ? "Unhealthy air quality. Sensitive individuals must stay indoors."
                        : "Moderate pollution levels. Monitor trends closely.");

        AlertLog alert = new AlertLog(
                null,
                stationId,
                "AQI_Threshold",
                severity,
                msg,
                LocalDateTime.now(),
                false
        );

        AlertLog saved = alertLogRepository.save(alert);
        log.info("Generated AQI Alert [ID: {}] for station {}: {}", saved.getAlertId(), stationName, msg);
        return Optional.of(saved);
    }

    public List<AlertLog> getRecentAlerts(int limit) {
        return alertLogRepository.findRecent(limit);
    }

    public List<AlertLog> getAllAlerts(String severity, Boolean isResolved, int limit) {
        return alertLogRepository.findAll(severity, isResolved, limit);
    }

    public void resolveAlert(int alertId) {
        alertLogRepository.resolve(alertId);
    }

    public long getUnresolvedCount() {
        return alertLogRepository.countUnresolved();
    }
}

