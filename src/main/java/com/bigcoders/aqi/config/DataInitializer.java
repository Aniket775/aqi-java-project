package com.bigcoders.aqi.config;

import com.bigcoders.aqi.model.AppUser;
import com.bigcoders.aqi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            ensureUser("admin", "admin@aqisystem.in", "admin123", "Admin");
            ensureUser("analyst", "analyst@aqisystem.in", "analyst123", "Analyst");
            ensureUser("operator", "operator@aqisystem.in", "operator123", "Operator");
            ensureUser("viewer", "viewer@aqisystem.in", "viewer123", "Viewer");

            log.info("Default demonstration accounts verified and updated with BCrypt hashes:");
            log.info(" -> Admin:    username=admin    | password=admin123");
            log.info(" -> Analyst:  username=analyst  | password=analyst123");
            log.info(" -> Operator: username=operator | password=operator123");
            log.info(" -> Viewer:   username=viewer   | password=viewer123");

            synchronizeDatesToToday();
        } catch (Exception ex) {
            log.warn("Database initialization check deferred: {}", ex.getMessage());
        }
    }

    private void synchronizeDatesToToday() {
        try {
            LocalDate today = LocalDate.now();
            Date maxSqlDate = jdbcTemplate.queryForObject("SELECT MAX(record_date) FROM aqi_history", Date.class);
            if (maxSqlDate == null) {
                return;
            }
            LocalDate maxDate = maxSqlDate.toLocalDate();
            long daysShift = ChronoUnit.DAYS.between(maxDate, today);
            if (daysShift == 0) {
                return;
            }

            log.info("Synchronizing historical AQI records: shifting by {} days from {} to current date {}", daysShift, maxDate, today);

            // 1. Shift aqi_history
            List<Map<String, Object>> aqiRows = jdbcTemplate.queryForList("SELECT aqi_id, record_date FROM aqi_history");
            for (Map<String, Object> row : aqiRows) {
                Number id = (Number) row.get("aqi_id");
                Date rd = (Date) row.get("record_date");
                if (id != null && rd != null) {
                    LocalDate newDate = rd.toLocalDate().plusDays(daysShift);
                    jdbcTemplate.update("UPDATE aqi_history SET record_date = ? WHERE aqi_id = ?", Date.valueOf(newDate), id.intValue());
                }
            }

            // 2. Shift reading
            List<Map<String, Object>> readingRows = jdbcTemplate.queryForList("SELECT reading_id, reading_timestamp FROM reading");
            for (Map<String, Object> r : readingRows) {
                Number id = (Number) r.get("reading_id");
                Timestamp ts = (Timestamp) r.get("reading_timestamp");
                if (id != null && ts != null) {
                    LocalDateTime newTs = ts.toLocalDateTime().plusDays(daysShift);
                    jdbcTemplate.update("UPDATE reading SET reading_timestamp = ? WHERE reading_id = ?", Timestamp.valueOf(newTs), id.intValue());
                }
            }

            // 3. Shift weather_data
            List<Map<String, Object>> weatherRows = jdbcTemplate.queryForList("SELECT weather_id, recorded_at FROM weather_data");
            for (Map<String, Object> w : weatherRows) {
                Number id = (Number) w.get("weather_id");
                Timestamp ts = (Timestamp) w.get("recorded_at");
                if (id != null && ts != null) {
                    LocalDateTime newTs = ts.toLocalDateTime().plusDays(daysShift);
                    jdbcTemplate.update("UPDATE weather_data SET recorded_at = ? WHERE weather_id = ?", Timestamp.valueOf(newTs), id.intValue());
                }
            }

            // 4. Shift alert_log
            List<Map<String, Object>> alertRows = jdbcTemplate.queryForList("SELECT alert_id, alert_timestamp FROM alert_log");
            for (Map<String, Object> a : alertRows) {
                Number id = (Number) a.get("alert_id");
                Timestamp ts = (Timestamp) a.get("alert_timestamp");
                if (id != null && ts != null) {
                    LocalDateTime newTs = ts.toLocalDateTime().plusDays(daysShift);
                    jdbcTemplate.update("UPDATE alert_log SET alert_timestamp = ? WHERE alert_id = ?", Timestamp.valueOf(newTs), id.intValue());
                }
            }

            // 5. Shift report_log
            List<Map<String, Object>> reportRows = jdbcTemplate.queryForList("SELECT report_id, generated_at FROM report_log");
            for (Map<String, Object> rep : reportRows) {
                Number id = (Number) rep.get("report_id");
                Timestamp ts = (Timestamp) rep.get("generated_at");
                if (id != null && ts != null) {
                    LocalDateTime newTs = ts.toLocalDateTime().plusDays(daysShift);
                    jdbcTemplate.update("UPDATE report_log SET generated_at = ? WHERE report_id = ?", Timestamp.valueOf(newTs), id.intValue());
                }
            }

            log.info("Database records successfully synchronized to current date ({}).", today);

        } catch (Exception ex) {
            log.warn("Date synchronization check deferred: {}", ex.getMessage());
        }
    }

    private void ensureUser(String username, String email, String rawPassword, String role) {
        var existing = userRepository.findByUsername(username);
        String encoded = passwordEncoder.encode(rawPassword);
        if (existing.isPresent()) {
            AppUser u = existing.get();
            u.setPasswordHash(encoded);
            u.setRole(role);
            userRepository.update(u);
        } else {
            AppUser user = new AppUser(
                    null,
                    username,
                    email,
                    encoded,
                    role,
                    LocalDateTime.now()
            );
            userRepository.save(user);
        }
    }
}

