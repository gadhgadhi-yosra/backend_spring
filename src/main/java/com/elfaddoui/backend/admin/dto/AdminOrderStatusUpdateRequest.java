package com.elfaddoui.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminOrderStatusUpdateRequest(
        @NotBlank(message = "must not be blank") String status
) {
}
