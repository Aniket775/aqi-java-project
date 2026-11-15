package com.bigcoders.aqi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reading {
    private Integer readingId;
    private Integer stationId;
    private Integer pollutantId;
    private BigDecimal readingValue;
    private LocalDateTime readingTimestamp;
    private String dataQuality = "Good";
    private String dataSource = "Manual";

    // Joined fields
    private String stationName;
    private String pollutantFormula;
    private String pollutantUnit;
    private String cityName;

    public Reading() {}

    public Reading(Integer readingId, Integer stationId, Integer pollutantId,
                   BigDecimal readingValue, LocalDateTime readingTimestamp,
                   String dataQuality, String dataSource) {
        this.readingId = readingId;
        this.stationId = stationId;
        this.pollutantId = pollutantId;
        this.readingValue = readingValue;
        this.readingTimestamp = readingTimestamp;
        this.dataQuality = dataQuality != null ? dataQuality : "Good";
        this.dataSource = dataSource != null ? dataSource : "Manual";
    }

    public Integer getReadingId() { return readingId; }
    public void setReadingId(Integer readingId) { this.readingId = readingId; }

    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }

    public Integer getPollutantId() { return pollutantId; }
    public void setPollutantId(Integer pollutantId) { this.pollutantId = pollutantId; }

    public BigDecimal getReadingValue() { return readingValue; }
    public void setReadingValue(BigDecimal readingValue) { this.readingValue = readingValue; }

    public LocalDateTime getReadingTimestamp() { return readingTimestamp; }
    public void setReadingTimestamp(LocalDateTime readingTimestamp) { this.readingTimestamp = readingTimestamp; }

    public String getDataQuality() { return dataQuality; }
    public void setDataQuality(String dataQuality) { this.dataQuality = dataQuality; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getPollutantFormula() { return pollutantFormula; }
    public void setPollutantFormula(String pollutantFormula) { this.pollutantFormula = pollutantFormula; }

    public String getPollutantUnit() { return pollutantUnit; }
    public void setPollutantUnit(String pollutantUnit) { this.pollutantUnit = pollutantUnit; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
}
