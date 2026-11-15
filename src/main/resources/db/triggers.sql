-- ============================================================
-- AQI Monitoring & Prediction System
-- Database Triggers — MySQL 8.x
-- Built by Big Coders (Group 3Q15)
-- ============================================================

USE aqi_system;

DROP TRIGGER IF EXISTS trg_reading_data_quality;

DELIMITER //

-- ============================================================
-- TRIGGER: trg_reading_data_quality
-- Fires AFTER INSERT on reading table.
-- Automatically logs a Data_Quality alert when a reading has
-- 'Poor' or 'Invalid' quality flag.
-- Note: AQI threshold alerts are managed by the Java Service Layer
-- to strictly prevent duplicate alerts.
-- ============================================================
CREATE TRIGGER trg_reading_data_quality
AFTER INSERT ON reading
FOR EACH ROW
BEGIN
    DECLARE v_station_name VARCHAR(150);
    DECLARE v_pollutant_name VARCHAR(50);
    DECLARE v_severity VARCHAR(20);

    IF NEW.data_quality IN ('Poor', 'Invalid') THEN
        -- Resolve station name
        SELECT station_name INTO v_station_name
        FROM station
        WHERE station_id = NEW.station_id;

        -- Resolve pollutant name
        SELECT chemical_formula INTO v_pollutant_name
        FROM pollutant
        WHERE pollutant_id = NEW.pollutant_id;

        -- Set severity
        IF NEW.data_quality = 'Invalid' THEN
            SET v_severity = 'High';
        ELSE
            SET v_severity = 'Medium';
        END IF;

        INSERT INTO alert_log (
            station_id,
            alert_type,
            severity,
            message,
            alert_timestamp,
            is_resolved
        )
        VALUES (
            NEW.station_id,
            'Data_Quality',
            v_severity,
            CONCAT('[SENSOR QUALITY WARNING] ', COALESCE(v_pollutant_name, 'Pollutant'),
                   ' sensor reading at ', COALESCE(v_station_name, 'Station'),
                   ' flagged as ', NEW.data_quality,
                   ' (Value: ', NEW.reading_value, '). Recalibration suggested.'),
            NOW(),
            FALSE
        );
    END IF;
END //

DELIMITER ;

