package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.Station;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class StationRepository {

    private final JdbcTemplate jdbcTemplate;

    public StationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Station> rowMapper = (rs, rowNum) -> {
        Date installDate = rs.getDate("installation_date");
        Station s = new Station(
                rs.getInt("station_id"),
                rs.getInt("city_id"),
                rs.getString("station_name"),
                rs.getString("location_desc"),
                rs.getString("station_type"),
                installDate != null ? installDate.toLocalDate() : null,
                rs.getBoolean("is_active")
        );
        try {
            s.setCityName(rs.getString("city_name"));
        } catch (Exception ignored) {}
        return s;
    };

    public List<Station> findAll() {
        String sql = "SELECT s.*, c.city_name FROM station s " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "ORDER BY c.city_name ASC, s.station_name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Station> findActive() {
        String sql = "SELECT s.*, c.city_name FROM station s " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "WHERE s.is_active = TRUE " +
                     "ORDER BY c.city_name ASC, s.station_name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Station> findByCityId(int cityId) {
        String sql = "SELECT s.*, c.city_name FROM station s " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "WHERE s.city_id = ? " +
                     "ORDER BY s.station_name ASC";
        return jdbcTemplate.query(sql, rowMapper, cityId);
    }

    public Optional<Station> findById(int stationId) {
        String sql = "SELECT s.*, c.city_name FROM station s " +
                     "JOIN city c ON s.city_id = c.city_id " +
                     "WHERE s.station_id = ?";
        List<Station> list = jdbcTemplate.query(sql, rowMapper, stationId);
        return list.stream().findFirst();
    }

    public Station save(Station station) {
        String sql = "INSERT INTO station (city_id, station_name, location_desc, station_type, installation_date, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, station.getCityId());
            ps.setString(2, station.getStationName());
            ps.setString(3, station.getLocationDesc());
            ps.setString(4, station.getStationType());
            ps.setDate(5, station.getInstallationDate() != null ? Date.valueOf(station.getInstallationDate()) : null);
            ps.setBoolean(6, station.isActive());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "station_id");
        if (id != null) {
            station.setStationId(id);
        }
        return station;
    }

    public void update(Station station) {
        String sql = "UPDATE station SET city_id = ?, station_name = ?, location_desc = ?, " +
                     "station_type = ?, installation_date = ?, is_active = ? WHERE station_id = ?";
        jdbcTemplate.update(sql,
                station.getCityId(),
                station.getStationName(),
                station.getLocationDesc(),
                station.getStationType(),
                station.getInstallationDate() != null ? Date.valueOf(station.getInstallationDate()) : null,
                station.isActive(),
                station.getStationId());
    }

    public void toggleActive(int stationId, boolean active) {
        String sql = "UPDATE station SET is_active = ? WHERE station_id = ?";
        jdbcTemplate.update(sql, active, stationId);
    }

    public long countActive() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM station WHERE is_active = TRUE", Long.class);
        return count != null ? count : 0L;
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM station", Long.class);
        return count != null ? count : 0L;
    }
}

