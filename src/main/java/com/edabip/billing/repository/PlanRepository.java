package com.edabip.billing.repository;

import com.edabip.billing.model.Plan;
import com.edabip.billing.model.CreatePlanRequest;
import com.edabip.billing.model.PageResult;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.Statement;

@Repository
public class PlanRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Plan> mapper = (rs, row) -> new Plan(
            rs.getLong("id"), rs.getString("name"), rs.getBigDecimal("price"),
            rs.getInt("user_limit"), rs.getInt("storage_limit_gb"),
            rs.getInt("reports_per_month"), rs.getInt("active_dashboard_limit"),
            rs.getInt("data_processing_limit_gb"), rs.getString("support"), rs.getBoolean("active"));

    public PlanRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public PageResult<Plan> findAll(int page, int size, String sortColumn, String direction, String sortLabel) {
        long total = countAll();
        int totalPages = (int) ((total + size - 1) / size);
        List<Plan> items = jdbc.query("SELECT id, name, price, user_limit, storage_limit_gb, reports_per_month, active_dashboard_limit, data_processing_limit_gb, support, active " +
                "FROM plans ORDER BY " + sortColumn + " " + direction + ", id ASC LIMIT ? OFFSET ?", mapper, size, (long) page * size);
        return new PageResult<>(items, page, size, total, totalPages, sortLabel);
    }

    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM plans", Long.class);
        return count == null ? 0 : count;
    }

    public Optional<Plan> findById(long id) {
        return jdbc.query("SELECT id, name, price, user_limit, storage_limit_gb, reports_per_month, active_dashboard_limit, data_processing_limit_gb, support, active FROM plans WHERE id = ?", mapper, id)
                .stream().findFirst();
    }

    public Plan create(CreatePlanRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO plans (name, price, user_limit, storage_limit_gb, reports_per_month, active_dashboard_limit, data_processing_limit_gb, support) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.name());
            statement.setBigDecimal(2, request.price());
            statement.setInt(3, request.userLimit());
            statement.setInt(4, request.storageLimitGb());
            statement.setInt(5, request.reportsPerMonth());
            statement.setInt(6, request.activeDashboardLimit());
            statement.setInt(7, request.dataProcessingLimitGb());
            statement.setString(8, request.support());
            return statement;
        }, keyHolder);
        Number id = keyHolder.getKey();
        return findById(id.longValue()).orElseThrow();
    }

    public void setActive(long id, boolean active) {
        jdbc.update("UPDATE plans SET active = ? WHERE id = ?", active, id);
    }
}
