-- ============================================================
-- AQI Monitoring & Prediction System
-- Stored Procedures — MySQL 8.x
-- Built by Big Coders (Group 3Q15)
-- ============================================================

USE aqi_system;

DROP PROCEDURE IF EXISTS sp_get_dashboard_data;
DROP PROCEDURE IF EXISTS sp_get_stations_and_pollutants;
DROP PROCEDURE IF EXISTS sp_get_cities;
DROP PROCEDURE IF EXISTS sp_generate_city_report;

DELIMITER //

-- ============================================================
-- 1. PROCEDURE: sp_get_dashboard_data
-- Returns 4 result sets for dashboard population
-- ============================================================
CREATE PROCEDURE sp_get_dashboard_data()
BEGIN
    -- Result Set 1: Latest AQI per station with city details
    SELECT
        s.station_id,
        s.station_name,
        c.city_name,
        c.state,
        ah.aqi_value,
        ah.aqi_category,
        ah.dominant_pollutant,
        ah.record_date,
        ah.data_source
    FROM station s
    JOIN city c ON s.city_id = c.city_id
    LEFT JOIN aqi_history ah ON s.station_id = ah.station_id
        AND ah.record_date = (
            SELECT MAX(record_date)
            FROM aqi_history
            WHERE station_id = s.station_id
        )
    WHERE s.is_active = TRUE
    ORDER BY ah.aqi_value DESC, s.station_name ASC;

    -- Result Set 2: Recent 20 alerts
    SELECT
        al.alert_id,
        al.station_id,
        s.station_name,
        c.city_name,
        al.alert_type,
        al.severity,
        al.message,
        al.alert_timestamp,
        al.is_resolved
    FROM alert_log al
    JOIN station s ON al.station_id = s.station_id
    JOIN city c ON s.city_id = c.city_id
    ORDER BY al.alert_timestamp DESC
    LIMIT 20;

    -- Result Set 3: City-wise average AQI
    SELECT
        c.city_id,
        c.city_name,
        c.state,
        ROUND(AVG(ah.aqi_value), 2) AS avg_aqi,
        fn_get_aqi_category(AVG(ah.aqi_value)) AS avg_category,
        COUNT(DISTINCT s.station_id) AS station_count
    FROM city c
    JOIN station s ON c.city_id = s.city_id
    JOIN aqi_history ah ON s.station_id = ah.station_id
    WHERE ah.record_date = (
        SELECT MAX(record_date) FROM aqi_history
    )
    GROUP BY c.city_id, c.city_name, c.state
    ORDER BY avg_aqi DESC;

    -- Result Set 4: Latest weather per city
    SELECT
        w.weather_id,
        c.city_id,
        c.city_name,
        w.temperature,
        w.humidity,
        w.wind_speed,
        w.wind_direction,
        w.precipitation,
        w.recorded_at,
        w.data_source
    FROM weather_data w
    JOIN city c ON w.city_id = c.city_id
    WHERE w.recorded_at = (
        SELECT MAX(recorded_at)
        FROM weather_data
        WHERE city_id = w.city_id
    )
    ORDER BY c.city_name ASC;
END //

-- ============================================================
-- 2. PROCEDURE: sp_get_stations_and_pollutants
-- Dropdown helper returning active stations and all pollutants
-- ============================================================
CREATE PROCEDURE sp_get_stations_and_pollutants()
BEGIN
    SELECT
        s.station_id,
        s.station_name,
        c.city_id,
        c.city_name
    FROM station s
    JOIN city c ON s.city_id = c.city_id
    WHERE s.is_active = TRUE
    ORDER BY c.city_name ASC, s.station_name ASC;

    SELECT
        pollutant_id,
        pollutant_name,
        chemical_formula,
        unit,
        safe_limit,
        averaging_period
    FROM pollutant
    ORDER BY pollutant_name ASC;
END //

-- ============================================================
-- 3. PROCEDURE: sp_get_cities
-- Returns all cities with coordinates
-- ============================================================
CREATE PROCEDURE sp_get_cities()
BEGIN
    SELECT city_id, city_name, state, country, latitude, longitude
    FROM city
    ORDER BY city_name ASC;
END //

-- ============================================================
-- 4. PROCEDURE: sp_generate_city_report (USES CURSOR)
-- Explicit demonstration of a MySQL CURSOR:
-- Iterates through each active station in a given city, aggregates
-- historical readings over the last 30 days, combines with latest
-- computed AQI, logs audit record in report_log, and returns report.
-- ============================================================
CREATE PROCEDURE sp_generate_city_report(
    IN p_city_id INT,
    IN p_user_id INT
)
BEGIN
    DECLARE v_done INT DEFAULT FALSE;
    DECLARE v_station_id INT;
    DECLARE v_station_name VARCHAR(150);
    DECLARE v_city_name VARCHAR(100);
    DECLARE v_report_id INT;
    DECLARE v_latest_aqi DECIMAL(6,2);
    DECLARE v_aqi_category VARCHAR(50);
    DECLARE v_dominant_pollutant VARCHAR(50);
    DECLARE v_total_readings INT;
    DECLARE v_avg_pm25 DECIMAL(10,2);
    DECLARE v_avg_pm10 DECIMAL(10,2);
    DECLARE v_max_pm25 DECIMAL(10,2);
    DECLARE v_max_pm10 DECIMAL(10,2);

    -- Cursor to iterate through all active stations in the selected city
    DECLARE station_cursor CURSOR FOR
        SELECT station_id, station_name
        FROM station
        WHERE city_id = p_city_id AND is_active = TRUE;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    -- Fetch city name for audit log
    SELECT city_name INTO v_city_name FROM city WHERE city_id = p_city_id LIMIT 1;

    -- Create Report_Log entry
    INSERT INTO report_log (user_id, report_type, parameters, status)
    VALUES (
        p_user_id,
        'Daily',
        CONCAT('{"city_id": ', p_city_id, ', "city": "', COALESCE(v_city_name, 'Unknown'), '", "date": "', CURDATE(), '"}'),
        'Pending'
    );
    SET v_report_id = LAST_INSERT_ID();

    -- Create Temporary Table for the aggregated station metrics
    DROP TEMPORARY TABLE IF EXISTS tmp_city_report;
    CREATE TEMPORARY TABLE tmp_city_report (
        station_name VARCHAR(150),
        latest_aqi DECIMAL(6,2),
        aqi_category VARCHAR(50),
        dominant_pollutant VARCHAR(50),
        total_readings INT,
        avg_pm25 DECIMAL(10,2),
        avg_pm10 DECIMAL(10,2),
        max_pm25 DECIMAL(10,2),
        max_pm10 DECIMAL(10,2)
    );

    -- Open cursor and loop through each station
    OPEN station_cursor;

    cursor_loop: LOOP
        FETCH station_cursor INTO v_station_id, v_station_name;
        IF v_done THEN
            LEAVE cursor_loop;
        END IF;

        -- 1. Fetch latest AQI history
        SET v_latest_aqi = 0.00;
        SET v_aqi_category = 'N/A';
        SET v_dominant_pollutant = 'N/A';

        SELECT
            COALESCE(aqi_value, 0.00),
            COALESCE(aqi_category, 'N/A'),
            COALESCE(dominant_pollutant, 'N/A')
        INTO v_latest_aqi, v_aqi_category, v_dominant_pollutant
        FROM aqi_history
        WHERE station_id = v_station_id
        ORDER BY record_date DESC
        LIMIT 1;

        -- 2. Aggregate readings for this station (recent 30 days)
        SELECT
            COUNT(*),
            COALESCE(AVG(CASE WHEN p.chemical_formula = 'PM2.5' THEN r.reading_value END), 0.00),
            COALESCE(AVG(CASE WHEN p.chemical_formula = 'PM10' THEN r.reading_value END), 0.00),
            COALESCE(MAX(CASE WHEN p.chemical_formula = 'PM2.5' THEN r.reading_value END), 0.00),
            COALESCE(MAX(CASE WHEN p.chemical_formula = 'PM10' THEN r.reading_value END), 0.00)
        INTO
            v_total_readings,
            v_avg_pm25,
            v_avg_pm10,
            v_max_pm25,
            v_max_pm10
        FROM reading r
        JOIN pollutant p ON r.pollutant_id = p.pollutant_id
        WHERE r.station_id = v_station_id
          AND r.reading_timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY);

        -- 3. Insert aggregated row into temporary table
        INSERT INTO tmp_city_report VALUES (
            v_station_name,
            v_latest_aqi,
            v_aqi_category,
            v_dominant_pollutant,
            v_total_readings,
            ROUND(v_avg_pm25, 2),
            ROUND(v_avg_pm10, 2),
            ROUND(v_max_pm25, 2),
            ROUND(v_max_pm10, 2)
        );
    END LOOP;

    CLOSE station_cursor;

    -- Update Report_Log status
    UPDATE report_log SET status = 'Completed' WHERE report_id = v_report_id;

    -- Return final aggregated result set
    SELECT * FROM tmp_city_report;

    DROP TEMPORARY TABLE IF EXISTS tmp_city_report;
END //

DELIMITER ;

