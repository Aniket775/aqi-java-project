package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.ReportLog;
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
public class ReportLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ReportLog> rowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("generated_at");
        ReportLog r = new ReportLog(
                rs.getInt("report_id"),
                rs.getInt("user_id"),
                rs.getString("report_type"),
                rs.getString("parameters"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("status")
        );
        try {
            r.setUsername(rs.getString("username"));
        } catch (Exception ignored) {}
        return r;
    };

    public ReportLog save(ReportLog report) {
        String sql = "INSERT INTO report_log (user_id, report_type, parameters, generated_at, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, report.getUserId());
            ps.setString(2, report.getReportType());
            ps.setString(3, report.getParameters());
            ps.setTimestamp(4, Timestamp.valueOf(report.getGeneratedAt()));
            ps.setString(5, report.getStatus());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "report_id");
        if (id != null) {
            report.setReportId(id);
        }
        return report;
    }

    public List<ReportLog> findAll(int limit) {
        String sql = "SELECT rl.*, u.username " +
                     "FROM report_log rl " +
                     "JOIN app_user u ON rl.user_id = u.user_id " +
                     "ORDER BY rl.generated_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public void updateStatus(int reportId, String status) {
        String sql = "UPDATE report_log SET status = ? WHERE report_id = ?";
        jdbcTemplate.update(sql, status, reportId);
    }
}

