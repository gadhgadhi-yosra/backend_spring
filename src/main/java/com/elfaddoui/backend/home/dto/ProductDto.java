package com.elfaddoui.backend.home.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductDto(
        String id,
        String name,
        String description,
        String image,
        BigDecimal price,
        BigDecimal oldPrice,
        Integer discountPct,
        String promoLabel,
        Instant promoStartsAt,
        Instant promoEndsAt,
        Double rating,
        Integer reviews,
        String category
) {
}
