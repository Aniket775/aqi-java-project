package com.bigcoders.aqi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WeatherData {
    private Integer weatherId;
    private Integer cityId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal windSpeed;
    private String windDirection;
    private BigDecimal precipitation;
    private LocalDateTime recordedAt;
    private String dataSource = "Manual";

    // Joined fields
    private String cityName;

    public WeatherData() {}

    public WeatherData(Integer weatherId, Integer cityId, BigDecimal temperature,
                       BigDecimal humidity, BigDecimal windSpeed, String windDirection,
                       BigDecimal precipitation, LocalDateTime recordedAt, String dataSource) {
        this.weatherId = weatherId;
        this.cityId = cityId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.precipitation = precipitation != null ? precipitation : BigDecimal.ZERO;
        this.recordedAt = recordedAt;
        this.dataSource = dataSource != null ? dataSource : "Manual";
    }

    public Integer getWeatherId() { return weatherId; }
    public void setWeatherId(Integer weatherId) { this.weatherId = weatherId; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }

    public BigDecimal getWindSpeed() { return windSpeed; }
    public void setWindSpeed(BigDecimal windSpeed) { this.windSpeed = windSpeed; }

    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }

    public BigDecimal getPrecipitation() { return precipitation; }
    public void setPrecipitation(BigDecimal precipitation) { this.precipitation = precipitation; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
}
