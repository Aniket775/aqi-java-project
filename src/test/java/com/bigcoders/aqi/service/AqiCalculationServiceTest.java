package com.bigcoders.aqi.service;

import com.bigcoders.aqi.dto.AqiResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CPCB National AQI Calculation Service Unit Tests")
class AqiCalculationServiceTest {

    private AqiCalculationService aqiService;

    @BeforeEach
    void setUp() {
        aqiService = new AqiCalculationService();
    }

    @Test
    @DisplayName("PM2.5 breakpoint interpolation across continuous intervals")
    void testPm25Breakpoints() {
        // [0, 30] -> [0, 50]
        assertEquals(0.00, aqiService.calculateSubIndex(BigDecimal.valueOf(0), "PM2.5").doubleValue());
        assertEquals(25.00, aqiService.calculateSubIndex(BigDecimal.valueOf(15), "PM2.5").doubleValue(), 0.01);
        assertEquals(50.00, aqiService.calculateSubIndex(BigDecimal.valueOf(30), "PM2.5").doubleValue());

        // (30, 60] -> (50, 100]: Decimal boundary test 30.5
        BigDecimal subAt30_5 = aqiService.calculateSubIndex(BigDecimal.valueOf(30.5), "PM2.5");
        assertTrue(subAt30_5.doubleValue() > 50.0 && subAt30_5.doubleValue() < 52.0, "30.5 must smoothly enter the (50,100] interval");

        assertEquals(100.00, aqiService.calculateSubIndex(BigDecimal.valueOf(60), "PM2.5").doubleValue());

        // (120, 250] -> (300, 400]
        assertEquals(350.00, aqiService.calculateSubIndex(BigDecimal.valueOf(185), "PM2.5").doubleValue(), 0.01);
    }

    @Test
    @DisplayName("PM10 breakpoint interpolation")
    void testPm10Breakpoints() {
        assertEquals(50.00, aqiService.calculateSubIndex(BigDecimal.valueOf(50), "PM10").doubleValue());
        assertEquals(100.00, aqiService.calculateSubIndex(BigDecimal.valueOf(100), "PM10").doubleValue());
        assertEquals(200.00, aqiService.calculateSubIndex(BigDecimal.valueOf(250), "PM10").doubleValue());
    }

    @Test
    @DisplayName("Gaseous pollutants: NO2, SO2, CO, O3, NH3, Pb")
    void testOtherPollutants() {
        // CO: [0, 1] -> [0, 50]
        assertEquals(25.00, aqiService.calculateSubIndex(BigDecimal.valueOf(0.5), "CO").doubleValue(), 0.01);
        assertEquals(50.00, aqiService.calculateSubIndex(BigDecimal.valueOf(1.0), "CO").doubleValue());

        // SO2: [0, 40] -> [0, 50]
        assertEquals(50.00, aqiService.calculateSubIndex(BigDecimal.valueOf(40.0), "SO2").doubleValue());

        // NO2: [0, 40] -> [0, 50]
        assertEquals(50.00, aqiService.calculateSubIndex(BigDecimal.valueOf(40.0), "NO2").doubleValue());

        // NH3: [0, 200] -> [0, 50]
        assertEquals(25.00, aqiService.calculateSubIndex(BigDecimal.valueOf(100.0), "NH3").doubleValue(), 0.01);

        // Pb: [0, 0.5] -> [0, 50]
        assertEquals(50.00, aqiService.calculateSubIndex(BigDecimal.valueOf(0.5), "PB").doubleValue());
    }

    @Test
    @DisplayName("Invalid and edge values handled safely")
    void testEdgeCases() {
        assertEquals(0.00, aqiService.calculateSubIndex(null, "PM2.5").doubleValue());
        assertEquals(0.00, aqiService.calculateSubIndex(BigDecimal.valueOf(-15.0), "PM2.5").doubleValue());

        // Severe values clamped to maximum 500
        BigDecimal huge = aqiService.calculateSubIndex(BigDecimal.valueOf(999.0), "PM2.5");
        assertEquals(500.00, huge.doubleValue());
    }

    @Test
    @DisplayName("CPCB Category mapping matches standards")
    void testCategories() {
        assertEquals("Good", aqiService.getCategory(BigDecimal.valueOf(45)));
        assertEquals("Satisfactory", aqiService.getCategory(BigDecimal.valueOf(75)));
        assertEquals("Moderately Polluted", aqiService.getCategory(BigDecimal.valueOf(150)));
        assertEquals("Poor", aqiService.getCategory(BigDecimal.valueOf(250)));
        assertEquals("Very Poor", aqiService.getCategory(BigDecimal.valueOf(350)));
        assertEquals("Severe", aqiService.getCategory(BigDecimal.valueOf(450)));
    }

    @Test
    @DisplayName("CPCB Overall AQI requires at least 3 pollutants including PM2.5 or PM10")
    void testCpcbSufficiencyRule() {
        // Case 1: Only 2 pollutants (PM2.5 and NO2) -> Insufficient Data
        Map<String, BigDecimal> twoPollutants = new HashMap<>();
        twoPollutants.put("PM2.5", BigDecimal.valueOf(80.0));
        twoPollutants.put("NO2", BigDecimal.valueOf(45.0));

        AqiResult r1 = aqiService.computeOverallAqi(twoPollutants);
        assertFalse(r1.isSufficientData(), "Must fail when fewer than 3 pollutants are present");
        assertTrue(r1.getMessage().contains("Insufficient Data"));

        // Case 2: 3 pollutants but NO PM2.5 or PM10 -> Insufficient Data
        Map<String, BigDecimal> noPm = new HashMap<>();
        noPm.put("NO2", BigDecimal.valueOf(45.0));
        noPm.put("SO2", BigDecimal.valueOf(30.0));
        noPm.put("CO", BigDecimal.valueOf(1.2));

        AqiResult r2 = aqiService.computeOverallAqi(noPm);
        assertFalse(r2.isSufficientData(), "Must fail when neither PM2.5 nor PM10 is present");
        assertTrue(r2.getMessage().contains("PM2.5 or PM10"));

        // Case 3: 3 pollutants including PM2.5 -> Valid Overall AQI
        Map<String, BigDecimal> valid = new HashMap<>();
        valid.put("PM2.5", BigDecimal.valueOf(145.5)); // sub ~ 319
        valid.put("NO2", BigDecimal.valueOf(45.0));    // sub ~ 56
        valid.put("CO", BigDecimal.valueOf(1.5));     // sub ~ 75

        AqiResult r3 = aqiService.computeOverallAqi(valid);
        assertTrue(r3.isSufficientData(), "Must succeed with >= 3 pollutants including PM2.5");
        assertEquals("PM2.5", r3.getDominantPollutant());
        assertEquals("Very Poor", r3.getAqiCategory());
        assertTrue(r3.getOverallAqi().doubleValue() > 300);
    }
}

