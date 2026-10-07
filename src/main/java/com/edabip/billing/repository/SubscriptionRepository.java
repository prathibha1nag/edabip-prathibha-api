package com.edabip.billing.repository;

import com.edabip.billing.model.BillingCycle;
import com.edabip.billing.model.CreateSubscriptionRequest;
import com.edabip.billing.model.Subscription;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

@Repository
public class SubscriptionRepository {
    private static final String SELECT = """
            SELECT s.id, s.customer_id, s.plan_id, p.name AS plan_name, s.billing_cycle,
                   s.current_period_start, s.current_period_end, s.amount_due, s.last_payment_at
            FROM subscriptions s JOIN plans p ON p.id = s.plan_id
            """;
    private final JdbcTemplate jdbc;
    private final RowMapper<Subscription> mapper = (rs, row) -> {
        Timestamp payment = rs.getTimestamp("last_payment_at");
        return new Subscription(rs.getLong("id"), rs.getString("customer_id"),
                rs.getLong("plan_id"), rs.getString("plan_name"),
                BillingCycle.valueOf(rs.getString("billing_cycle").toUpperCase()),
                rs.getDate("current_period_start").toLocalDate(),
                rs.getDate("current_period_end").toLocalDate(),
                rs.getBigDecimal("amount_due"), payment == null ? null : payment.toInstant());
    };

    public SubscriptionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Subscription create(CreateSubscriptionRequest request, java.math.BigDecimal amountDue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator creator = (Connection connection) -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO subscriptions (customer_id, plan_id, billing_cycle, current_period_start, current_period_end, amount_due) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.customerId());
            statement.setLong(2, request.planId());
            statement.setString(3, request.billingCycle().name().toLowerCase());
            statement.setObject(4, request.currentPeriodStart());
            statement.setObject(5, request.currentPeriodEnd());
            statement.setBigDecimal(6, amountDue);
            return statement;
        };
        jdbc.update(creator, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return findById(id).orElseThrow();
    }

    public Optional<Subscription> findById(long id) {
        return jdbc.query(SELECT + " WHERE s.id = ?", mapper, id).stream().findFirst();
    }

    public Optional<Subscription> findCurrentByCustomerId(String customerId) {
        return jdbc.query(SELECT + " WHERE s.customer_id = ? AND s.current_period_start <= CURRENT_DATE " +
                        "AND s.current_period_end > CURRENT_DATE ORDER BY s.current_period_start DESC, s.id DESC LIMIT 1",
                mapper, customerId).stream().findFirst();
    }
}
