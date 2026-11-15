package com.bigcoders.aqi.dto;

import java.math.BigDecimal;

public class CityReportRow {
    private String stationName;
    private BigDecimal latestAqi;
    private String aqiCategory;
    private String dominantPollutant;
    private int totalReadings;
    private BigDecimal avgPm25;
    private BigDecimal avgPm10;
    private BigDecimal maxPm25;
    private BigDecimal maxPm10;

    public CityReportRow() {}

    public CityReportRow(String stationName, BigDecimal latestAqi, String aqiCategory,
                         String dominantPollutant, int totalReadings, BigDecimal avgPm25,
                         BigDecimal avgPm10, BigDecimal maxPm25, BigDecimal maxPm10) {
        this.stationName = stationName;
        this.latestAqi = latestAqi;
        this.aqiCategory = aqiCategory;
        this.dominantPollutant = dominantPollutant;
        this.totalReadings = totalReadings;
        this.avgPm25 = avgPm25;
        this.avgPm10 = avgPm10;
        this.maxPm25 = maxPm25;
        this.maxPm10 = maxPm10;
    }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public BigDecimal getLatestAqi() { return latestAqi; }
    public void setLatestAqi(BigDecimal latestAqi) { this.latestAqi = latestAqi; }

    public String getAqiCategory() { return aqiCategory; }
    public void setAqiCategory(String aqiCategory) { this.aqiCategory = aqiCategory; }

    public String getDominantPollutant() { return dominantPollutant; }
    public void setDominantPollutant(String dominantPollutant) { this.dominantPollutant = dominantPollutant; }

    public int getTotalReadings() { return totalReadings; }
    public void setTotalReadings(int totalReadings) { this.totalReadings = totalReadings; }

    public BigDecimal getAvgPm25() { return avgPm25; }
    public void setAvgPm25(BigDecimal avgPm25) { this.avgPm25 = avgPm25; }

    public BigDecimal getAvgPm10() { return avgPm10; }
    public void setAvgPm10(BigDecimal avgPm10) { this.avgPm10 = avgPm10; }

    public BigDecimal getMaxPm25() { return maxPm25; }
    public void setMaxPm25(BigDecimal maxPm25) { this.maxPm25 = maxPm25; }

    public BigDecimal getMaxPm10() { return maxPm10; }
    public void setMaxPm10(BigDecimal maxPm10) { this.maxPm10 = maxPm10; }
}

