package com.edabip.billing.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.ChangePlanRequest;
import com.edabip.billing.model.CreateSubscriptionRequest;
import com.edabip.billing.model.Plan;
import com.edabip.billing.model.Subscription;
import com.edabip.billing.repository.PlanRepository;
import com.edabip.billing.repository.SubscriptionRepository;

@Service
public class SubscriptionService {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private final SubscriptionRepository subscriptions;
    private final PlanRepository plans;

    public SubscriptionService(SubscriptionRepository subscriptions, PlanRepository plans) {
        this.subscriptions = subscriptions;
        this.plans = plans;
    }

    @Transactional
    public Subscription create(CreateSubscriptionRequest request) {
        log.info("Creating subscription customerId={}, planId={}", request.customerId(), request.planId());
        if (!request.currentPeriodEnd().isAfter(request.currentPeriodStart())) {
            throw new IllegalArgumentException("currentPeriodEnd must be after currentPeriodStart");
        }
        Plan plan = plans.findById(request.planId())
                .orElseThrow(() -> new NotFoundException("Plan " + request.planId() + " was not found"));
        requireActive(plan);
        return subscriptions.create(request, plan.price());
    }

    @Transactional
    public Subscription changePlan(long subscriptionId, ChangePlanRequest request) {
        log.info("Changing plan for subscriptionId={}, planId={}", subscriptionId, request.planId());
        subscriptions.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription " + subscriptionId + " was not found"));
        Plan plan = plans.findById(request.planId())
                .orElseThrow(() -> new NotFoundException("Plan " + request.planId() + " was not found"));
        requireActive(plan);
        subscriptions.changePlan(subscriptionId, plan.id(), plan.price());
        return findById(subscriptionId);
    }

    private void requireActive(Plan plan) {
        if (!plan.active()) throw new IllegalArgumentException("Plan " + plan.id() + " is inactive");
    }

    public Subscription findById(long id) {
        return subscriptions.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription " + id + " was not found"));
    }

    public Subscription findCurrentByCustomerId(Long customerId) {
        return subscriptions.findCurrentByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("No current subscription was found for customer " + customerId));
    }
}
