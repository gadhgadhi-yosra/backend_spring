package com.elfaddoui.backend.loyalty.dto;

import java.time.Instant;

public record LoyaltyVoucherResponse(
        String title,
        String code,
        String description,
        Instant expiresAt
) {
}
