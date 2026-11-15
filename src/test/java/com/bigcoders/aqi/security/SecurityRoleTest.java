package com.bigcoders.aqi.security;

import com.bigcoders.aqi.model.AppUser;
import com.bigcoders.aqi.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security & Role-based Access Control Unit Tests")
class SecurityRoleTest {

    static class FakeUserRepository extends UserRepository {
        private final Map<String, AppUser> users = new HashMap<>();

        public FakeUserRepository() {
            super(null);
        }

        public void addUser(AppUser user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public Optional<AppUser> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }
    }

    @Test
    @DisplayName("User roles are properly mapped with ROLE_ prefix for Spring Security")
    void testRoleMapping() {
        FakeUserRepository fakeRepo = new FakeUserRepository();
        AppUser operatorUser = new AppUser(3, "operator", "op@aqi.in", "bcrypt_hash", "Operator", LocalDateTime.now());
        fakeRepo.addUser(operatorUser);

        CustomUserDetailsService service = new CustomUserDetailsService(fakeRepo);
        UserDetails userDetails = service.loadUserByUsername("operator");

        assertEquals("operator", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERATOR")));
    }

    @Test
    @DisplayName("Non-existent user throws UsernameNotFoundException")
    void testUserNotFound() {
        FakeUserRepository fakeRepo = new FakeUserRepository();
        CustomUserDetailsService service = new CustomUserDetailsService(fakeRepo);
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost"));
    }
}
