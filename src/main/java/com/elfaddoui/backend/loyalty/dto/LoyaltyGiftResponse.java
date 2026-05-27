package com.elfaddoui.backend.loyalty.dto;

public record LoyaltyGiftResponse(
        String title,
        int points,
        boolean unlocked
) {
}
