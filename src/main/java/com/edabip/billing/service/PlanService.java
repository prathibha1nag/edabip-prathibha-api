package com.edabip.billing.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.PageResult;
import com.edabip.billing.model.Plan;
import com.edabip.billing.model.CreatePlanRequest;
import com.edabip.billing.repository.PlanRepository;

@Service
public class PlanService {
    private static final Logger log = LoggerFactory.getLogger(PlanService.class);
    private final PlanRepository repository;

    public PlanService(PlanRepository repository) { this.repository = repository; }

    private static final int MAX_PAGE_SIZE = 100;

    public PageResult<Plan> findAll(int page, int size, String sort) {
        if (page < 0) throw new IllegalArgumentException("page must be zero or greater");
        if (size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        Sort selected = parseSort(sort);
        return repository.findAll(page, size, selected.column(), selected.direction(), selected.label());
    }

    private Sort parseSort(String sort) {
        String value = sort == null || sort.isBlank() ? "price,asc" : sort.trim();
        String[] parts = value.split(",", -1);
        if (parts.length != 2) throw new IllegalArgumentException("sort must use field,direction format");
        String column = switch (parts[0].trim().toLowerCase(Locale.ROOT)) {
            case "name" -> "name";
            case "price" -> "price";
            case "userlimit" -> "user_limit";
            case "storagelimitgb" -> "storage_limit_gb";
            case "reportspermonth" -> "reports_per_month";
            case "active" -> "active";
            default -> throw new IllegalArgumentException("sort field must be name, price, userLimit, storageLimitGb, reportsPerMonth or active");
        };
        String direction = switch (parts[1].trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> "ASC";
            case "desc" -> "DESC";
            default -> throw new IllegalArgumentException("sort direction must be asc or desc");
        };
        return new Sort(column, direction, parts[0].trim() + "," + direction.toLowerCase(Locale.ROOT));
    }

    private record Sort(String column, String direction, String label) {}

    @Transactional
    public Plan create(CreatePlanRequest request) {
        log.info("Creating plan name={}", request.name());
        return repository.create(request);
    }

    public Plan findById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Plan " + id + " was not found"));
    }

    @Transactional
    public Plan activate(long id) {
        log.info("Activating plan id={}", id);
        return setActive(id, true);
    }

    @Transactional
    public Plan deactivate(long id) {
        log.info("Deactivating plan id={}", id);
        return setActive(id, false);
    }

    private Plan setActive(long id, boolean active) {
        findById(id);
        repository.setActive(id, active);
        return findById(id);
    }
}
