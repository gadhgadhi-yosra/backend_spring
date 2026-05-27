package com.elfaddoui.backend.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminLoyaltyGiftRequest(
        @NotBlank String title,
        @NotNull @Min(1) Integer points,
        Boolean active,
        Integer sortOrder
) {
}
