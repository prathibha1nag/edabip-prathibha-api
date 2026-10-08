package com.edabip.billing.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangePlanRequest(@NotNull @Positive Long planId) {}
