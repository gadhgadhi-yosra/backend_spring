package com.elfaddoui.backend.auth.dto;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
        @NotBlank String resetToken,
        @Size(min = 8, message = "must be at least 8 chars") String newPassword
) {}