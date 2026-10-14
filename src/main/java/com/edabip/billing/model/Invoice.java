package com.edabip.billing.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Invoice(Long id, LocalDate periodStart, LocalDate periodEnd, BigDecimal amount,
                      String status, LocalDate paidOn) {}
