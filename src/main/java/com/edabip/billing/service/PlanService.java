package com.edabip.billing.service;

import com.edabip.billing.common.NotFoundException;
import com.edabip.billing.model.Plan;
import com.edabip.billing.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {
    private final PlanRepository repository;

    public PlanService(PlanRepository repository) { this.repository = repository; }

    public List<Plan> findAll() { return repository.findAll(); }

    public Plan findById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Plan " + id + " was not found"));
    }
}
