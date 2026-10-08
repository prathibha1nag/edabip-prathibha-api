package com.edabip.billing.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.Plan;
import com.edabip.billing.repository.PlanRepository;

@Service
public class PlanService {
    private final PlanRepository repository;

    public PlanService(PlanRepository repository) { this.repository = repository; }

    public List<Plan> findAll() { return repository.findAll(); }

    public Plan findById(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Plan " + id + " was not found"));
    }

    public Plan activate(long id) { return setActive(id, true); }

    public Plan deactivate(long id) { return setActive(id, false); }

    private Plan setActive(long id, boolean active) {
        findById(id);
        repository.setActive(id, active);
        return findById(id);
    }
}
