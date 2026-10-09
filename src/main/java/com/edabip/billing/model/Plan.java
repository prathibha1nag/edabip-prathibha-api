package com.edabip.billing.model;

import java.math.BigDecimal;

public record Plan(Long id, String name, BigDecimal price, Integer userLimit,
                   Integer storageLimitGb, Integer reportsPerMonth, String support, boolean active) {}
