package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.City;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CityRepository {

    private final JdbcTemplate jdbcTemplate;

    public CityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<City> rowMapper = (rs, rowNum) -> new City(
            rs.getInt("city_id"),
            rs.getString("city_name"),
            rs.getString("state"),
            rs.getString("country"),
            rs.getBigDecimal("latitude"),
            rs.getBigDecimal("longitude")
    );

    public List<City> findAll() {
        String sql = "SELECT city_id, city_name, state, country, latitude, longitude FROM city ORDER BY city_name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<City> findById(int cityId) {
        String sql = "SELECT city_id, city_name, state, country, latitude, longitude FROM city WHERE city_id = ?";
        List<City> list = jdbcTemplate.query(sql, rowMapper, cityId);
        return list.stream().findFirst();
    }

    public City save(City city) {
        String sql = "INSERT INTO city (city_name, state, country, latitude, longitude) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, city.getCityName());
            ps.setString(2, city.getState());
            ps.setString(3, city.getCountry());
            ps.setBigDecimal(4, city.getLatitude());
            ps.setBigDecimal(5, city.getLongitude());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "city_id");
        if (id != null) {
            city.setCityId(id);
        }
        return city;
    }

    public void update(City city) {
        String sql = "UPDATE city SET city_name = ?, state = ?, country = ?, latitude = ?, longitude = ? WHERE city_id = ?";
        jdbcTemplate.update(sql, city.getCityName(), city.getState(), city.getCountry(),
                city.getLatitude(), city.getLongitude(), city.getCityId());
    }

    public void deleteById(int cityId) {
        String sql = "DELETE FROM city WHERE city_id = ?";
        jdbcTemplate.update(sql, cityId);
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM city", Long.class);
        return count != null ? count : 0L;
    }
}

