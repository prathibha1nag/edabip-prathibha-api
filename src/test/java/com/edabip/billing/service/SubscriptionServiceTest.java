package com.edabip.billing.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.edabip.billing.exceptionHandler.NotFoundException;
import com.edabip.billing.model.BillingCycle;
import com.edabip.billing.model.ChangePlanRequest;
import com.edabip.billing.model.CreateSubscriptionRequest;
import com.edabip.billing.model.Plan;
import com.edabip.billing.model.Subscription;
import com.edabip.billing.repository.PlanRepository;
import com.edabip.billing.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
    @Mock SubscriptionRepository subscriptions;
    @Mock PlanRepository plans;
    @InjectMocks SubscriptionService service;

    @Test
    void createUsesActivePlanPrice() {
        var request = new CreateSubscriptionRequest(7L, 2L, BillingCycle.MONTHLY,
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-11-01"));
        var expected = subscription(2L, "Business");
        when(plans.findById(2)).thenReturn(Optional.of(plan(true)));
        when(subscriptions.create(request, new BigDecimal("29.99"))).thenReturn(expected);
        assertEquals(expected, service.create(request));
    }

    @Test
    void createRejectsInvalidPeriodBeforeDatabaseCalls() {
        var request = new CreateSubscriptionRequest(7L, 2L, BillingCycle.MONTHLY,
                LocalDate.parse("2026-11-01"), LocalDate.parse("2026-10-01"));
        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verifyNoInteractions(plans, subscriptions);
    }

    @Test
    void createRejectsMissingPlan() {
        var request = new CreateSubscriptionRequest(7L, 99L, BillingCycle.MONTHLY,
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-11-01"));
        when(plans.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(request));
        verify(subscriptions, never()).create(any(), any());
    }

    @Test
    void createRejectsInactivePlan() {
        var request = new CreateSubscriptionRequest(7L, 2L, BillingCycle.MONTHLY,
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-11-01"));
        when(plans.findById(2)).thenReturn(Optional.of(plan(false)));
        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(subscriptions, never()).create(any(), any());
    }

    @Test
    void changePlanUpdatesSubscriptionPrice() {
        var existing = subscription(1L, "Basic");
        var changed = subscription(2L, "Business");
        when(subscriptions.findById(10)).thenReturn(Optional.of(existing), Optional.of(changed));
        when(plans.findById(2)).thenReturn(Optional.of(plan(true)));
        var result = service.changePlan(10, new ChangePlanRequest(2L));
        assertEquals("Business", result.planName());
        verify(subscriptions).changePlan(10, 2, new BigDecimal("29.99"));
    }

    @Test
    void changePlanRejectsMissingSubscription() {
        when(subscriptions.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.changePlan(99, new ChangePlanRequest(2L)));
        verifyNoInteractions(plans);
    }

    private Plan plan(boolean active) {
        return new Plan(2L, "Business", new BigDecimal("29.99"), 25, 100, 1000, 15, 200, "Priority email", active);
    }

    private Subscription subscription(long planId, String name) {
        return new Subscription(10L, 7L, planId, name, BillingCycle.MONTHLY,
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-11-01"),
                new BigDecimal(planId == 1 ? "9.99" : "29.99"), null);
    }
}
