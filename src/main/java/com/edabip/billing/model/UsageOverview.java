package com.edabip.billing.model;

import java.util.List;

public record UsageOverview(Long customerId, String planName, List<UsageMetric> metrics) {}
