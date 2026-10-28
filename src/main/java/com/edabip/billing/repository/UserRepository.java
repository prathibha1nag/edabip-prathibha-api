package com.edabip.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.edabip.billing.model.AuthUser;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<AuthUser> findByEmail(String email) {
        List<AuthUser> users = jdbc.query(
                "SELECT user_id, name, password, email, `role`, status FROM users WHERE LOWER(email) = ?",
                (rs, row) -> new AuthUser(rs.getLong("user_id"), rs.getString("name"),
                        rs.getString("password"), rs.getString("email"), rs.getString("role"),
                        rs.getString("status")), email);
        return users.stream().findFirst();
    }

    public void updateLastLogin(long userId) {
        jdbc.update("UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?", userId);
    }
}
