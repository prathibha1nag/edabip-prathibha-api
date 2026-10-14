package com.edabip.billing.model;

import java.math.BigDecimal;

public record UsageMetric(String metric, BigDecimal used, BigDecimal limit, BigDecimal percentageUsed) {}
