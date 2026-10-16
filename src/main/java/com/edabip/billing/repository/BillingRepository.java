package com.edabip.billing.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.edabip.billing.model.Invoice;
import com.edabip.billing.model.InvoiceSummary;
import com.edabip.billing.model.MonthlyInvoiceTotal;
import com.edabip.billing.model.PaymentMethod;

@Repository
public class BillingRepository {
    private final JdbcTemplate jdbc;

    public BillingRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<UsageSnapshot> findUsage(long customerId) {
        String sql = """
                SELECT p.name AS plan_name,
                       COALESCE(u.active_users, 0) AS active_users,
                       COALESCE(u.storage_used_gb, 0) AS storage_used_gb,
                       COALESCE(u.reports_generated, 0) AS reports_generated,
                       COALESCE(u.active_dashboards, 0) AS active_dashboards,
                       COALESCE(u.data_processing_gb, 0) AS data_processing_gb,
                       p.user_limit, p.storage_limit_gb, p.reports_per_month,
                       p.active_dashboard_limit, p.data_processing_limit_gb
                FROM subscriptions s
                JOIN plans p ON p.id = s.plan_id
                LEFT JOIN customer_usage u ON u.customer_id = s.customer_id
                WHERE s.customer_id = ? AND s.current_period_start <= CURRENT_DATE
                  AND s.current_period_end > CURRENT_DATE
                ORDER BY s.current_period_start DESC, s.id DESC LIMIT 1
                """;
        return jdbc.query(sql, (rs, row) -> new UsageSnapshot(rs.getString("plan_name"),
                rs.getBigDecimal("active_users"), rs.getBigDecimal("storage_used_gb"),
                rs.getBigDecimal("reports_generated"), rs.getBigDecimal("active_dashboards"),
                rs.getBigDecimal("data_processing_gb"), BigDecimal.valueOf(rs.getInt("user_limit")),
                BigDecimal.valueOf(rs.getInt("storage_limit_gb")), BigDecimal.valueOf(rs.getInt("reports_per_month")),
                BigDecimal.valueOf(rs.getInt("active_dashboard_limit")),
                BigDecimal.valueOf(rs.getInt("data_processing_limit_gb"))), customerId).stream().findFirst();
    }

    public List<Invoice> findInvoices(long customerId, String status, String sortColumn,
                                      String direction, int limit, long offset) {
        String sql = "SELECT id, period_start, period_end, amount, status, paid_on FROM invoices " +
                "WHERE customer_id = ? " + (status == null ? "" : "AND status = ? ") +
                "ORDER BY " + sortColumn + " " + direction + ", id DESC LIMIT ? OFFSET ?";
        return status == null
                ? jdbc.query(sql, (rs, row) -> mapInvoice(rs.getLong("id"), rs.getDate("period_start"),
                    rs.getDate("period_end"), rs.getBigDecimal("amount"), rs.getString("status"), rs.getDate("paid_on")),
                    customerId, limit, offset)
                : jdbc.query(sql, (rs, row) -> mapInvoice(rs.getLong("id"), rs.getDate("period_start"),
                    rs.getDate("period_end"), rs.getBigDecimal("amount"), rs.getString("status"), rs.getDate("paid_on")),
                    customerId, status, limit, offset);
    }

    public long countInvoices(long customerId, String status) {
        Long count = status == null
                ? jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE customer_id = ?", Long.class, customerId)
                : jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE customer_id = ? AND status = ?", Long.class, customerId, status);
        return count == null ? 0 : count;
    }

    public InvoiceSummary invoiceSummary(long customerId) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(CASE WHEN status='Paid' THEN amount ELSE 0 END),0) total_paid, " +
                "COUNT(*) total_invoices, COALESCE(SUM(CASE WHEN status='Due' THEN amount ELSE 0 END),0) outstanding " +
                "FROM invoices WHERE customer_id=?", (rs, row) -> new InvoiceSummary(
                rs.getBigDecimal("total_paid"), rs.getLong("total_invoices"), rs.getBigDecimal("outstanding")), customerId);
    }

    public List<MonthlyInvoiceTotal> monthlyTotals(long customerId) {
        return jdbc.query("WITH RECURSIVE months(month_start) AS (SELECT DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 5 MONTH),'%Y-%m-01') " +
                "UNION ALL SELECT DATE_ADD(month_start, INTERVAL 1 MONTH) FROM months WHERE month_start < DATE_FORMAT(CURRENT_DATE,'%Y-%m-01')) " +
                "SELECT DATE_FORMAT(m.month_start,'%Y-%m') month, COALESCE(SUM(i.amount),0) total FROM months m " +
                "LEFT JOIN invoices i ON i.customer_id=? AND i.period_start >= m.month_start AND i.period_start < DATE_ADD(m.month_start, INTERVAL 1 MONTH) " +
                "GROUP BY m.month_start ORDER BY m.month_start", (rs,row) ->
                new MonthlyInvoiceTotal(rs.getString("month"), rs.getBigDecimal("total")), customerId);
    }

    public List<PaymentMethod> paymentMethods(long customerId) {
        return jdbc.query("SELECT id,customer_id,brand,last_four,expiry_month,expiry_year,is_primary FROM payment_methods WHERE customer_id=? ORDER BY is_primary DESC,id",
                (rs,row)->new PaymentMethod(rs.getLong("id"),rs.getLong("customer_id"),rs.getString("brand"),rs.getString("last_four"),rs.getInt("expiry_month"),rs.getInt("expiry_year"),rs.getBoolean("is_primary")), customerId);
    }

    public int addPaymentMethod(long customerId, String brand, String lastFour, int month, int year, boolean primary) {
        return jdbc.update("INSERT INTO payment_methods(customer_id,brand,last_four,expiry_month,expiry_year,is_primary) VALUES(?,?,?,?,?,?)",
                customerId,brand,lastFour,month,year,primary);
    }

    public int setPaymentMethodPrimary(long customerId, long id, boolean primary) {
        return jdbc.update("UPDATE payment_methods SET is_primary=? WHERE customer_id=? AND id=?",primary,customerId,id);
    }

    public boolean isPaymentMethodPrimary(long customerId,long id) {
        List<Boolean> values=jdbc.query("SELECT is_primary FROM payment_methods WHERE customer_id=? AND id=?",(rs,row)->rs.getBoolean(1),customerId,id);
        return !values.isEmpty() && values.get(0);
    }

    public int deletePaymentMethod(long customerId,long id) {
        return jdbc.update("DELETE FROM payment_methods WHERE customer_id=? AND id=?",customerId,id);
    }

    public List<PaymentMethod> findPaymentMethod(long customerId,long id) {
        return jdbc.query("SELECT id,customer_id,brand,last_four,expiry_month,expiry_year,is_primary FROM payment_methods WHERE customer_id=? AND id=?",
                (rs,row)->new PaymentMethod(rs.getLong("id"),rs.getLong("customer_id"),rs.getString("brand"),rs.getString("last_four"),rs.getInt("expiry_month"),rs.getInt("expiry_year"),rs.getBoolean("is_primary")),customerId,id);
    }

    private Invoice mapInvoice(long id, Date start, Date end, BigDecimal amount, String status, Date paidOn) {
        return new Invoice(id, start.toLocalDate(), end.toLocalDate(), amount, status,
                paidOn == null ? null : paidOn.toLocalDate());
    }

    public record UsageSnapshot(String planName, BigDecimal activeUsers, BigDecimal storageUsedGb,
            BigDecimal reportsGenerated, BigDecimal activeDashboards, BigDecimal dataProcessingGb,
            BigDecimal userLimit, BigDecimal storageLimitGb, BigDecimal reportsPerMonth,
            BigDecimal activeDashboardLimit, BigDecimal dataProcessingLimitGb) {}
}
