package com.edabip.billing.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSubscriptionRequest(
        @NotNull @Positive Long customerId,
        @NotNull @Positive Long planId,
        @NotNull BillingCycle billingCycle,
        @NotNull LocalDate currentPeriodStart,
        @NotNull LocalDate currentPeriodEnd) {}
