package com.bigcoders.aqi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PredictionResult {
    private Integer stationId;
    private String stationName;
    private String cityName;
    private boolean sufficientData;
    private int historicalDataPoints;
    private String method;
    private String disclaimer;
    private String trendDirection; // Increasing, Decreasing, Stable
    private BigDecimal slope;

    private List<PredictionDay> predictions = new ArrayList<>();
    private List<HistoricalPoint> historicalPoints = new ArrayList<>();

    public PredictionResult() {}

    public static class PredictionDay {
        private LocalDate date;
        private BigDecimal predictedAqi;
        private String predictedCategory;

        public PredictionDay() {}

        public PredictionDay(LocalDate date, BigDecimal predictedAqi, String predictedCategory) {
            this.date = date;
            this.predictedAqi = predictedAqi;
            this.predictedCategory = predictedCategory;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public BigDecimal getPredictedAqi() { return predictedAqi; }
        public void setPredictedAqi(BigDecimal predictedAqi) { this.predictedAqi = predictedAqi; }

        public String getPredictedCategory() { return predictedCategory; }
        public void setPredictedCategory(String predictedCategory) { this.predictedCategory = predictedCategory; }
    }

    public static class HistoricalPoint {
        private LocalDate date;
        private BigDecimal aqiValue;

        public HistoricalPoint() {}

        public HistoricalPoint(LocalDate date, BigDecimal aqiValue) {
            this.date = date;
            this.aqiValue = aqiValue;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public BigDecimal getAqiValue() { return aqiValue; }
        public void setAqiValue(BigDecimal aqiValue) { this.aqiValue = aqiValue; }
    }

    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public boolean isSufficientData() { return sufficientData; }
    public void setSufficientData(boolean sufficientData) { this.sufficientData = sufficientData; }

    public int getHistoricalDataPoints() { return historicalDataPoints; }
    public void setHistoricalDataPoints(int historicalDataPoints) { this.historicalDataPoints = historicalDataPoints; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }

    public String getTrendDirection() { return trendDirection; }
    public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

    public BigDecimal getSlope() { return slope; }
    public void setSlope(BigDecimal slope) { this.slope = slope; }

    public List<PredictionDay> getPredictions() { return predictions; }
    public void setPredictions(List<PredictionDay> predictions) { this.predictions = predictions; }

    public List<HistoricalPoint> getHistoricalPoints() { return historicalPoints; }
    public void setHistoricalPoints(List<HistoricalPoint> historicalPoints) { this.historicalPoints = historicalPoints; }
}

