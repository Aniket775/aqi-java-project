package com.bigcoders.aqi.model;

import java.time.LocalDateTime;

public class ReportLog {
    private Integer reportId;
    private Integer userId;
    private String reportType;
    private String parameters;
    private LocalDateTime generatedAt;
    private String status = "Pending";

    // Joined field
    private String username;

    public ReportLog() {}

    public ReportLog(Integer reportId, Integer userId, String reportType,
                     String parameters, LocalDateTime generatedAt, String status) {
        this.reportId = reportId;
        this.userId = userId;
        this.reportType = reportType;
        this.parameters = parameters;
        this.generatedAt = generatedAt;
        this.status = status != null ? status : "Pending";
    }

    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
