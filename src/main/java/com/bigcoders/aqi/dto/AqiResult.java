package com.bigcoders.aqi.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AqiResult {
    private BigDecimal subIndex;
    private BigDecimal overallAqi;
    private String aqiCategory;
    private String dominantPollutant;
    private boolean sufficientData;
    private String message;
    private String alertGenerated;
    private List<Map<String, Object>> pollutantSubIndices = new ArrayList<>();

    public AqiResult() {}

    public BigDecimal getSubIndex() { return subIndex; }
    public void setSubIndex(BigDecimal subIndex) { this.subIndex = subIndex; }

    public BigDecimal getOverallAqi() { return overallAqi; }
    public void setOverallAqi(BigDecimal overallAqi) { this.overallAqi = overallAqi; }

    public String getAqiCategory() { return aqiCategory; }
    public void setAqiCategory(String aqiCategory) { this.aqiCategory = aqiCategory; }

    public String getDominantPollutant() { return dominantPollutant; }
    public void setDominantPollutant(String dominantPollutant) { this.dominantPollutant = dominantPollutant; }

    public boolean isSufficientData() { return sufficientData; }
    public void setSufficientData(boolean sufficientData) { this.sufficientData = sufficientData; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAlertGenerated() { return alertGenerated; }
    public void setAlertGenerated(String alertGenerated) { this.alertGenerated = alertGenerated; }

    public List<Map<String, Object>> getPollutantSubIndices() { return pollutantSubIndices; }
    public void setPollutantSubIndices(List<Map<String, Object>> pollutantSubIndices) { this.pollutantSubIndices = pollutantSubIndices; }
}

