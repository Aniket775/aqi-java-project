package com.bigcoders.aqi.service;

import com.bigcoders.aqi.dto.CityReportRow;
import com.bigcoders.aqi.model.AqiHistory;
import com.bigcoders.aqi.model.HealthAdvisory;
import com.bigcoders.aqi.model.ReportLog;
import com.bigcoders.aqi.repository.AqiHistoryRepository;
import com.bigcoders.aqi.repository.HealthAdvisoryRepository;
import com.bigcoders.aqi.repository.ReportLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final JdbcTemplate jdbcTemplate;
    private final AqiHistoryRepository aqiHistoryRepository;
    private final HealthAdvisoryRepository healthAdvisoryRepository;
    private final ReportLogRepository reportLogRepository;

    public ReportService(JdbcTemplate jdbcTemplate,
                         AqiHistoryRepository aqiHistoryRepository,
                         HealthAdvisoryRepository healthAdvisoryRepository,
                         ReportLogRepository reportLogRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.aqiHistoryRepository = aqiHistoryRepository;
        this.healthAdvisoryRepository = healthAdvisoryRepository;
        this.reportLogRepository = reportLogRepository;
    }

    /**
     * Executes the MySQL cursor-based stored procedure: CALL sp_generate_city_report(city_id, user_id).
     * If the procedure execution encounters any environmental failure, gracefully executes a direct
     * SQL aggregation to ensure 100% operational resilience.
     */
    public List<CityReportRow> generateCityReport(int cityId, int userId) {
        List<CityReportRow> reportRows = new ArrayList<>();

        try {
            log.info("Executing MySQL Stored Procedure 'sp_generate_city_report' for city_id={}, user_id={}", cityId, userId);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("CALL sp_generate_city_report(?, ?)", cityId, userId);

            for (Map<String, Object> r : rows) {
                reportRows.add(new CityReportRow(
                        (String) r.get("station_name"),
                        toBigDecimal(r.get("latest_aqi")),
                        (String) r.get("aqi_category"),
                        (String) r.get("dominant_pollutant"),
                        r.get("total_readings") != null ? ((Number) r.get("total_readings")).intValue() : 0,
                        toBigDecimal(r.get("avg_pm25")),
                        toBigDecimal(r.get("avg_pm10")),
                        toBigDecimal(r.get("max_pm25")),
                        toBigDecimal(r.get("max_pm10"))
                ));
            }
            log.info("Procedure executed successfully. Retrieved {} station rows.", reportRows.size());
            return reportRows;

        } catch (Exception ex) {
            log.warn("Stored procedure CALL sp_generate_city_report failed ({}), falling back to direct SQL aggregation.", ex.getMessage());

            // Direct Java SQL fallback providing the identical aggregation logic
            String fallbackSql =
                    "SELECT s.station_name, " +
                    "       COALESCE(ah.aqi_value, 0) AS latest_aqi, " +
                    "       COALESCE(ah.aqi_category, 'N/A') AS aqi_category, " +
                    "       COALESCE(ah.dominant_pollutant, 'N/A') AS dominant_pollutant, " +
                    "       COUNT(r.reading_id) AS total_readings, " +
                    "       COALESCE(AVG(CASE WHEN p.chemical_formula = 'PM2.5' THEN r.reading_value END), 0) AS avg_pm25, " +
                    "       COALESCE(AVG(CASE WHEN p.chemical_formula = 'PM10' THEN r.reading_value END), 0) AS avg_pm10, " +
                    "       COALESCE(MAX(CASE WHEN p.chemical_formula = 'PM2.5' THEN r.reading_value END), 0) AS max_pm25, " +
                    "       COALESCE(MAX(CASE WHEN p.chemical_formula = 'PM10' THEN r.reading_value END), 0) AS max_pm10 " +
                    "FROM station s " +
                    "LEFT JOIN aqi_history ah ON s.station_id = ah.station_id " +
                    "    AND ah.record_date = (SELECT MAX(record_date) FROM aqi_history WHERE station_id = s.station_id) " +
                    "LEFT JOIN reading r ON s.station_id = r.station_id " +
                    "    AND r.reading_timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                    "LEFT JOIN pollutant p ON r.pollutant_id = p.pollutant_id " +
                    "WHERE s.city_id = ? AND s.is_active = TRUE " +
                    "GROUP BY s.station_id, s.station_name, ah.aqi_value, ah.aqi_category, ah.dominant_pollutant " +
                    "ORDER BY s.station_name ASC";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(fallbackSql, cityId);
            for (Map<String, Object> r : rows) {
                reportRows.add(new CityReportRow(
                        (String) r.get("station_name"),
                        toBigDecimal(r.get("latest_aqi")),
                        (String) r.get("aqi_category"),
                        (String) r.get("dominant_pollutant"),
                        r.get("total_readings") != null ? ((Number) r.get("total_readings")).intValue() : 0,
                        toBigDecimal(r.get("avg_pm25")),
                        toBigDecimal(r.get("avg_pm10")),
                        toBigDecimal(r.get("max_pm25")),
                        toBigDecimal(r.get("max_pm10"))
                ));
            }

            // Audit in report_log
            try {
                reportLogRepository.save(new ReportLog(
                        null,
                        userId,
                        "Daily",
                        "{\"city_id\": " + cityId + ", \"status\": \"Completed via Java Fallback\"}",
                        LocalDateTime.now(),
                        "Completed"
                ));
            } catch (Exception ignored) {}

            return reportRows;
        }
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return BigDecimal.valueOf(((Number) obj).doubleValue());
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public List<AqiHistory> getAqiHistory(Integer cityId, Integer stationId, LocalDate fromDate, LocalDate toDate) {
        return aqiHistoryRepository.findByFilters(cityId, stationId, fromDate, toDate);
    }

    public List<HealthAdvisory> getAllAdvisories() {
        return healthAdvisoryRepository.findAll();
    }

    public List<ReportLog> getRecentReportLogs(int limit) {
        return reportLogRepository.findAll(limit);
    }

    /**
     * Generates a CSV export byte array from historical AQI records.
     */
    public byte[] exportAqiHistoryCsv(List<AqiHistory> records) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.println("Record Date,City,Station,AQI Value,Category,Dominant Pollutant,Data Source");
            for (AqiHistory r : records) {
                writer.printf("\"%s\",\"%s\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\"%n",
                        r.getRecordDate(),
                        escapeCsv(r.getCityName()),
                        escapeCsv(r.getStationName()),
                        r.getAqiValue() != null ? r.getAqiValue().doubleValue() : 0.0,
                        escapeCsv(r.getAqiCategory()),
                        escapeCsv(r.getDominantPollutant()),
                        escapeCsv(r.getDataSource())
                );
            }
            writer.flush();
        }
        return out.toByteArray();
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}

