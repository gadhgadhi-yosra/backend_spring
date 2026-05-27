package com.elfaddoui.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminHomeSettingsRequest(
        @NotBlank(message = "must not be blank") String locationLabel,
        @NotBlank(message = "must not be blank") String etaLabel,
        String deliveryAreas
) {
}
