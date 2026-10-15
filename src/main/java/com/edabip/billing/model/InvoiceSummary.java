package com.edabip.billing.model;

import java.math.BigDecimal;

public record InvoiceSummary(BigDecimal totalPaid, long totalInvoices, BigDecimal outstandingAmount) {}
