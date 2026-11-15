package com.bigcoders.aqi.model;

public class HealthAdvisory {
    private Integer advisoryId;
    private String aqiCategory;
    private String generalMessage;
    private String sensitiveGroupMessage;
    private String outdoorGuidance;

    public HealthAdvisory() {}

    public HealthAdvisory(Integer advisoryId, String aqiCategory, String generalMessage,
                          String sensitiveGroupMessage, String outdoorGuidance) {
        this.advisoryId = advisoryId;
        this.aqiCategory = aqiCategory;
        this.generalMessage = generalMessage;
        this.sensitiveGroupMessage = sensitiveGroupMessage;
        this.outdoorGuidance = outdoorGuidance;
    }

    public Integer getAdvisoryId() { return advisoryId; }
    public void setAdvisoryId(Integer advisoryId) { this.advisoryId = advisoryId; }

    public String getAqiCategory() { return aqiCategory; }
    public void setAqiCategory(String aqiCategory) { this.aqiCategory = aqiCategory; }

    public String getGeneralMessage() { return generalMessage; }
    public void setGeneralMessage(String generalMessage) { this.generalMessage = generalMessage; }

    public String getSensitiveGroupMessage() { return sensitiveGroupMessage; }
    public void setSensitiveGroupMessage(String sensitiveGroupMessage) { this.sensitiveGroupMessage = sensitiveGroupMessage; }

    public String getOutdoorGuidance() { return outdoorGuidance; }
    public void setOutdoorGuidance(String outdoorGuidance) { this.outdoorGuidance = outdoorGuidance; }
}
