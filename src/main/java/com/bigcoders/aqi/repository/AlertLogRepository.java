package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.AlertLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class AlertLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AlertLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AlertLog> rowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("alert_timestamp");
        AlertLog a = new AlertLog(
                rs.getInt("alert_id"),
                rs.getInt("station_id"),
                rs.getString("alert_type"),
                rs.getString("severity"),
                rs.getString("message"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getBoolean("is_resolved")
        );
        try {
            a.setStationName(rs.getString("station_name"));
            a.setCityName(rs.getString("city_name"));
        } catch (Exception ignored) {}
        return a;
    };

    public AlertLog save(AlertLog alert) {
        String sql = "INSERT INTO alert_log (station_id, alert_type, severity, message, alert_timestamp, is_resolved) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, alert.getStationId());
            ps.setString(2, alert.getAlertType());
            ps.setString(3, alert.getSeverity());
            ps.setString(4, alert.getMessage());
            ps.setTimestamp(5, Timestamp.valueOf(alert.getAlertTimestamp()));
            ps.setBoolean(6, alert.isResolved());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "alert_id");
        if (id != null) {
            alert.setAlertId(id);
        }
        return alert;
    }

    public List<AlertLog> findRecent(int limit) {
        String sql = "SELECT al.*, s.station_name, c.city_name " +
                     "FROM alert_log al " +
                     "JOIN station s ON al.station_id = s.station_id " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "ORDER BY al.alert_timestamp DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public List<AlertLog> findAll(String severity, Boolean isResolved, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT al.*, s.station_name, c.city_name " +
                "FROM alert_log al " +
                "JOIN station s ON al.station_id = s.station_id " +
                "JOIN city c ON s.city_id = c.city_id WHERE 1=1 "
        );
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (severity != null && !severity.isBlank() && !severity.equalsIgnoreCase("ALL")) {
            sql.append("AND al.severity = ? ");
            params.add(severity);
        }
        if (isResolved != null) {
            sql.append("AND al.is_resolved = ? ");
            params.add(isResolved);
        }

        sql.append("ORDER BY al.alert_timestamp DESC LIMIT ?");
        params.add(limit);
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public long countUnresolved() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM alert_log WHERE is_resolved = FALSE", Long.class);
        return count != null ? count : 0L;
    }

    public void resolve(int alertId) {
        String sql = "UPDATE alert_log SET is_resolved = TRUE WHERE alert_id = ?";
        jdbcTemplate.update(sql, alertId);
    }

    /**
     * Checks if an identical unresolved alert was created within the last 4 hours for this station,
     * preventing alert flooding/duplication.
     */
    public boolean existsRecentDuplicate(int stationId, String alertType, String severity) {
        String sql = "SELECT COUNT(*) FROM alert_log " +
                     "WHERE station_id = ? AND alert_type = ? AND severity = ? AND is_resolved = FALSE " +
                     "  AND alert_timestamp >= DATE_SUB(NOW(), INTERVAL 4 HOUR)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, stationId, alertType, severity);
        return count != null && count > 0;
    }
}

