package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.HealthAdvisory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HealthAdvisoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public HealthAdvisoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<HealthAdvisory> rowMapper = (rs, rowNum) -> new HealthAdvisory(
            rs.getInt("advisory_id"),
            rs.getString("aqi_category"),
            rs.getString("general_message"),
            rs.getString("sensitive_group_message"),
            rs.getString("outdoor_guidance")
    );

    public List<HealthAdvisory> findAll() {
        String sql = "SELECT * FROM health_advisory ORDER BY advisory_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<HealthAdvisory> findByCategory(String category) {
        String sql = "SELECT * FROM health_advisory WHERE UPPER(aqi_category) = UPPER(?)";
        List<HealthAdvisory> list = jdbcTemplate.query(sql, rowMapper, category);
        return list.stream().findFirst();
    }
}

