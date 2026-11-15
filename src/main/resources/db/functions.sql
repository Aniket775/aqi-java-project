-- ============================================================
-- AQI Monitoring & Prediction System
-- Stored Functions — MySQL 8.x
-- Implements CPCB Indian National AQI Breakpoint Formula
-- ============================================================

USE aqi_system;

DROP FUNCTION IF EXISTS fn_calculate_aqi_subindex;
DROP FUNCTION IF EXISTS fn_get_aqi_category;

DELIMITER //

CREATE FUNCTION fn_calculate_aqi_subindex(
    p_value DECIMAL(10,2),
    p_pollutant_type VARCHAR(50)
)
RETURNS DECIMAL(6,2)
DETERMINISTIC
BEGIN
    DECLARE v_aqi DECIMAL(6,2);
    DECLARE v_i_lo DECIMAL(10,2);
    DECLARE v_i_hi DECIMAL(10,2);
    DECLARE v_c_lo DECIMAL(10,2);
    DECLARE v_c_hi DECIMAL(10,2);

    IF p_value IS NULL OR p_value < 0 THEN
        RETURN 0.00;
    END IF;

    -- 1. PM2.5 (24-hour average, µg/m³)
    IF UPPER(p_pollutant_type) = 'PM2.5' THEN
        IF p_value <= 30 THEN
            SET v_c_lo = 0; SET v_c_hi = 30; SET v_i_lo = 0; SET v_i_hi = 50;
        ELSEIF p_value <= 60 THEN
            SET v_c_lo = 30; SET v_c_hi = 60; SET v_i_lo = 50; SET v_i_hi = 100;
        ELSEIF p_value <= 90 THEN
            SET v_c_lo = 60; SET v_c_hi = 90; SET v_i_lo = 100; SET v_i_hi = 200;
        ELSEIF p_value <= 120 THEN
            SET v_c_lo = 90; SET v_c_hi = 120; SET v_i_lo = 200; SET v_i_hi = 300;
        ELSEIF p_value <= 250 THEN
            SET v_c_lo = 120; SET v_c_hi = 250; SET v_i_lo = 300; SET v_i_hi = 400;
        ELSE
            SET v_c_lo = 250; SET v_c_hi = 500; SET v_i_lo = 400; SET v_i_hi = 500;
        END IF;

    -- 2. PM10 (24-hour average, µg/m³)
    ELSEIF UPPER(p_pollutant_type) = 'PM10' THEN
        IF p_value <= 50 THEN
            SET v_c_lo = 0; SET v_c_hi = 50; SET v_i_lo = 0; SET v_i_hi = 50;
        ELSEIF p_value <= 100 THEN
            SET v_c_lo = 50; SET v_c_hi = 100; SET v_i_lo = 50; SET v_i_hi = 100;
        ELSEIF p_value <= 250 THEN
            SET v_c_lo = 100; SET v_c_hi = 250; SET v_i_lo = 100; SET v_i_hi = 200;
        ELSEIF p_value <= 350 THEN
            SET v_c_lo = 250; SET v_c_hi = 350; SET v_i_lo = 200; SET v_i_hi = 300;
        ELSEIF p_value <= 430 THEN
            SET v_c_lo = 350; SET v_c_hi = 430; SET v_i_lo = 300; SET v_i_hi = 400;
        ELSE
            SET v_c_lo = 430; SET v_c_hi = 600; SET v_i_lo = 400; SET v_i_hi = 500;
        END IF;

    -- 3. NO2 (24-hour average, µg/m³)
    ELSEIF UPPER(p_pollutant_type) = 'NO2' THEN
        IF p_value <= 40 THEN
            SET v_c_lo = 0; SET v_c_hi = 40; SET v_i_lo = 0; SET v_i_hi = 50;
        ELSEIF p_value <= 80 THEN
            SET v_c_lo = 40; SET v_c_hi = 80; SET v_i_lo = 50; SET v_i_hi = 100;
        ELSEIF p_value <= 180 THEN
            SET v_c_lo = 80; SET v_c_hi = 180; SET v_i_lo = 100; SET v_i_hi = 200;
        ELSEIF p_value <= 280 THEN
            SET v_c_lo = 180; SET v_c_hi = 280; SET v_i_lo = 200; SET v_i_hi = 300;
        ELSEIF p_value <= 400 THEN
            SET v_c_lo = 280; SET v_c_hi = 400; SET v_i_lo = 300; SET v_i_hi = 400;
        ELSE
            SET v_c_lo = 400; SET v_c_hi = 600; SET v_i_lo = 400; SET v_i_hi = 500;
        END IF;

    -- 4. SO2 (24-hour average, µg/m³)
    ELSEIF UPPER(p_pollutant_type) = 'SO2' THEN
        IF p_value <= 40 THEN
            SET v_c_lo = 0; SET v_c_hi = 40; SET v_i_lo = 0; SET v_i_hi = 50;
        ELSEIF p_value <= 80 THEN
            SET v_c_lo = 40; SET v_c_hi = 80; SET v_i_lo = 50; SET v_i_hi = 100;
        ELSEIF p_value <= 380 THEN
            SET v_c_lo = 80; SET v_c_hi = 380; SET v_i_lo = 100; SET v_i_hi = 200;
        ELSEIF p_value <= 800 THEN
            SET v_c_lo = 380; SET v_c_hi = 800; SET v_i_lo = 200; SET v_i_hi = 300;
        ELSEIF p_value <= 1600 THEN
            SET v_c_lo = 800; SET v_c_hi = 1600; SET v_i_lo = 300; SET v_i_hi = 400;
        ELSE
            SET v_c_lo = 1600; SET v_c_hi = 2400; SET v_i_lo = 400; SET v_i_hi = 500;
        END IF;

    -- 5. CO (8-hour average, mg/m³)
    ELSEIF UPPER(p_pollutant_type) = 'CO' THEN
        IF p_value <= 1.0 THEN
            SET v_c_lo = 0.0; SET v_c_hi = 1.0; SET v_i_lo = 0; SET v_i_hi = 50;
        ELSEIF p_value <= 2.0 THEN
            SET v_c_lo = 1.0; SET v_c_hi = 2.0; SET v_i_lo = 50; SET v_i_hi = 100;
        ELSEIF p_value <= 10.0 THEN
            SET v_c_lo = 2.0; SET v_c_hi = 10.0; SET v_i_lo = 100; SET v_i_hi = 200;
        ELSEIF p_value <= 17.0 THEN
            SET v_c_lo = 10.0; SET v_c_hi = 17.0; SET v_i_lo = 200; SET v_i_hi = 300;
        ELSEIF p_value <= 34.0 THEN
            SET v_c_lo = 17.0; SET v_c_hi = 34.0; SET v_i_lo = 300; SET v_i_hi = 400;
        ELSE
            SET v_c_lo = 34.0; SET v_c_hi = 50.0; SET v_i_lo = 400; SET v_i_hi = 500;
        END IF;

    -- 6. O3 (8-hour average, µg/m³)
    ELSEIF UPPER(p_pollutant_type) = 'O3' THEN
        IF p_value <= 50 THEN
            SET v_c_lo = 0; SET v_c_hi = 50; SET v_i_lo = 0; SET v_i_hi = 50;
        ELSEIF p_value <= 100 THEN
            SET v_c_lo = 50; SET v_c_hi = 100; SET v_i_lo = 50; SET v_i_hi = 100;
        ELSEIF p_value <= 168 THEN
            SET v_c_lo = 100; SET v_c_hi = 168; SET v_i_lo = 100; SET v_i_hi = 200;
        ELSEIF p_value <= 208 THEN
            SET v_c_lo = 168; SET v_c_hi = 208; SET v_i_lo = 200; SET v_i_hi = 300;
        ELSEIF p_value <= 748 THEN
            SET v_c_lo = 208; SET v_c_hi = 748; SET v_i_lo = 300; SET v_i_hi = 400;
        ELSE
            SET v_c_lo = 748; SET v_c_hi = 1000; SET v_i_lo = 400; SET v_i_hi = 500;
        END IF;

    -- 7. NH3 (24-hour average, µg/m³)
    ELSEIF UPPER(p_pollutant_type) = 'NH3' THEN
        IF p_value <= 200 THEN
            SET v_c_lo = 0; SET v_c_hi = 200; SET v_i_lo = 0; SET v_i_hi = 50;
        ELSEIF p_value <= 400 THEN
            SET v_c_lo = 200; SET v_c_hi = 400; SET v_i_lo = 50; SET v_i_hi = 100;
        ELSEIF p_value <= 800 THEN
            SET v_c_lo = 400; SET v_c_hi = 800; SET v_i_lo = 100; SET v_i_hi = 200;
        ELSEIF p_value <= 1200 THEN
            SET v_c_lo = 800; SET v_c_hi = 1200; SET v_i_lo = 200; SET v_i_hi = 300;
        ELSEIF p_value <= 1800 THEN
            SET v_c_lo = 1200; SET v_c_hi = 1800; SET v_i_lo = 300; SET v_i_hi = 400;
        ELSE
            SET v_c_lo = 1800; SET v_c_hi = 2400; SET v_i_lo = 400; SET v_i_hi = 500;
        END IF;

    -- 8. Pb (24-hour average, µg/m³)
    ELSEIF UPPER(p_pollutant_type) = 'PB' THEN
        IF p_value <= 0.5 THEN
            SET v_c_lo = 0.0; SET v_c_hi = 0.5; SET v_i_lo = 0; SET v_i_hi = 50;
        ELSEIF p_value <= 1.0 THEN
            SET v_c_lo = 0.5; SET v_c_hi = 1.0; SET v_i_lo = 50; SET v_i_hi = 100;
        ELSEIF p_value <= 2.0 THEN
            SET v_c_lo = 1.0; SET v_c_hi = 2.0; SET v_i_lo = 100; SET v_i_hi = 200;
        ELSEIF p_value <= 3.0 THEN
            SET v_c_lo = 2.0; SET v_c_hi = 3.0; SET v_i_lo = 200; SET v_i_hi = 300;
        ELSEIF p_value <= 3.5 THEN
            SET v_c_lo = 3.0; SET v_c_hi = 3.5; SET v_i_lo = 300; SET v_i_hi = 400;
        ELSE
            SET v_c_lo = 3.5; SET v_c_hi = 5.0; SET v_i_lo = 400; SET v_i_hi = 500;
        END IF;

    ELSE
        -- Fallback: return value clamped
        RETURN LEAST(500.00, GREATEST(0.00, p_value));
    END IF;

    -- Linear Breakpoint Interpolation: I = ((I_hi - I_lo) / (C_hi - C_lo)) * (C - C_lo) + I_lo
    IF v_c_hi = v_c_lo THEN
        SET v_aqi = v_i_lo;
    ELSE
        SET v_aqi = ((v_i_hi - v_i_lo) / (v_c_hi - v_c_lo)) * (p_value - v_c_lo) + v_i_lo;
    END IF;

    -- Clamp to 0-500
    IF v_aqi < 0 THEN SET v_aqi = 0; END IF;
    IF v_aqi > 500 THEN SET v_aqi = 500; END IF;

    RETURN ROUND(v_aqi, 2);
END //

CREATE FUNCTION fn_get_aqi_category(p_aqi DECIMAL(6,2))
RETURNS VARCHAR(50)
DETERMINISTIC
BEGIN
    IF p_aqi IS NULL THEN
        RETURN 'Unknown';
    ELSEIF p_aqi <= 50 THEN
        RETURN 'Good';
    ELSEIF p_aqi <= 100 THEN
        RETURN 'Satisfactory';
    ELSEIF p_aqi <= 200 THEN
        RETURN 'Moderately Polluted';
    ELSEIF p_aqi <= 300 THEN
        RETURN 'Poor';
    ELSEIF p_aqi <= 400 THEN
        RETURN 'Very Poor';
    ELSE
        RETURN 'Severe';
    END IF;
END //

DELIMITER ;

