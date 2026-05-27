package com.elfaddoui.backend.payment.dto;

public record PaymentIntentResponse(
        String checkoutUrl,
        String paymentIntentId
) {
}
