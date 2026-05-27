package com.elfaddoui.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminOrderCourierUpdateRequest(
        @NotBlank(message = "must not be blank") String courierName,
        @NotBlank(message = "must not be blank")
        @Pattern(
                regexp = "^\\+216 \\d{2} \\d{3} \\d{3}$",
                message = "phone must match +216 XX XXX XXX"
        )
        String courierPhone
) {
}
