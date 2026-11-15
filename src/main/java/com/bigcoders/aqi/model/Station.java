package com.bigcoders.aqi.model;

import java.time.LocalDate;

public class Station {
    private Integer stationId;
    private Integer cityId;
    private String stationName;
    private String locationDesc;
    private String stationType = "Continuous";
    private LocalDate installationDate;
    private boolean active = true;

    // Joined attribute for UI convenience
    private String cityName;

    public Station() {}

    public Station(Integer stationId, Integer cityId, String stationName, String locationDesc,
                   String stationType, LocalDate installationDate, boolean active) {
        this.stationId = stationId;
        this.cityId = cityId;
        this.stationName = stationName;
        this.locationDesc = locationDesc;
        this.stationType = stationType != null ? stationType : "Continuous";
        this.installationDate = installationDate;
        this.active = active;
    }

    public Integer getStationId() { return stationId; }
    public void setStationId(Integer stationId) { this.stationId = stationId; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }

    public String getStationType() { return stationType; }
    public void setStationType(String stationType) { this.stationType = stationType; }

    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
}
