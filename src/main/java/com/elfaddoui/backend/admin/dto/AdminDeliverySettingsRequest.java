package com.elfaddoui.backend.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record AdminDeliverySettingsRequest(
        @NotBlank(message = "must not be blank") String courierName,
        @NotBlank(message = "must not be blank")
        @Pattern(
                regexp = "^\\+216 \\d{2} \\d{3} \\d{3}$",
                message = "phone must match +216 XX XXX XXX"
        )
        String courierPhone,
        @NotBlank(message = "must not be blank")
        @Pattern(
                regexp = "^\\+216 \\d{2} \\d{3} \\d{3}$",
                message = "phone must match +216 XX XXX XXX"
        )
        String storePhone,
        @DecimalMin(value = "0.00", message = "must be at least 0")
        BigDecimal deliveryFee,
        @NotBlank(message = "must not be blank") String etaLabel
) {
}
