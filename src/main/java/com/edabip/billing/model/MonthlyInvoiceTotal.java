package com.edabip.billing.model;

import java.math.BigDecimal;

public record MonthlyInvoiceTotal(String month, BigDecimal total) {}
