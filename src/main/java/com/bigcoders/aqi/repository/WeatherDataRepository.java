package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.WeatherData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class WeatherDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public WeatherDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<WeatherData> rowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("recorded_at");
        WeatherData w = new WeatherData(
                rs.getInt("weather_id"),
                rs.getInt("city_id"),
                rs.getBigDecimal("temperature"),
                rs.getBigDecimal("humidity"),
                rs.getBigDecimal("wind_speed"),
                rs.getString("wind_direction"),
                rs.getBigDecimal("precipitation"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("data_source")
        );
        try {
            w.setCityName(rs.getString("city_name"));
        } catch (Exception ignored) {}
        return w;
    };

    public WeatherData save(WeatherData weather) {
        String sql = "INSERT INTO weather_data (city_id, temperature, humidity, wind_speed, wind_direction, precipitation, recorded_at, data_source) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, weather.getCityId());
            ps.setBigDecimal(2, weather.getTemperature());
            ps.setBigDecimal(3, weather.getHumidity());
            ps.setBigDecimal(4, weather.getWindSpeed());
            ps.setString(5, weather.getWindDirection());
            ps.setBigDecimal(6, weather.getPrecipitation());
            ps.setTimestamp(7, Timestamp.valueOf(weather.getRecordedAt()));
            ps.setString(8, weather.getDataSource());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "weather_id");
        if (id != null) {
            weather.setWeatherId(id);
        }
        return weather;
    }

    public List<WeatherData> findLatestPerCity() {
        String sql = "SELECT w.*, c.city_name " +
                     "FROM weather_data w " +
                     "JOIN city c ON w.city_id = c.city_id " +
                     "WHERE w.recorded_at = ( " +
                     "    SELECT MAX(recorded_at) FROM weather_data WHERE city_id = w.city_id " +
                     ") " +
                     "ORDER BY c.city_name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<WeatherData> findLatestByCity(int cityId) {
        String sql = "SELECT w.*, c.city_name " +
                     "FROM weather_data w " +
                     "JOIN city c ON w.city_id = c.city_id " +
                     "WHERE w.city_id = ? " +
                     "ORDER BY w.recorded_at DESC LIMIT 1";
        List<WeatherData> list = jdbcTemplate.query(sql, rowMapper, cityId);
        return list.stream().findFirst();
    }
}

