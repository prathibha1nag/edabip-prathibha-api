package com.edabip.billing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotBlank String customerId,
        @NotNull @Positive Long planId,
        @NotNull BillingCycle billingCycle,
        @NotNull LocalDate currentPeriodStart,
        @NotNull LocalDate currentPeriodEnd) {}
