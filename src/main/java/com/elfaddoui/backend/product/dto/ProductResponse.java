package com.elfaddoui.backend.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal oldPrice,
        Integer discountPct,
        Long categoryId,
        String categoryKey,
        String categoryName,
        String displayCategoryName,
        String imageUrl,
        Integer stockQty,
        boolean isActive,
        boolean isPromo,
        boolean isBio,
        boolean isNew,
        boolean isPopular,
        List<String> customTags,
        String promoLabel,
        Instant promoStartsAt,
        Instant promoEndsAt,
        Double rating,
        Integer reviews,
        Integer score,
        Long salesCount,
        Instant createdAt,
        Instant updatedAt
) {
}
