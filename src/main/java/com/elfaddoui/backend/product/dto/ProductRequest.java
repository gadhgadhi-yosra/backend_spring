package com.elfaddoui.backend.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "must not be blank") String name,
        @NotBlank(message = "must not be blank") String description,
        @NotNull(message = "must not be null") @Positive(message = "must be positive") BigDecimal price,
        @Positive(message = "must be positive") BigDecimal oldPrice,
        @NotNull(message = "must not be null") @Min(value = 0, message = "must be at least 0") @Max(value = 100, message = "must be at most 100") Integer discountPct,
        @NotNull(message = "must not be null") Long categoryId,
        @NotBlank(message = "must not be blank") String imageUrl,
        @NotNull(message = "must not be null") @Min(value = 0, message = "must be at least 0") Integer stockQty,
        boolean isActive,
        boolean isPromo,
        boolean isBio,
        boolean isNew,
        boolean isPopular,
        List<String> customTags,
        String promoLabel,
        Instant promoStartsAt,
        Instant promoEndsAt,
        @DecimalMin(value = "0.0", message = "must be at least 0.0") @DecimalMax(value = "5.0", message = "must be at most 5.0") Double rating,
        @Min(value = 0, message = "must be at least 0") Long salesCount
) {
}
