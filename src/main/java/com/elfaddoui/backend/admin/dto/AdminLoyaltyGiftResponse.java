package com.elfaddoui.backend.admin.dto;

public record AdminLoyaltyGiftResponse(
        Long id,
        String title,
        int points,
        boolean active,
        int sortOrder
) {
}

