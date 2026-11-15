package com.bigcoders.aqi.dto;

import com.bigcoders.aqi.model.AlertLog;
import com.bigcoders.aqi.model.AqiHistory;
import com.bigcoders.aqi.model.WeatherData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardData {
    private long cityCount;
    private long stationCount;
    private double avgAqi;
    private long activeAlerts;

    private List<AqiHistory> stationAqiList = new ArrayList<>();
    private List<AlertLog> recentAlerts = new ArrayList<>();
    private List<Map<String, Object>> cityAvgAqi = new ArrayList<>();
    private List<WeatherData> weatherList = new ArrayList<>();

    public DashboardData() {}

    public long getCityCount() { return cityCount; }
    public void setCityCount(long cityCount) { this.cityCount = cityCount; }

    public long getStationCount() { return stationCount; }
    public void setStationCount(long stationCount) { this.stationCount = stationCount; }

    public double getAvgAqi() { return avgAqi; }
    public void setAvgAqi(double avgAqi) { this.avgAqi = avgAqi; }

    public long getActiveAlerts() { return activeAlerts; }
    public void setActiveAlerts(long activeAlerts) { this.activeAlerts = activeAlerts; }

    public List<AqiHistory> getStationAqiList() { return stationAqiList; }
    public void setStationAqiList(List<AqiHistory> stationAqiList) { this.stationAqiList = stationAqiList; }

    public List<AlertLog> getRecentAlerts() { return recentAlerts; }
    public void setRecentAlerts(List<AlertLog> recentAlerts) { this.recentAlerts = recentAlerts; }

    public List<Map<String, Object>> getCityAvgAqi() { return cityAvgAqi; }
    public void setCityAvgAqi(List<Map<String, Object>> cityAvgAqi) { this.cityAvgAqi = cityAvgAqi; }

    public List<WeatherData> getWeatherList() { return weatherList; }
    public void setWeatherList(List<WeatherData> weatherList) { this.weatherList = weatherList; }
}

