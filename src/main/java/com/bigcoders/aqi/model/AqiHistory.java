package com.bigcoders.aqi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AqiHistory {
    private Integer aqiId;
    private Integer stationId;
    private Integer cityId;
    private BigDecimal aqiValue;
    private String aqiCategory;
    private LocalDate recordDate;
    private String dominantPollutant;
    private String dataSource = "Calculated";

    // Joined fields
    private String stationName;
    private String cityName;
    private String state;

    public AqiHistory() {}

    public AqiHistory(Integer aqiId, Integer stationId, BigDecimal aqiValue,
                      String aqiCategory, LocalDate recordDate,
                      String dominantPollutant, String dataSource) {
        this.aqiId = aqiId;
        this.stationId = stationId;
        this.aqiValue = aqiValue;
        this.aqiCategory = aqiCategory;
        this.recordDate = recordDate;
        this.dominantPollutant = dominantPollutant;
        this.dataSource = dataSource != null ? dataSource : "Calculated";
    }

    public Integer getAqiId() { return aqiId; }
    public void setAqiId(Integer aqiId) { this.aqiId = aqiId; }

    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public BigDecimal getAqiValue() { return aqiValue; }
    public void setAqiValue(BigDecimal aqiValue) { this.aqiValue = aqiValue; }

    public String getAqiCategory() { return aqiCategory; }
    public void setAqiCategory(String aqiCategory) { this.aqiCategory = aqiCategory; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getDominantPollutant() { return dominantPollutant; }
    public void setDominantPollutant(String dominantPollutant) { this.dominantPollutant = dominantPollutant; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
