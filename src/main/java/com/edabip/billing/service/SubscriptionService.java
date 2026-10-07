package com.edabip.billing.service;

import org.springframework.stereotype.Service;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.CreateSubscriptionRequest;
import com.edabip.billing.model.Plan;
import com.edabip.billing.model.Subscription;
import com.edabip.billing.repository.PlanRepository;
import com.edabip.billing.repository.SubscriptionRepository;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptions;
    private final PlanRepository plans;

    public SubscriptionService(SubscriptionRepository subscriptions, PlanRepository plans) {
        this.subscriptions = subscriptions;
        this.plans = plans;
    }

    public Subscription create(CreateSubscriptionRequest request) {
        if (!request.currentPeriodEnd().isAfter(request.currentPeriodStart())) {
            throw new IllegalArgumentException("currentPeriodEnd must be after currentPeriodStart");
        }
        Plan plan = plans.findById(request.planId())
                .orElseThrow(() -> new NotFoundException("Plan " + request.planId() + " was not found"));
        return subscriptions.create(request, plan.price());
    }

    public Subscription findById(long id) {
        return subscriptions.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription " + id + " was not found"));
    }

    public Subscription findCurrentByCustomerId(String customerId) {
        return subscriptions.findCurrentByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("No current subscription was found for customer " + customerId));
    }
}
