package com.bigcoders.aqi.model;

import java.math.BigDecimal;

public class Pollutant {
    private Integer pollutantId;
    private String pollutantName;
    private String chemicalFormula;
    private String unit;
    private BigDecimal safeLimit;
    private String averagingPeriod = "24-hour";
    private String description;

    public Pollutant() {}

    public Pollutant(Integer pollutantId, String pollutantName, String chemicalFormula,
                     String unit, BigDecimal safeLimit, String averagingPeriod, String description) {
        this.pollutantId = pollutantId;
        this.pollutantName = pollutantName;
        this.chemicalFormula = chemicalFormula;
        this.unit = unit;
        this.safeLimit = safeLimit;
        this.averagingPeriod = averagingPeriod != null ? averagingPeriod : "24-hour";
        this.description = description;
    }

    public Integer getPollutantId() { return pollutantId; }
    public void setPollutantId(Integer pollutantId) { this.pollutantId = pollutantId; }

    public String getPollutantName() { return pollutantName; }
    public void setPollutantName(String pollutantName) { this.pollutantName = pollutantName; }

    public String getChemicalFormula() { return chemicalFormula; }
    public void setChemicalFormula(String chemicalFormula) { this.chemicalFormula = chemicalFormula; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getSafeLimit() { return safeLimit; }
    public void setSafeLimit(BigDecimal safeLimit) { this.safeLimit = safeLimit; }

    public String getAveragingPeriod() { return averagingPeriod; }
    public void setAveragingPeriod(String averagingPeriod) { this.averagingPeriod = averagingPeriod; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
