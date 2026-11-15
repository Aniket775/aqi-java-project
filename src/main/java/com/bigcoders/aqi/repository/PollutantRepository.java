package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.Pollutant;
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
public class PollutantRepository {

    private final JdbcTemplate jdbcTemplate;

    public PollutantRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Pollutant> rowMapper = (rs, rowNum) -> new Pollutant(
            rs.getInt("pollutant_id"),
            rs.getString("pollutant_name"),
            rs.getString("chemical_formula"),
            rs.getString("unit"),
            rs.getBigDecimal("safe_limit"),
            rs.getString("averaging_period"),
            rs.getString("description")
    );

    public List<Pollutant> findAll() {
        String sql = "SELECT * FROM pollutant ORDER BY pollutant_name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Pollutant> findById(int pollutantId) {
        String sql = "SELECT * FROM pollutant WHERE pollutant_id = ?";
        List<Pollutant> list = jdbcTemplate.query(sql, rowMapper, pollutantId);
        return list.stream().findFirst();
    }

    public Optional<Pollutant> findByFormula(String formula) {
        String sql = "SELECT * FROM pollutant WHERE UPPER(chemical_formula) = UPPER(?)";
        List<Pollutant> list = jdbcTemplate.query(sql, rowMapper, formula);
        return list.stream().findFirst();
    }

    public Pollutant save(Pollutant pollutant) {
        String sql = "INSERT INTO pollutant (pollutant_name, chemical_formula, unit, safe_limit, averaging_period, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, pollutant.getPollutantName());
            ps.setString(2, pollutant.getChemicalFormula());
            ps.setString(3, pollutant.getUnit());
            ps.setBigDecimal(4, pollutant.getSafeLimit());
            ps.setString(5, pollutant.getAveragingPeriod());
            ps.setString(6, pollutant.getDescription());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "pollutant_id");
        if (id != null) {
            pollutant.setPollutantId(id);
        }
        return pollutant;
    }

    public void update(Pollutant pollutant) {
        String sql = "UPDATE pollutant SET pollutant_name = ?, chemical_formula = ?, unit = ?, " +
                     "safe_limit = ?, averaging_period = ?, description = ? WHERE pollutant_id = ?";
        jdbcTemplate.update(sql,
                pollutant.getPollutantName(),
                pollutant.getChemicalFormula(),
                pollutant.getUnit(),
                pollutant.getSafeLimit(),
                pollutant.getAveragingPeriod(),
                pollutant.getDescription(),
                pollutant.getPollutantId());
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pollutant", Long.class);
        return count != null ? count : 0L;
    }
}

