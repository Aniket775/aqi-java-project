-- ============================================================
-- AQI Monitoring & Prediction System
-- Database Schema (DDL) — MySQL 8.x
-- Built by Big Coders (Group 3Q15)
-- ============================================================

CREATE DATABASE IF NOT EXISTS aqi_system;
USE aqi_system;

-- 1. City Table
CREATE TABLE IF NOT EXISTS city (
    city_id INT AUTO_INCREMENT PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    UNIQUE KEY uk_city_state (city_name, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Monitoring Station Table
CREATE TABLE IF NOT EXISTS station (
    station_id INT AUTO_INCREMENT PRIMARY KEY,
    city_id INT NOT NULL,
    station_name VARCHAR(150) NOT NULL,
    location_desc VARCHAR(255),
    station_type ENUM('Continuous', 'Manual', 'Automatic') NOT NULL DEFAULT 'Continuous',
    installation_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_station_city FOREIGN KEY (city_id)
        REFERENCES city(city_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Pollutant Master Table (All 8 CPCB Pollutants)
CREATE TABLE IF NOT EXISTS pollutant (
    pollutant_id INT AUTO_INCREMENT PRIMARY KEY,
    pollutant_name VARCHAR(50) NOT NULL UNIQUE,
    chemical_formula VARCHAR(20) NOT NULL UNIQUE,
    unit VARCHAR(20) NOT NULL,
    safe_limit DECIMAL(10, 2),
    averaging_period VARCHAR(20) NOT NULL DEFAULT '24-hour',
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Pollutant Reading Table
CREATE TABLE IF NOT EXISTS reading (
    reading_id INT AUTO_INCREMENT PRIMARY KEY,
    station_id INT NOT NULL,
    pollutant_id INT NOT NULL,
    reading_value DECIMAL(10, 2) NOT NULL,
    reading_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_quality ENUM('Good', 'Moderate', 'Poor', 'Invalid') NOT NULL DEFAULT 'Good',
    data_source VARCHAR(50) NOT NULL DEFAULT 'Manual',
    CONSTRAINT fk_reading_station FOREIGN KEY (station_id)
        REFERENCES station(station_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reading_pollutant FOREIGN KEY (pollutant_id)
        REFERENCES pollutant(pollutant_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_reading_value CHECK (reading_value >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. AQI History Table
CREATE TABLE IF NOT EXISTS aqi_history (
    aqi_id INT AUTO_INCREMENT PRIMARY KEY,
    station_id INT NOT NULL,
    aqi_value DECIMAL(6, 2) NOT NULL,
    aqi_category VARCHAR(50) NOT NULL,
    record_date DATE NOT NULL,
    dominant_pollutant VARCHAR(50),
    data_source VARCHAR(50) NOT NULL DEFAULT 'Calculated',
    CONSTRAINT fk_aqi_station FOREIGN KEY (station_id)
        REFERENCES station(station_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_aqi_value CHECK (aqi_value >= 0 AND aqi_value <= 500),
    UNIQUE KEY uk_station_date (station_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Alert Log Table
CREATE TABLE IF NOT EXISTS alert_log (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    station_id INT NOT NULL,
    alert_type ENUM('AQI_Threshold', 'Rapid_Change', 'Equipment_Failure', 'Data_Quality') NOT NULL,
    severity ENUM('Low', 'Medium', 'High', 'Critical') NOT NULL,
    message TEXT NOT NULL,
    alert_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_alert_station FOREIGN KEY (station_id)
        REFERENCES station(station_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. App Users Table
CREATE TABLE IF NOT EXISTS app_user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Analyst', 'Operator', 'Viewer') NOT NULL DEFAULT 'Viewer',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Weather Data Table
CREATE TABLE IF NOT EXISTS weather_data (
    weather_id INT AUTO_INCREMENT PRIMARY KEY,
    city_id INT NOT NULL,
    temperature DECIMAL(5, 2),
    humidity DECIMAL(5, 2),
    wind_speed DECIMAL(5, 2),
    wind_direction VARCHAR(10),
    precipitation DECIMAL(5, 2) DEFAULT 0.00,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_source VARCHAR(50) NOT NULL DEFAULT 'Manual',
    CONSTRAINT fk_weather_city FOREIGN KEY (city_id)
        REFERENCES city(city_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_humidity CHECK (humidity >= 0 AND humidity <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Health Advisory Table
CREATE TABLE IF NOT EXISTS health_advisory (
    advisory_id INT AUTO_INCREMENT PRIMARY KEY,
    aqi_category VARCHAR(50) NOT NULL UNIQUE,
    general_message TEXT NOT NULL,
    sensitive_group_message TEXT NOT NULL,
    outdoor_guidance TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Report Log Table
CREATE TABLE IF NOT EXISTS report_log (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    report_type ENUM('Daily', 'Weekly', 'Monthly', 'Custom') NOT NULL,
    parameters TEXT,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Pending', 'Completed', 'Failed') NOT NULL DEFAULT 'Pending',
    CONSTRAINT fk_report_user FOREIGN KEY (user_id)
        REFERENCES app_user(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Performance Indexes
CREATE INDEX idx_reading_timestamp ON reading(reading_timestamp);
CREATE INDEX idx_reading_station ON reading(station_id);
CREATE INDEX idx_reading_pollutant ON reading(pollutant_id);
CREATE INDEX idx_aqi_date ON aqi_history(record_date);
CREATE INDEX idx_alert_timestamp ON alert_log(alert_timestamp);
CREATE INDEX idx_weather_recorded ON weather_data(recorded_at);
