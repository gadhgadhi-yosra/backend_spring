package com.elfaddoui.backend.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "must not be blank")
        @Size(min = 3, max = 120, message = "size must be between 3 and 120")
        String fullName,
        @NotBlank(message = "must not be blank")
        @Email(message = "must be a well-formed email address")
        String email,
        @Pattern(
                regexp = "^$|^[+0-9()\\-\\s]{8,20}$",
                message = "phone must be a valid phone number"
        )
        String phone,
        @Size(max = 1200, message = "size must be at most 1200")
        String avatarUrl,
        @Size(max = 2000, message = "size must be at most 2000")
        String address
) {
}
