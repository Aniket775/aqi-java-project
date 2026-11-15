package com.bigcoders.aqi.repository;

import com.bigcoders.aqi.model.AppUser;
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
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AppUser> rowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("created_at");
        return new AppUser(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("role"),
                ts != null ? ts.toLocalDateTime() : null
        );
    };

    public Optional<AppUser> findByUsername(String username) {
        String sql = "SELECT * FROM app_user WHERE username = ?";
        List<AppUser> list = jdbcTemplate.query(sql, rowMapper, username);
        return list.stream().findFirst();
    }

    public Optional<AppUser> findById(int userId) {
        String sql = "SELECT * FROM app_user WHERE user_id = ?";
        List<AppUser> list = jdbcTemplate.query(sql, rowMapper, userId);
        return list.stream().findFirst();
    }

    public List<AppUser> findAll() {
        String sql = "SELECT * FROM app_user ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public AppUser save(AppUser user) {
        String sql = "INSERT INTO app_user (username, email, password_hash, role) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            return ps;
        }, keyHolder);

        Integer id = RepositoryUtils.extractGeneratedKey(keyHolder, "user_id");
        if (id != null) {
            user.setUserId(id);
        }
        return user;
    }

    public void updateRole(int userId, String role) {
        String sql = "UPDATE app_user SET role = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, role, userId);
    }

    public void update(AppUser user) {
        String sql = "UPDATE app_user SET email = ?, password_hash = ?, role = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getPasswordHash(), user.getRole(), user.getUserId());
    }

    public void updatePassword(String username, String passwordHash) {
        String sql = "UPDATE app_user SET password_hash = ? WHERE username = ?";
        jdbcTemplate.update(sql, passwordHash, username);
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_user", Long.class);
        return count != null ? count : 0L;
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_user WHERE username = ?", Integer.class, username);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_user WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }
}

