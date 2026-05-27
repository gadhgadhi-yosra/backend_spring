package com.elfaddoui.backend.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "must not be blank")
        String currentPassword,
        @NotBlank(message = "must not be blank")
        @Size(min = 8, message = "size must be at least 8")
        String newPassword
) {
}
