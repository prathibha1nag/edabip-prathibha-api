package com.edabip.billing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.Invoice;
import com.edabip.billing.model.PageResult;
import com.edabip.billing.model.UsageMetric;
import com.edabip.billing.model.UsageOverview;
import com.edabip.billing.model.InvoiceSummary;
import com.edabip.billing.model.MonthlyInvoiceTotal;
import com.edabip.billing.model.PaymentMethod;
import com.edabip.billing.model.CreatePaymentMethodRequest;
import com.edabip.billing.repository.BillingRepository;
import com.edabip.billing.repository.BillingRepository.UsageSnapshot;

@Service
public class BillingService {
    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private static final int MAX_PAGE_SIZE = 100;
    private final BillingRepository billing;

    public BillingService(BillingRepository billing) { this.billing = billing; }

    public UsageOverview getUsage(long customerId) {
        log.info("Loading usage overview for customerId={}", customerId);
        UsageSnapshot u = billing.findUsage(customerId)
                .orElseThrow(() -> new NotFoundException("No current subscription was found for customer " + customerId));
        return new UsageOverview(customerId, u.planName(), List.of(
                metric("Active Users", u.activeUsers(), u.userLimit()),
                metric("Storage (GB)", u.storageUsedGb(), u.storageLimitGb()),
                metric("Reports Generated", u.reportsGenerated(), u.reportsPerMonth()),
                metric("Active Dashboards", u.activeDashboards(), u.activeDashboardLimit()),
                metric("Data Processing (GB)", u.dataProcessingGb(), u.dataProcessingLimitGb())));
    }

    private UsageMetric metric(String name, BigDecimal used, BigDecimal limit) {
        BigDecimal percent = limit.signum() == 0
                ? (used.signum() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100))
                : used.multiply(BigDecimal.valueOf(100)).divide(limit, 2, RoundingMode.HALF_UP);
        return new UsageMetric(name, used, limit, percent);
    }

    public PageResult<Invoice> listInvoices(long customerId, int page, int size, String status, String sort) {
        if (page < 0) throw new IllegalArgumentException("page must be zero or greater");
        if (size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        String normalizedStatus = normalizeStatus(status);
        Sort selectedSort = parseSort(sort);
        log.info("Listing invoices for customerId={}, page={}, size={}, status={}", customerId, page, size, normalizedStatus);
        long total = billing.countInvoices(customerId, normalizedStatus);
        int totalPages = (int) ((total + size - 1) / size);
        List<Invoice> items = billing.findInvoices(customerId, normalizedStatus,
                selectedSort.column(), selectedSort.direction(), size, (long) page * size);
        return new PageResult<>(items, page, size, total, totalPages, selectedSort.label());
    }

    public InvoiceSummary invoiceSummary(long customerId) { return billing.invoiceSummary(customerId); }

    public List<MonthlyInvoiceTotal> monthlyInvoiceTotals(long customerId) { return billing.monthlyTotals(customerId); }

    public List<PaymentMethod> listPaymentMethods(long customerId) { return billing.paymentMethods(customerId); }

    @Transactional
    public PaymentMethod addPaymentMethod(long customerId, CreatePaymentMethodRequest request) {
        if (request.expiryYear() < java.time.Year.now().getValue() ||
                (request.expiryYear() == java.time.Year.now().getValue() && request.expiryMonth() < java.time.LocalDate.now().getMonthValue()))
            throw new IllegalArgumentException("payment method expiry must be in the future");
        List<PaymentMethod> existingMethods = billing.paymentMethods(customerId);
        boolean makePrimary = request.makePrimary() || existingMethods.isEmpty() ||
                existingMethods.stream().noneMatch(PaymentMethod::primary);
        if (makePrimary) existingMethods.stream().filter(PaymentMethod::primary)
                .findFirst().ifPresent(old -> billing.setPaymentMethodPrimary(customerId, old.id(), false));
        billing.addPaymentMethod(customerId, request.brand().trim(), request.lastFour(), request.expiryMonth(), request.expiryYear(), makePrimary);
        return billing.paymentMethods(customerId).stream().filter(m -> m.brand().equalsIgnoreCase(request.brand().trim()) && m.lastFour().equals(request.lastFour()))
                .findFirst().orElseThrow();
    }

    @Transactional
    public PaymentMethod setPrimaryPaymentMethod(long customerId, long id) {
        List<PaymentMethod> target = billing.findPaymentMethod(customerId, id);
        if (target.isEmpty()) throw new NotFoundException("Payment method " + id + " was not found for customer " + customerId);
        billing.paymentMethods(customerId).stream().filter(PaymentMethod::primary).filter(m -> !m.id().equals(id))
                .forEach(m -> billing.setPaymentMethodPrimary(customerId, m.id(), false));
        billing.setPaymentMethodPrimary(customerId, id, true);
        return billing.findPaymentMethod(customerId,id).get(0);
    }

    @Transactional
    public void removePaymentMethod(long customerId, long id) {
        List<PaymentMethod> method = billing.findPaymentMethod(customerId,id);
        if (method.isEmpty()) throw new NotFoundException("Payment method " + id + " was not found for customer " + customerId);
        if (method.get(0).primary()) throw new IllegalArgumentException("Choose another primary payment method before removing this one");
        billing.deletePaymentMethod(customerId,id);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return null;
        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "paid" -> "Paid";
            case "due" -> "Due";
            default -> throw new IllegalArgumentException("status must be Paid or Due");
        };
    }

    private Sort parseSort(String sort) {
        String value = sort == null || sort.isBlank() ? "periodStart,desc" : sort.trim();
        String[] parts = value.split(",", -1);
        if (parts.length != 2) throw new IllegalArgumentException("sort must use field,direction format");
        String column = switch (parts[0].trim().toLowerCase(Locale.ROOT)) {
            case "periodstart" -> "period_start";
            case "periodend" -> "period_end";
            case "amount" -> "amount";
            case "status" -> "status";
            case "paidon" -> "paid_on";
            default -> throw new IllegalArgumentException("sort field must be periodStart, periodEnd, amount, status or paidOn");
        };
        String direction = switch (parts[1].trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> "ASC";
            case "desc" -> "DESC";
            default -> throw new IllegalArgumentException("sort direction must be asc or desc");
        };
        return new Sort(column, direction, parts[0].trim() + "," + direction.toLowerCase(Locale.ROOT));
    }

    private record Sort(String column, String direction, String label) {}
}
