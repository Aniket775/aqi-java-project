package com.bigcoders.aqi.service;

import com.bigcoders.aqi.dto.AqiResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * CPCB Indian National AQI Calculation Engine
 * Reference: Central Pollution Control Board (CPCB), Ministry of Environment, Forest & Climate Change
 * Technical Specification:
 * - 8 Pollutants: PM2.5, PM10, NO2, SO2, CO, O3, NH3, Pb
 * - Sub-index formula: I = ((I_hi - I_lo) / (C_hi - C_lo)) * (C - C_lo) + I_lo
 * - Continuous breakpoints: no decimal gaps
 * - CPCB Overall AQI Rule: Minimum 3 pollutants required, of which at least one MUST be PM2.5 or PM10
 * - Dominant pollutant = pollutant with maximum sub-index
 */
@Service
public class AqiCalculationService {

    public static class Breakpoint {
        final BigDecimal cLo;
        final BigDecimal cHi;
        final BigDecimal iLo;
        final BigDecimal iHi;

        public Breakpoint(double cLo, double cHi, double iLo, double iHi) {
            this.cLo = BigDecimal.valueOf(cLo);
            this.cHi = BigDecimal.valueOf(cHi);
            this.iLo = BigDecimal.valueOf(iLo);
            this.iHi = BigDecimal.valueOf(iHi);
        }
    }

    private static final Map<String, List<Breakpoint>> BREAKPOINTS = new HashMap<>();

    static {
        // 1. PM2.5 (24-hour average, µg/m³)
        BREAKPOINTS.put("PM2.5", Arrays.asList(
                new Breakpoint(0.0, 30.0, 0.0, 50.0),
                new Breakpoint(30.0, 60.0, 50.0, 100.0),
                new Breakpoint(60.0, 90.0, 100.0, 200.0),
                new Breakpoint(90.0, 120.0, 200.0, 300.0),
                new Breakpoint(120.0, 250.0, 300.0, 400.0),
                new Breakpoint(250.0, 500.0, 400.0, 500.0)
        ));

        // 2. PM10 (24-hour average, µg/m³)
        BREAKPOINTS.put("PM10", Arrays.asList(
                new Breakpoint(0.0, 50.0, 0.0, 50.0),
                new Breakpoint(50.0, 100.0, 50.0, 100.0),
                new Breakpoint(100.0, 250.0, 100.0, 200.0),
                new Breakpoint(250.0, 350.0, 200.0, 300.0),
                new Breakpoint(350.0, 430.0, 300.0, 400.0),
                new Breakpoint(430.0, 600.0, 400.0, 500.0)
        ));

        // 3. NO2 (24-hour average, µg/m³)
        BREAKPOINTS.put("NO2", Arrays.asList(
                new Breakpoint(0.0, 40.0, 0.0, 50.0),
                new Breakpoint(40.0, 80.0, 50.0, 100.0),
                new Breakpoint(80.0, 180.0, 100.0, 200.0),
                new Breakpoint(180.0, 280.0, 200.0, 300.0),
                new Breakpoint(280.0, 400.0, 300.0, 400.0),
                new Breakpoint(400.0, 600.0, 400.0, 500.0)
        ));

        // 4. SO2 (24-hour average, µg/m³)
        BREAKPOINTS.put("SO2", Arrays.asList(
                new Breakpoint(0.0, 40.0, 0.0, 50.0),
                new Breakpoint(40.0, 80.0, 50.0, 100.0),
                new Breakpoint(80.0, 380.0, 100.0, 200.0),
                new Breakpoint(380.0, 800.0, 200.0, 300.0),
                new Breakpoint(800.0, 1600.0, 300.0, 400.0),
                new Breakpoint(1600.0, 2400.0, 400.0, 500.0)
        ));

        // 5. CO (8-hour average, mg/m³)
        BREAKPOINTS.put("CO", Arrays.asList(
                new Breakpoint(0.0, 1.0, 0.0, 50.0),
                new Breakpoint(1.0, 2.0, 50.0, 100.0),
                new Breakpoint(2.0, 10.0, 100.0, 200.0),
                new Breakpoint(10.0, 17.0, 200.0, 300.0),
                new Breakpoint(17.0, 34.0, 300.0, 400.0),
                new Breakpoint(34.0, 50.0, 400.0, 500.0)
        ));

        // 6. O3 (8-hour average, µg/m³)
        BREAKPOINTS.put("O3", Arrays.asList(
                new Breakpoint(0.0, 50.0, 0.0, 50.0),
                new Breakpoint(50.0, 100.0, 50.0, 100.0),
                new Breakpoint(100.0, 168.0, 100.0, 200.0),
                new Breakpoint(168.0, 208.0, 200.0, 300.0),
                new Breakpoint(208.0, 748.0, 300.0, 400.0),
                new Breakpoint(748.0, 1000.0, 400.0, 500.0)
        ));

        // 7. NH3 (24-hour average, µg/m³)
        BREAKPOINTS.put("NH3", Arrays.asList(
                new Breakpoint(0.0, 200.0, 0.0, 50.0),
                new Breakpoint(200.0, 400.0, 50.0, 100.0),
                new Breakpoint(400.0, 800.0, 100.0, 200.0),
                new Breakpoint(800.0, 1200.0, 200.0, 300.0),
                new Breakpoint(1200.0, 1800.0, 300.0, 400.0),
                new Breakpoint(1800.0, 2400.0, 400.0, 500.0)
        ));

        // 8. Pb (Lead, 24-hour average, µg/m³)
        BREAKPOINTS.put("PB", Arrays.asList(
                new Breakpoint(0.0, 0.5, 0.0, 50.0),
                new Breakpoint(0.5, 1.0, 50.0, 100.0),
                new Breakpoint(1.0, 2.0, 100.0, 200.0),
                new Breakpoint(2.0, 3.0, 200.0, 300.0),
                new Breakpoint(3.0, 3.5, 300.0, 400.0),
                new Breakpoint(3.5, 5.0, 400.0, 500.0)
        ));
    }

    /**
     * Calculates the AQI sub-index for a single pollutant concentration using linear interpolation.
     * I = ((I_hi - I_lo) / (C_hi - C_lo)) * (C - C_lo) + I_lo
     */
    public BigDecimal calculateSubIndex(BigDecimal concentration, String pollutantFormula) {
        if (concentration == null || concentration.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        String key = pollutantFormula.toUpperCase().trim();
        List<Breakpoint> list = BREAKPOINTS.get(key);

        if (list == null) {
            return concentration.min(BigDecimal.valueOf(500)).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }

        Breakpoint matched = null;
        for (Breakpoint bp : list) {
            if (concentration.compareTo(bp.cHi) <= 0) {
                matched = bp;
                break;
            }
        }

        if (matched == null) {
            matched = list.get(list.size() - 1);
        }

        BigDecimal cDelta = matched.cHi.subtract(matched.cLo);
        BigDecimal iDelta = matched.iHi.subtract(matched.iLo);

        if (cDelta.compareTo(BigDecimal.ZERO) == 0) {
            return matched.iLo.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal concOffset = concentration.subtract(matched.cLo);
        BigDecimal subIndex = iDelta.multiply(concOffset)
                .divide(cDelta, 6, RoundingMode.HALF_UP)
                .add(matched.iLo);

        BigDecimal clamped = subIndex.min(BigDecimal.valueOf(500)).max(BigDecimal.ZERO);
        return clamped.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Classifies an AQI value into standard CPCB category.
     */
    public String getCategory(BigDecimal aqi) {
        return getCategoryAlias(aqi);
    }

    /**
     * Static alias function for SQL / H2 alias execution.
     */
    public static String getCategoryAlias(BigDecimal aqi) {
        if (aqi == null) return "Unknown";
        double val = aqi.doubleValue();
        if (val <= 50.0) return "Good";
        if (val <= 100.0) return "Satisfactory";
        if (val <= 200.0) return "Moderately Polluted";
        if (val <= 300.0) return "Poor";
        if (val <= 400.0) return "Very Poor";
        return "Severe";
    }

    public static BigDecimal calculateAqiAlias(BigDecimal val, String formula) {
        AqiCalculationService service = new AqiCalculationService();
        return service.calculateSubIndex(val, formula);
    }

    /**
     * Computes Overall AQI according to CPCB National AQI rules.
     */
    public AqiResult computeOverallAqi(Map<String, BigDecimal> pollutantConcentrations) {
        AqiResult result = new AqiResult();

        if (pollutantConcentrations == null || pollutantConcentrations.isEmpty()) {
            result.setSufficientData(false);
            result.setMessage("No pollutant measurements available.");
            return result;
        }

        List<Map<String, Object>> subIndicesList = new ArrayList<>();
        BigDecimal maxSubIndex = BigDecimal.ZERO;
        String dominant = null;
        boolean hasPm = false;
        int validCount = 0;

        for (Map.Entry<String, BigDecimal> entry : pollutantConcentrations.entrySet()) {
            String formula = entry.getKey().toUpperCase().trim();
            BigDecimal conc = entry.getValue();

            if (conc != null && conc.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal sub = calculateSubIndex(conc, formula);

                Map<String, Object> item = new HashMap<>();
                item.put("formula", formula);
                item.put("concentration", conc);
                item.put("subIndex", sub);
                item.put("category", getCategory(sub));
                subIndicesList.add(item);

                validCount++;
                if (formula.equals("PM2.5") || formula.equals("PM10")) {
                    hasPm = true;
                }

                if (sub.compareTo(maxSubIndex) > 0) {
                    maxSubIndex = sub;
                    dominant = formula;
                }
            }
        }

        result.setPollutantSubIndices(subIndicesList);

        if (validCount >= 3 && hasPm) {
            result.setSufficientData(true);
            result.setOverallAqi(maxSubIndex);
            result.setSubIndex(maxSubIndex);
            result.setAqiCategory(getCategory(maxSubIndex));
            result.setDominantPollutant(dominant);
            result.setMessage("Overall AQI successfully computed as per CPCB National AQI standards.");
        } else {
            result.setSufficientData(false);
            result.setSubIndex(maxSubIndex);
            result.setDominantPollutant(dominant);

            if (!hasPm) {
                result.setMessage("Insufficient Data: Overall AQI requires at least PM2.5 or PM10 measurement (found none). Individual sub-indices displayed.");
            } else {
                result.setMessage("Insufficient Data: CPCB guidelines require at least 3 distinct pollutants including PM2.5/PM10 (only " + validCount + " provided). Individual sub-indices displayed.");
            }
        }

        return result;
    }
}
