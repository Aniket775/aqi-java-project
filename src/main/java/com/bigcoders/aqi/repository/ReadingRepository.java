package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.Reading;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class ReadingRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReadingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reading> rowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("reading_timestamp");
        Reading r = new Reading(
                rs.getInt("reading_id"),
                rs.getInt("station_id"),
                rs.getInt("pollutant_id"),
                rs.getBigDecimal("reading_value"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("data_quality"),
                rs.getString("data_source")
        );
        try {
            r.setStationName(rs.getString("station_name"));
            r.setPollutantFormula(rs.getString("chemical_formula"));
            r.setPollutantUnit(rs.getString("unit"));
            r.setCityName(rs.getString("city_name"));
        } catch (Exception ignored) {}
        return r;
    };

    public Reading save(Reading reading) {
        String sql = "INSERT INTO reading (station_id, pollutant_id, reading_value, reading_timestamp, data_quality, data_source) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, reading.getStationId());
            ps.setInt(2, reading.getPollutantId());
            ps.setBigDecimal(3, reading.getReadingValue());
            ps.setTimestamp(4, Timestamp.valueOf(reading.getReadingTimestamp()));
            ps.setString(5, reading.getDataQuality());
            ps.setString(6, reading.getDataSource());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "reading_id");
        if (id != null) {
            reading.setReadingId(id);
        }
        return reading;
    }

    public List<Reading> findByStationAndDate(int stationId, LocalDate date) {
        String sql = "SELECT r.*, s.station_name, p.chemical_formula, p.unit, c.city_name " +
                     "FROM reading r " +
                     "JOIN station s ON r.station_id = s.station_id " +
                     "JOIN pollutant p ON r.pollutant_id = p.pollutant_id " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "WHERE r.station_id = ? AND DATE(r.reading_timestamp) = ? " +
                     "ORDER BY r.reading_timestamp DESC";
        return jdbcTemplate.query(sql, rowMapper, stationId, date.toString());
    }

    public List<Reading> findRecentByStation(int stationId, int limit) {
        String sql = "SELECT r.*, s.station_name, p.chemical_formula, p.unit, c.city_name " +
                     "FROM reading r " +
                     "JOIN station s ON r.station_id = s.station_id " +
                     "JOIN pollutant p ON r.pollutant_id = p.pollutant_id " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "WHERE r.station_id = ? " +
                     "ORDER BY r.reading_timestamp DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, stationId, limit);
    }

    public List<Reading> findRecent(int limit) {
        String sql = "SELECT r.*, s.station_name, p.chemical_formula, p.unit, c.city_name " +
                     "FROM reading r " +
                     "JOIN station s ON r.station_id = s.station_id " +
                     "JOIN pollutant p ON r.pollutant_id = p.pollutant_id " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "ORDER BY r.reading_timestamp DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public int countDistinctPollutantsByStationAndDate(int stationId, LocalDate date) {
        String sql = "SELECT COUNT(DISTINCT pollutant_id) FROM reading " +
                     "WHERE station_id = ? AND DATE(reading_timestamp) = ? AND data_quality IN ('Good', 'Moderate')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, stationId, date.toString());
        return count != null ? count : 0;
    }

    /**
     * Aggregates readings for a station on a given date by pollutant:
     * returns pollutant formula, average concentration, reading count, and min/max timestamp span.
     */
    public List<Map<String, Object>> getAvgReadingsByStationAndDate(int stationId, LocalDate date) {
        String sql = "SELECT p.chemical_formula, p.averaging_period, " +
                     "       AVG(r.reading_value) AS avg_value, " +
                     "       COUNT(r.reading_id) AS reading_count, " +
                     "       MIN(r.reading_timestamp) AS first_reading, " +
                     "       MAX(r.reading_timestamp) AS last_reading " +
                     "FROM reading r " +
                     "JOIN pollutant p ON r.pollutant_id = p.pollutant_id " +
                     "WHERE r.station_id = ? " +
                     "  AND DATE(r.reading_timestamp) = ? " +
                     "  AND r.data_quality IN ('Good', 'Moderate') " +
                     "GROUP BY p.pollutant_id, p.chemical_formula, p.averaging_period";
        return jdbcTemplate.queryForList(sql, stationId, date.toString());
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reading", Long.class);
        return count != null ? count : 0L;
    }
}

