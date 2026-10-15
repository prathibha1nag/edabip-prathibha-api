package com.edabip.billing.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatePaymentMethodRequest(
        @NotBlank @Pattern(regexp = "(?i)Visa|Mastercard|American Express|Discover", message = "brand must be Visa, Mastercard, American Express or Discover") String brand,
        @NotBlank @Pattern(regexp = "\\d{4}", message = "lastFour must contain exactly four digits") String lastFour,
        @Min(1) @Max(12) int expiryMonth,
        @Min(2000) int expiryYear,
        boolean makePrimary) {}
