package com.bigcoders.aqi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReadingSubmission {

    @NotNull(message = "Station is required")
    private Integer stationId;

    @NotNull(message = "Pollutant is required")
    private Integer pollutantId;

    @NotNull(message = "Reading value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Reading value must be non-negative")
    private BigDecimal readingValue;

    private String dataQuality = "Good";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime readingTimestamp;

    public ReadingSubmission() {
        this.readingTimestamp = LocalDateTime.now();
    }

    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }

    public Integer getPollutantId() { return pollutantId; }
    public void setPollutantId(Integer pollutantId) { this.pollutantId = pollutantId; }

    public BigDecimal getReadingValue() { return readingValue; }
    public void setReadingValue(BigDecimal readingValue) { this.readingValue = readingValue; }

    public String getDataQuality() { return dataQuality; }
    public void setDataQuality(String dataQuality) { this.dataQuality = dataQuality; }

    public LocalDateTime getReadingTimestamp() { return readingTimestamp; }
    public void setReadingTimestamp(LocalDateTime readingTimestamp) { this.readingTimestamp = readingTimestamp; }
}

