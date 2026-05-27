package com.elfaddoui.backend.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(
        @NotBlank String token,
        @NotBlank String platform
) {
}
