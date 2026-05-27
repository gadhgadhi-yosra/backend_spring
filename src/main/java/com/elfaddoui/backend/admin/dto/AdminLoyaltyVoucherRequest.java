package com.elfaddoui.backend.admin.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AdminLoyaltyVoucherRequest(
        @NotBlank String title,
        @NotBlank String code,
        String description,
        @NotNull @Future Instant expiresAt
) {
}
