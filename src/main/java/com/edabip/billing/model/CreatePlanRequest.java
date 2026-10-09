package com.edabip.billing.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePlanRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @Positive Integer userLimit,
        @NotNull @Positive Integer storageLimitGb,
        @NotNull @Min(0) Integer reportsPerMonth,
        @NotBlank String support) {}
