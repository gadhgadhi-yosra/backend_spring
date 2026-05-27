package com.elfaddoui.backend.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaymentIntentRequest(
        @NotNull(message = "must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "must be greater than 0")
        BigDecimal amount,
        @Size(max = 8, message = "size must be at most 8")
        String currency,
        Object orderPreview,
        String providerHint
) {
}
