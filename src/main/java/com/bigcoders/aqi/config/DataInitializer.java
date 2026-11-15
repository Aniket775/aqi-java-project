package com.bigcoders.aqi.config;

import com.bigcoders.aqi.model.AppUser;
import com.bigcoders.aqi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        } catch (Exception ex) {
            log.warn("Database initialization check deferred: {}", ex.getMessage());
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

