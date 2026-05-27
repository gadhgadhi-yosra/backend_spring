package com.elfaddoui.backend.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @Size(min = 8, message = "must be at least 8 chars") String password
) {}