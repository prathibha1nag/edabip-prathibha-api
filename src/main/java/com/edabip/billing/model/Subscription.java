package com.edabip.billing.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record Subscription(Long id, String customerId, Long planId, String planName,
                           BillingCycle billingCycle, LocalDate currentPeriodStart,
                           LocalDate currentPeriodEnd, BigDecimal amountDue,
                           Instant lastPaymentAt) {}
