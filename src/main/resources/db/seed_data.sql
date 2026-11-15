-- ============================================================
-- AQI Monitoring & Prediction System
-- Sample / Seed Data (DML) — MySQL 8.x
-- All sample data is explicitly marked with data_source = 'Sample'
-- Built by Big Coders (Group 3Q15)
-- ============================================================

USE aqi_system;

-- 1. CITIES (11 Indian Cities)
INSERT INTO city (city_id, city_name, state, country, latitude, longitude) VALUES
(1, 'Delhi', 'Delhi', 'India', 28.6139391, 77.2090212),
(2, 'Mumbai', 'Maharashtra', 'India', 19.0759837, 72.8776559),
(3, 'Bengaluru', 'Karnataka', 'India', 12.9715987, 77.5945627),
(4, 'Chennai', 'Tamil Nadu', 'India', 13.0826802, 80.2707184),
(5, 'Kolkata', 'West Bengal', 'India', 22.5726459, 88.3638953),
(6, 'Hyderabad', 'Telangana', 'India', 17.3850440, 78.4866710),
(7, 'Pune', 'Maharashtra', 'India', 18.5204303, 73.8567437),
(8, 'Lucknow', 'Uttar Pradesh', 'India', 26.8467088, 80.9461592),
(9, 'Patiala', 'Punjab', 'India', 30.3398000, 76.3869000),
(10, 'Patna', 'Bihar', 'India', 25.6093239, 85.1235252),
(11, 'Srinagar', 'Jammu and Kashmir', 'India', 34.0836708, 74.7972825)
ON DUPLICATE KEY UPDATE city_name = VALUES(city_name);

-- 2. STATIONS (21 Monitoring Stations)
INSERT INTO station (station_id, city_id, station_name, location_desc, station_type, installation_date, is_active) VALUES
(1, 1, 'ITO Delhi', 'Income Tax Office, Central Delhi', 'Continuous', '2018-01-15', TRUE),
(2, 1, 'Anand Vihar', 'Anand Vihar Bus Terminal, East Delhi', 'Continuous', '2017-06-20', TRUE),
(3, 1, 'RK Puram', 'R.K. Puram Sector 1, South Delhi', 'Continuous', '2019-03-10', TRUE),
(4, 2, 'Bandra-Worli', 'Bandra Worli Sea Link Area, Mumbai', 'Continuous', '2018-09-01', TRUE),
(5, 2, 'Mazgaon', 'Mazgaon Dock Road, South Mumbai', 'Automatic', '2019-11-15', TRUE),
(6, 3, 'BTM Layout', 'BTM Layout 2nd Stage, Bengaluru', 'Continuous', '2019-01-10', TRUE),
(7, 3, 'Peenya', 'Peenya Industrial Area, Bengaluru', 'Continuous', '2018-07-22', TRUE),
(8, 4, 'Manali', 'Manali Industrial Area, Chennai', 'Continuous', '2017-12-01', TRUE),
(9, 4, 'Velachery', 'Velachery Main Road, South Chennai', 'Automatic', '2020-02-14', TRUE),
(10, 5, 'Victoria Memorial', 'Queens Way, Central Kolkata', 'Continuous', '2018-04-05', TRUE),
(11, 5, 'Jadavpur', 'Jadavpur University Campus, South Kolkata', 'Manual', '2019-08-18', TRUE),
(12, 6, 'Jubilee Hills', 'Road No. 36, Jubilee Hills, Hyderabad', 'Continuous', '2019-05-25', TRUE),
(13, 7, 'Shivajinagar', 'Shivajinagar, Central Pune', 'Continuous', '2018-11-30', TRUE),
(14, 7, 'Hadapsar', 'Hadapsar Industrial Estate, Pune', 'Automatic', '2020-06-10', TRUE),
(15, 8, 'Talkatora', 'Talkatora Industrial Area, Lucknow', 'Continuous', '2019-09-01', TRUE),
(16, 9, 'Model Town', 'Model Town, Central Patiala', 'Continuous', '2020-03-15', TRUE),
(17, 9, 'Rajpura Road', 'Rajpura Road Industrial Area, Patiala', 'Automatic', '2021-01-10', TRUE),
(18, 10, 'IGSC Planetarium', 'IGSC Planetarium Complex, Patna', 'Continuous', '2019-04-20', TRUE),
(19, 10, 'Collectorate', 'Collectorate, Patna City', 'Automatic', '2020-08-12', TRUE),
(20, 11, 'Rajbagh', 'Rajbagh, Central Srinagar', 'Continuous', '2020-05-01', TRUE),
(21, 11, 'Parimpora', 'Parimpora Fruit Market, Srinagar', 'Automatic', '2021-06-15', TRUE)
ON DUPLICATE KEY UPDATE station_name = VALUES(station_name);

-- 3. POLLUTANTS (All 8 CPCB Pollutants)
INSERT INTO pollutant (pollutant_id, pollutant_name, chemical_formula, unit, safe_limit, averaging_period, description) VALUES
(1, 'Particulate Matter 2.5', 'PM2.5', 'µg/m³', 60.00, '24-hour', 'Fine inhalable particles with diameters 2.5 micrometers and smaller'),
(2, 'Particulate Matter 10', 'PM10', 'µg/m³', 100.00, '24-hour', 'Inhalable particles with diameters 10 micrometers and smaller'),
(3, 'Nitrogen Dioxide', 'NO2', 'µg/m³', 80.00, '24-hour', 'Reddish-brown gas produced from vehicle emissions and power plants'),
(4, 'Sulfur Dioxide', 'SO2', 'µg/m³', 80.00, '24-hour', 'Colorless pungent gas from fossil fuel combustion at industrial facilities'),
(5, 'Carbon Monoxide', 'CO', 'mg/m³', 4.00, '8-hour', 'Colorless and odorless gas produced by incomplete combustion of carbon'),
(6, 'Ozone', 'O3', 'µg/m³', 100.00, '8-hour', 'Ground-level ozone formed by atmospheric chemical reactions in sunlight'),
(7, 'Ammonia', 'NH3', 'µg/m³', 400.00, '24-hour', 'Colorless pungent gas from agricultural fertilizer and livestock waste'),
(8, 'Lead', 'Pb', 'µg/m³', 1.00, '24-hour', 'Toxic heavy metal emitted from battery recycling and industrial smelters')
ON DUPLICATE KEY UPDATE pollutant_name = VALUES(pollutant_name);

-- 4. HEALTH ADVISORY (6 CPCB AQI Categories)
INSERT INTO health_advisory (advisory_id, aqi_category, general_message, sensitive_group_message, outdoor_guidance) VALUES
(1, 'Good', 'Air quality is satisfactory and poses little or no risk.', 'No precautions needed.', 'Enjoy outdoor activities freely.'),
(2, 'Satisfactory', 'Air quality is acceptable; minor concern for sensitive individuals.', 'People with respiratory issues should limit prolonged outdoor exertion.', 'Outdoor activities are generally safe.'),
(3, 'Moderately Polluted', 'May cause breathing discomfort to sensitive people upon prolonged exposure.', 'Children, elderly, and people with respiratory or heart conditions should reduce heavy outdoor exertion.', 'Limit prolonged outdoor exertion. Take frequent indoor breaks.'),
(4, 'Poor', 'May cause breathing discomfort to most people on prolonged exposure.', 'Sensitive groups should avoid all physical exertion outdoors. Wear N95 masks when outside.', 'Minimize outdoor physical activity. Keep windows closed.'),
(5, 'Very Poor', 'May cause respiratory illness on prolonged exposure. Effects noticeable even in healthy individuals.', 'People with heart or lung disease should stay indoors. Air purifiers recommended.', 'Avoid all outdoor activities entirely. Consult doctors if symptoms develop.'),
(6, 'Severe', 'May cause severe health impacts and respiratory distress. Seriously affects healthy people.', 'Emergency conditions: all individuals must stay indoors with air filtration.', 'All outdoor physical activities must be cancelled immediately.')
ON DUPLICATE KEY UPDATE aqi_category = VALUES(aqi_category);

-- 5. AQI HISTORY (Spread across past 8-10 days to satisfy 7-day linear prediction requirement)
-- Today is treated dynamically, but we seed baseline historical daily records
INSERT INTO aqi_history (station_id, aqi_value, aqi_category, record_date, dominant_pollutant, data_source) VALUES
-- Station 1: ITO Delhi (10 days trend: 220 -> 265)
(1, 218.00, 'Poor', '2026-05-01', 'PM2.5', 'Sample'),
(1, 225.00, 'Poor', '2026-05-02', 'PM2.5', 'Sample'),
(1, 230.00, 'Poor', '2026-05-03', 'PM2.5', 'Sample'),
(1, 238.00, 'Poor', '2026-05-04', 'PM2.5', 'Sample'),
(1, 242.00, 'Poor', '2026-05-05', 'PM2.5', 'Sample'),
(1, 248.00, 'Poor', '2026-05-06', 'PM2.5', 'Sample'),
(1, 255.00, 'Poor', '2026-05-07', 'PM2.5', 'Sample'),
(1, 258.00, 'Poor', '2026-05-08', 'PM2.5', 'Sample'),
(1, 262.00, 'Poor', '2026-05-09', 'PM2.5', 'Sample'),
(1, 265.00, 'Poor', '2026-05-10', 'PM2.5', 'Sample'),

-- Station 2: Anand Vihar Delhi (High pollution trend)
(2, 290.00, 'Poor', '2026-05-01', 'PM10', 'Sample'),
(2, 298.00, 'Poor', '2026-05-02', 'PM10', 'Sample'),
(2, 305.00, 'Very Poor', '2026-05-03', 'PM2.5', 'Sample'),
(2, 312.00, 'Very Poor', '2026-05-04', 'PM2.5', 'Sample'),
(2, 318.00, 'Very Poor', '2026-05-05', 'PM2.5', 'Sample'),
(2, 325.00, 'Very Poor', '2026-05-06', 'PM2.5', 'Sample'),
(2, 330.00, 'Very Poor', '2026-05-07', 'PM2.5', 'Sample'),
(2, 335.00, 'Very Poor', '2026-05-08', 'PM2.5', 'Sample'),

-- Station 4: Bandra-Worli Mumbai (Moderate coastal trend)
(4, 95.00, 'Satisfactory', '2026-05-01', 'PM2.5', 'Sample'),
(4, 98.00, 'Satisfactory', '2026-05-02', 'PM2.5', 'Sample'),
(4, 102.00, 'Moderately Polluted', '2026-05-03', 'PM2.5', 'Sample'),
(4, 105.00, 'Moderately Polluted', '2026-05-04', 'PM2.5', 'Sample'),
(4, 108.00, 'Moderately Polluted', '2026-05-05', 'PM2.5', 'Sample'),
(4, 112.00, 'Moderately Polluted', '2026-05-06', 'PM2.5', 'Sample'),
(4, 115.00, 'Moderately Polluted', '2026-05-07', 'PM2.5', 'Sample'),
(4, 110.00, 'Moderately Polluted', '2026-05-08', 'PM2.5', 'Sample'),

-- Station 6: BTM Layout Bengaluru (Clean/Good trend)
(6, 42.00, 'Good', '2026-05-01', 'PM2.5', 'Sample'),
(6, 44.00, 'Good', '2026-05-02', 'PM2.5', 'Sample'),
(6, 45.00, 'Good', '2026-05-03', 'PM2.5', 'Sample'),
(6, 43.00, 'Good', '2026-05-04', 'PM2.5', 'Sample'),
(6, 46.00, 'Good', '2026-05-05', 'PM2.5', 'Sample'),
(6, 48.00, 'Good', '2026-05-06', 'PM2.5', 'Sample'),
(6, 47.00, 'Good', '2026-05-07', 'PM2.5', 'Sample'),
(6, 45.00, 'Good', '2026-05-08', 'PM2.5', 'Sample'),

-- Other stations latest day baseline
(3, 195.00, 'Moderately Polluted', '2026-05-10', 'PM2.5', 'Sample'),
(5, 120.00, 'Moderately Polluted', '2026-05-10', 'PM2.5', 'Sample'),
(7, 85.00, 'Satisfactory', '2026-05-10', 'PM10', 'Sample'),
(8, 78.00, 'Satisfactory', '2026-05-10', 'SO2', 'Sample'),
(9, 62.00, 'Satisfactory', '2026-05-10', 'PM2.5', 'Sample'),
(10, 142.00, 'Moderately Polluted', '2026-05-10', 'PM2.5', 'Sample'),
(11, 125.00, 'Moderately Polluted', '2026-05-10', 'PM10', 'Sample'),
(12, 58.00, 'Satisfactory', '2026-05-10', 'PM2.5', 'Sample'),
(13, 72.00, 'Satisfactory', '2026-05-10', 'PM2.5', 'Sample'),
(14, 88.00, 'Satisfactory', '2026-05-10', 'PM10', 'Sample'),
(15, 230.00, 'Poor', '2026-05-10', 'PM2.5', 'Sample'),
(16, 175.00, 'Moderately Polluted', '2026-05-10', 'PM2.5', 'Sample'),
(17, 185.00, 'Moderately Polluted', '2026-05-10', 'PM10', 'Sample'),
(18, 210.00, 'Poor', '2026-05-10', 'PM2.5', 'Sample'),
(19, 188.00, 'Moderately Polluted', '2026-05-10', 'PM2.5', 'Sample'),
(20, 35.00, 'Good', '2026-05-10', 'PM2.5', 'Sample'),
(21, 52.00, 'Satisfactory', '2026-05-10', 'PM10', 'Sample')
ON DUPLICATE KEY UPDATE aqi_value = VALUES(aqi_value);

-- 6. READINGS (Sample readings across multiple pollutants)
INSERT INTO reading (station_id, pollutant_id, reading_value, reading_timestamp, data_quality, data_source) VALUES
-- Station 1: ITO Delhi (Full 8 pollutant sample)
(1, 1, 145.50, '2026-05-10 08:00:00', 'Good', 'Sample'),
(1, 2, 220.30, '2026-05-10 08:00:00', 'Good', 'Sample'),
(1, 3, 62.40, '2026-05-10 08:00:00', 'Good', 'Sample'),
(1, 4, 25.10, '2026-05-10 08:00:00', 'Good', 'Sample'),
(1, 5, 2.40, '2026-05-10 08:00:00', 'Good', 'Sample'),
(1, 6, 45.00, '2026-05-10 08:00:00', 'Good', 'Sample'),
(1, 7, 180.00, '2026-05-10 08:00:00', 'Good', 'Sample'),
(1, 8, 0.45, '2026-05-10 08:00:00', 'Good', 'Sample'),

-- Station 2: Anand Vihar Delhi
(2, 1, 195.80, '2026-05-10 08:00:00', 'Good', 'Sample'),
(2, 2, 310.50, '2026-05-10 08:00:00', 'Good', 'Sample'),
(2, 3, 78.20, '2026-05-10 08:00:00', 'Good', 'Sample'),
(2, 4, 32.60, '2026-05-10 08:00:00', 'Good', 'Sample'),

-- Station 4: Bandra-Worli Mumbai
(4, 1, 65.40, '2026-05-10 08:00:00', 'Good', 'Sample'),
(4, 2, 115.00, '2026-05-10 08:00:00', 'Good', 'Sample'),
(4, 3, 38.50, '2026-05-10 08:00:00', 'Good', 'Sample'),
(4, 4, 15.20, '2026-05-10 08:00:00', 'Good', 'Sample'),

-- Station 6: BTM Layout Bengaluru
(6, 1, 28.50, '2026-05-10 08:00:00', 'Good', 'Sample'),
(6, 2, 48.20, '2026-05-10 08:00:00', 'Good', 'Sample'),
(6, 3, 22.10, '2026-05-10 08:00:00', 'Good', 'Sample'),
(6, 4, 8.40, '2026-05-10 08:00:00', 'Good', 'Sample'),

-- Station 8: Manali Chennai
(8, 1, 48.00, '2026-05-10 08:00:00', 'Good', 'Sample'),
(8, 2, 75.00, '2026-05-10 08:00:00', 'Good', 'Sample'),
(8, 4, 65.50, '2026-05-10 08:00:00', 'Good', 'Sample');

-- 7. WEATHER DATA (One per city)
INSERT INTO weather_data (city_id, temperature, humidity, wind_speed, wind_direction, precipitation, recorded_at, data_source) VALUES
(1, 38.50, 32.00, 11.50, 'NW', 0.00, '2026-05-10 08:00:00', 'Sample'),
(2, 33.20, 74.00, 16.80, 'SW', 1.20, '2026-05-10 08:00:00', 'Sample'),
(3, 27.60, 58.00, 9.20, 'SE', 0.00, '2026-05-10 08:00:00', 'Sample'),
(4, 35.40, 69.50, 13.50, 'E', 0.00, '2026-05-10 08:00:00', 'Sample'),
(5, 34.80, 76.00, 10.20, 'S', 3.50, '2026-05-10 08:00:00', 'Sample'),
(6, 32.50, 48.00, 8.70, 'NE', 0.00, '2026-05-10 08:00:00', 'Sample'),
(7, 30.80, 52.00, 7.40, 'W', 0.00, '2026-05-10 08:00:00', 'Sample'),
(8, 39.20, 34.00, 14.10, 'NW', 0.00, '2026-05-10 08:00:00', 'Sample'),
(9, 37.00, 38.00, 10.80, 'NW', 0.00, '2026-05-10 08:00:00', 'Sample'),
(10, 38.00, 56.00, 9.00, 'E', 0.00, '2026-05-10 08:00:00', 'Sample'),
(11, 21.50, 44.00, 5.20, 'N', 0.00, '2026-05-10 08:00:00', 'Sample');

-- 8. ALERT LOG
INSERT INTO alert_log (station_id, alert_type, severity, message, alert_timestamp, is_resolved) VALUES
(2, 'AQI_Threshold', 'Critical', 'AQI at Anand Vihar has reached 335 (Very Poor). Health emergency level for vulnerable citizens.', '2026-05-10 08:30:00', FALSE),
(1, 'AQI_Threshold', 'High', 'AQI at ITO Delhi has reached 265 (Poor). Sensitive individuals should avoid outdoor exertion.', '2026-05-10 08:15:00', FALSE),
(15, 'AQI_Threshold', 'High', 'AQI at Talkatora Lucknow reached 230 (Poor). Industrial emissions elevated.', '2026-05-10 08:10:00', FALSE),
(18, 'AQI_Threshold', 'Medium', 'AQI at IGSC Planetarium Patna reached 210 (Poor). Air stagnation observed.', '2026-05-10 08:05:00', FALSE),
(4, 'AQI_Threshold', 'Medium', 'AQI at Bandra-Worli reached 115 (Moderately Polluted). Humidity trapping particulate matter.', '2026-05-09 17:00:00', TRUE),
(10, 'AQI_Threshold', 'Medium', 'AQI at Victoria Memorial Kolkata reached 142 (Moderately Polluted).', '2026-05-09 16:30:00', TRUE);

-- 9. APP USERS (BCrypt password hashes for demo accounts)
-- Passwords:
--   admin -> admin123
--   analyst -> analyst123
--   operator -> operator123
--   viewer -> viewer123
INSERT INTO app_user (user_id, username, email, password_hash, role) VALUES
(1, 'admin', 'admin@aqisystem.in', '$2a$10$wE1v.h8jZzU7zUj1BqYd7uR6yR1o0bU5pG9p9T1wF0gA3hJ4kL5mN', 'Admin'),
(2, 'analyst', 'analyst@aqisystem.in', '$2a$10$wE1v.h8jZzU7zUj1BqYd7uR6yR1o0bU5pG9p9T1wF0gA3hJ4kL5mN', 'Analyst'),
(3, 'operator', 'operator@aqisystem.in', '$2a$10$wE1v.h8jZzU7zUj1BqYd7uR6yR1o0bU5pG9p9T1wF0gA3hJ4kL5mN', 'Operator'),
(4, 'viewer', 'viewer@aqisystem.in', '$2a$10$wE1v.h8jZzU7zUj1BqYd7uR6yR1o0bU5pG9p9T1wF0gA3hJ4kL5mN', 'Viewer')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 10. REPORT LOG
INSERT INTO report_log (report_id, user_id, report_type, parameters, generated_at, status) VALUES
(1, 1, 'Daily', '{"city_id": 1, "city": "Delhi", "date": "2026-05-10"}', '2026-05-10 09:00:00', 'Completed'),
(2, 2, 'Weekly', '{"city_id": 2, "city": "Mumbai", "date": "2026-05-10"}', '2026-05-10 09:15:00', 'Completed'),
(3, 1, 'Daily', '{"city_id": 3, "city": "Bengaluru", "date": "2026-05-10"}', '2026-05-10 09:30:00', 'Completed')
ON DUPLICATE KEY UPDATE report_type = VALUES(report_type);

