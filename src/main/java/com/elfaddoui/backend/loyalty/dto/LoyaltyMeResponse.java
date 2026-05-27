package com.elfaddoui.backend.loyalty.dto;

public record LoyaltyMeResponse(
        int pointsBalance,
        String cardNumber,
        int earnedThisMonth,
        int usedThisMonth,
        int nextGiftPoints
) {
}
