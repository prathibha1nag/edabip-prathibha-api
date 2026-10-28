package com.edabip.billing.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.edabip.billing.model.AuthUser;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {
    @Mock JdbcTemplate jdbc;
    @InjectMocks UserRepository repository;

    @Test
    void findByEmailReturnsUserFromParameterizedQuery() {
        AuthUser user = new AuthUser(4L, "Demo User", "demo123", "demo@example.com", "ADMIN", "ACTIVE");
        when(jdbc.query(eq("SELECT user_id, name, password, email, `role`, status FROM users WHERE LOWER(email) = ?"),
                org.mockito.ArgumentMatchers.<RowMapper<AuthUser>>any(), eq("demo@example.com")))
                .thenReturn(List.of(user));

        var result = repository.findByEmail("demo@example.com");

        assertTrue(result.isPresent());
        assertEquals(4L, result.get().userId());
        assertEquals("ADMIN", result.get().role());
    }

    @Test
    void updateLastLoginTargetsUserId() {
        repository.updateLastLogin(4L);

        verify(jdbc).update("UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?", 4L);
    }
}
