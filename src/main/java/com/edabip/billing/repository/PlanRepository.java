package com.edabip.billing.repository;

import com.edabip.billing.model.Plan;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PlanRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Plan> mapper = (rs, row) -> new Plan(
            rs.getLong("id"), rs.getString("name"), rs.getBigDecimal("price"),
            rs.getInt("user_limit"), rs.getInt("storage_limit_gb"),
            rs.getInt("reports_per_month"), rs.getString("support"), rs.getBoolean("active"));

    public PlanRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Plan> findAll() {
        return jdbc.query("SELECT id, name, price, user_limit, storage_limit_gb, reports_per_month, support, active FROM plans ORDER BY price, id", mapper);
    }

    public Optional<Plan> findById(long id) {
        return jdbc.query("SELECT id, name, price, user_limit, storage_limit_gb, reports_per_month, support, active FROM plans WHERE id = ?", mapper, id)
                .stream().findFirst();
    }

    public void setActive(long id, boolean active) {
        jdbc.update("UPDATE plans SET active = ? WHERE id = ?", active, id);
    }
}
