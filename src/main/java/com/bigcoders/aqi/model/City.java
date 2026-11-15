package com.bigcoders.aqi.model;

import java.math.BigDecimal;

public class City {
    private Integer cityId;
    private String cityName;
    private String state;
    private String country = "India";
    private BigDecimal latitude;
    private BigDecimal longitude;

    public City() {}

    public City(Integer cityId, String cityName, String state, String country, BigDecimal latitude, BigDecimal longitude) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.state = state;
        this.country = country != null ? country : "India";
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}
