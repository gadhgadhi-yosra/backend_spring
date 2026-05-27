package com.elfaddoui.backend.loyalty.dto;

import java.time.Instant;

public record LoyaltyHistoryResponse(
        String title,
        Instant createdAt,
        int points
) {
}
