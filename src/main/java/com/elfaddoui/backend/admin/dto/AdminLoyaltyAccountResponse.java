package com.elfaddoui.backend.admin.dto;

public record AdminLoyaltyAccountResponse(
        Long customerId,
        String customerName,
        String customerEmail,
        String cardNumber,
        int pointsBalance
) {
}
