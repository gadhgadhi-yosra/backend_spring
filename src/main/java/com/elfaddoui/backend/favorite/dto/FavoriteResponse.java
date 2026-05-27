package com.elfaddoui.backend.favorite.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FavoriteResponse(
        Long id,
        Long productId,
        String name,
        String description,
        BigDecimal price,
        BigDecimal oldPrice,
        Integer discountPct,
        String imageUrl,
        Double rating,
        Long salesCount,
        Long categoryId,
        String categoryName,
        Instant favoritedAt
) {
}
