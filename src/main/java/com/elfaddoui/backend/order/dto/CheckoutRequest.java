package com.elfaddoui.backend.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CheckoutRequest(
        @Valid @NotNull(message = "must not be null") Customer customer,
        @Valid @NotNull(message = "must not be null") Address address,
        @Valid @NotNull(message = "must not be null") Payment payment,
        @Valid @NotNull(message = "must not be null") Delivery delivery,
        BigDecimal total
) {
    public record Customer(
            @NotBlank(message = "must not be blank")
            @Size(min = 3, max = 120, message = "size must be between 3 and 120")
            String fullName,
            @NotBlank(message = "must not be blank")
            @Pattern(
                    regexp = "^\\+216 \\d{2} \\d{3} \\d{3}$",
                    message = "phone must match +216 XX XXX XXX"
            )
            String phone,
            @Email(message = "must be a well-formed email address")
            String email,
            @Size(max = 2000, message = "size must be at most 2000")
            String note
    ) {
    }

    public record Address(
            @NotBlank(message = "must not be blank") String city,
            @NotBlank(message = "must not be blank") String area,
            @NotBlank(message = "must not be blank") String street,
            String extra,
            String postalCode,
            String hint,
            @NotBlank(message = "must not be blank") String placeType
    ) {
    }

    public record Payment(
            @NotBlank(message = "must not be blank") String method
    ) {
    }

    public record Delivery(
            @NotBlank(message = "must not be blank") String slot,
            String scheduledTime
    ) {
    }
}
