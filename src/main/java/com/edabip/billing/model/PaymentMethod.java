package com.edabip.billing.model;

public record PaymentMethod(Long id, long customerId, String brand, String lastFour,
                            int expiryMonth, int expiryYear, boolean primary) {}
