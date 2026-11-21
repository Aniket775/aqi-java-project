package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.AqiHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AqiHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public AqiHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AqiHistory> rowMapper = (rs, rowNum) -> {
        Date d = rs.getDate("record_date");
        AqiHistory h = new AqiHistory(
                rs.getInt("aqi_id"),
                rs.getInt("station_id"),
                rs.getBigDecimal("aqi_value"),
                rs.getString("aqi_category"),
                d != null ? d.toLocalDate() : null,
                rs.getString("dominant_pollutant"),
                rs.getString("data_source")
        );
        try {
            h.setStationName(rs.getString("station_name"));
            h.setCityName(rs.getString("city_name"));
            h.setState(rs.getString("state"));
            int cid = rs.getInt("city_id");
            if (!rs.wasNull()) {
                h.setCityId(cid);
            }
        } catch (Exception ignored) {}
        return h;
    };

    public AqiHistory save(AqiHistory history) {
        String sql = "INSERT INTO aqi_history (station_id, aqi_value, aqi_category, record_date, dominant_pollutant, data_source) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, history.getStationId());
            ps.setBigDecimal(2, history.getAqiValue());
            ps.setString(3, history.getAqiCategory());
            ps.setDate(4, Date.valueOf(history.getRecordDate()));
            ps.setString(5, history.getDominantPollutant());
            ps.setString(6, history.getDataSource());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "aqi_id");
        if (id != null) {
            history.setAqiId(id);
        }
        return history;
    }

    public void update(AqiHistory history) {
        String sql = "UPDATE aqi_history SET aqi_value = ?, aqi_category = ?, dominant_pollutant = ?, data_source = ? " +
                     "WHERE aqi_id = ?";
        jdbcTemplate.update(sql,
                history.getAqiValue(),
                history.getAqiCategory(),
                history.getDominantPollutant(),
                history.getDataSource(),
                history.getAqiId());
    }

    public void saveOrUpdate(AqiHistory history) {
        Optional<AqiHistory> existing = findByStationAndDate(history.getStationId(), history.getRecordDate());
        if (existing.isPresent()) {
            history.setAqiId(existing.get().getAqiId());
            update(history);
        } else {
            save(history);
        }
    }

    public Optional<AqiHistory> findByStationAndDate(int stationId, LocalDate date) {
        String sql = "SELECT ah.*, s.station_name, s.city_id, c.city_name, c.state " +
                     "FROM aqi_history ah " +
                     "JOIN station s ON ah.station_id = s.station_id " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "WHERE ah.station_id = ? AND ah.record_date = ?";
        List<AqiHistory> list = jdbcTemplate.query(sql, rowMapper, stationId, Date.valueOf(date));
        return list.stream().findFirst();
    }

    public List<AqiHistory> findLatestPerStation() {
        return findLatestPerStation(null);
    }

    public List<AqiHistory> findLatestPerStation(Integer cityId) {
        StringBuilder sql = new StringBuilder(
            "SELECT ah.*, s.station_name, s.city_id, c.city_name, c.state " +
            "FROM station s " +
            "JOIN city c ON s.city_id = c.city_id " +
            "LEFT JOIN aqi_history ah ON s.station_id = ah.station_id " +
            "    AND ah.record_date = ( " +
            "        SELECT MAX(record_date) FROM aqi_history WHERE station_id = s.station_id " +
            "    ) " +
            "WHERE s.is_active = TRUE "
        );
        if (cityId != null) {
            sql.append("AND s.city_id = ? ");
            sql.append("ORDER BY ah.aqi_value DESC, s.station_name ASC");
            return jdbcTemplate.query(sql.toString(), rowMapper, cityId);
        } else {
            sql.append("ORDER BY ah.aqi_value DESC, s.station_name ASC");
            return jdbcTemplate.query(sql.toString(), rowMapper);
        }
    }

    public List<AqiHistory> findAll(int limit) {
        String sql = "SELECT ah.*, s.station_name, s.city_id, c.city_name, c.state " +
                     "FROM aqi_history ah " +
                     "JOIN station s ON ah.station_id = s.station_id " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "ORDER BY ah.record_date DESC, ah.aqi_value DESC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public List<AqiHistory> findByStationId(int stationId, int limit) {
        String sql = "SELECT ah.*, s.station_name, s.city_id, c.city_name, c.state " +
                     "FROM aqi_history ah " +
                     "JOIN station s ON ah.station_id = s.station_id " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "WHERE ah.station_id = ? " +
                     "ORDER BY ah.record_date DESC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, stationId, limit);
    }

    public List<AqiHistory> findHistoricalPointsAsc(int stationId, int days) {
        String sql = "SELECT ah.*, s.station_name, s.city_id, c.city_name, c.state " +
                     "FROM aqi_history ah " +
                     "JOIN station s ON ah.station_id = s.station_id " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "WHERE ah.station_id = ? " +
                     "ORDER BY ah.record_date ASC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, stationId, days);
    }

    public List<AqiHistory> findByFilters(Integer cityId, Integer stationId, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT ah.*, s.station_name, s.city_id, c.city_name, c.state " +
                "FROM aqi_history ah " +
                "JOIN station s ON ah.station_id = s.station_id " +
                "JOIN city c ON s.city_id = c.city_id WHERE 1=1 "
        );
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (cityId != null) {
            sql.append("AND s.city_id = ? ");
            params.add(cityId);
        }
        if (stationId != null) {
            sql.append("AND ah.station_id = ? ");
            params.add(stationId);
        }
        if (fromDate != null) {
            sql.append("AND ah.record_date >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append("AND ah.record_date <= ? ");
            params.add(Date.valueOf(toDate));
        }

        sql.append("ORDER BY ah.record_date DESC, ah.aqi_value DESC LIMIT 100");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public List<Map<String, Object>> getCityAverages() {
        String sql = "SELECT c.city_id, c.city_name, c.state, " +
                     "       ROUND(AVG(ah.aqi_value), 2) AS avg_aqi, " +
                     "       fn_get_aqi_category(AVG(ah.aqi_value)) AS avg_category, " +
                     "       COUNT(DISTINCT s.station_id) AS station_count " +
                     "FROM city c " +
                     "JOIN station s ON c.city_id = s.city_id " +
                     "JOIN aqi_history ah ON s.station_id = ah.station_id " +
                     "WHERE ah.record_date = (SELECT MAX(record_date) FROM aqi_history) " +
                     "GROUP BY c.city_id, c.city_name, c.state " +
                     "ORDER BY avg_aqi DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public double getOverallNationalAverage() {
        String sql = "SELECT AVG(ah.aqi_value) FROM aqi_history ah " +
                     "WHERE ah.record_date = (SELECT MAX(record_date) FROM aqi_history)";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class);
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM aqi_history", Long.class);
        return count != null ? count : 0L;
    }
}

