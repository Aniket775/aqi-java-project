package com.bigcoders.aqi.model;

import java.time.LocalDateTime;

public class AlertLog {
    private Integer alertId;
    private Integer stationId;
    private String alertType;
    private String severity;
    private String message;
    private LocalDateTime alertTimestamp;
    private boolean resolved = false;

    // Joined fields
    private String stationName;
    private String cityName;

    public AlertLog() {}

    public AlertLog(Integer alertId, Integer stationId, String alertType, String severity,
                    String message, LocalDateTime alertTimestamp, boolean resolved) {
        this.alertId = alertId;
        this.stationId = stationId;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.alertTimestamp = alertTimestamp;
        this.resolved = resolved;
    }

    public Integer getAlertId() { return alertId; }
    public void setAlertId(Integer alertId) { this.alertId = alertId; }

    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getAlertTimestamp() { return alertTimestamp; }
    public void setAlertTimestamp(LocalDateTime alertTimestamp) { this.alertTimestamp = alertTimestamp; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
}
