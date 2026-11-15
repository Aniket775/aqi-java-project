-- ============================================================
-- AQI Monitoring & Prediction System
-- Master Setup Script — MySQL 8.x
-- Built by Big Coders (Group 3Q15)
-- Usage: mysql -u root -p < src/main/resources/db/setup.sql
-- ============================================================

DROP DATABASE IF EXISTS aqi_system;
CREATE DATABASE aqi_system;
USE aqi_system;

-- 1. Schema DDL
SOURCE schema.sql;

-- 2. Stored Functions (AQI sub-index & category)
SOURCE functions.sql;

-- 3. Stored Procedures (Dashboard, Dropdowns, Cursor-based City Report)
SOURCE procedures.sql;

-- 4. Triggers (Data quality sensor warning)
SOURCE triggers.sql;

-- 5. Seed / Sample Data
SOURCE seed_data.sql;

-- ============================================================
-- Verification Queries
-- ============================================================
SELECT '=== AQI Database Setup Successfully Completed ===' AS status;
SELECT COUNT(*) AS total_cities FROM city;
SELECT COUNT(*) AS total_stations FROM station;
SELECT COUNT(*) AS total_pollutants FROM pollutant;
SELECT COUNT(*) AS total_readings FROM reading;
SELECT COUNT(*) AS total_aqi_records FROM aqi_history;
SELECT COUNT(*) AS total_alerts FROM alert_log;
SELECT COUNT(*) AS total_weather_records FROM weather_data;
SELECT COUNT(*) AS total_health_advisories FROM health_advisory;
SELECT COUNT(*) AS total_report_logs FROM report_log;

-- Test Stored Functions directly
SELECT '=== Testing fn_calculate_aqi_subindex & fn_get_aqi_category ===' AS test_info;
SELECT fn_calculate_aqi_subindex(145.50, 'PM2.5') AS aqi_pm25_145_5;
SELECT fn_calculate_aqi_subindex(220.30, 'PM10') AS aqi_pm10_220_3;
SELECT fn_get_aqi_category(fn_calculate_aqi_subindex(145.50, 'PM2.5')) AS category_pm25;

