package com.elfaddoui.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminLoyaltyPointAdjustmentRequest(
        @NotBlank String title,
        @NotNull Integer points
) {
}
