package com.elfaddoui.backend.admin.dto;

public record AdminLoyaltyCustomerSummaryResponse(
        Long customerId,
        String customerName,
        String customerEmail,
        String cardNumber,
        int pointsBalance
) {
}

