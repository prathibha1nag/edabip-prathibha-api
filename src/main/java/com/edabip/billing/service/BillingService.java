package com.edabip.billing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.Invoice;
import com.edabip.billing.model.PageResult;
import com.edabip.billing.model.UsageMetric;
import com.edabip.billing.model.UsageOverview;
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
