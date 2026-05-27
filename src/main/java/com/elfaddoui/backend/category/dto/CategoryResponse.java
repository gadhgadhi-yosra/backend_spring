package com.elfaddoui.backend.category.dto;

import java.math.BigDecimal;
import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String key,
        String displayName,
        String imageUrl,
        Integer sortOrder,
        boolean isActive,
        boolean isPromo,
        boolean isBio,
        boolean isNew,
        boolean isPopular,
        List<String> customTags,
        int productCount,
        int activeProductCount,
        int inactiveProductCount,
        int promoProductCount,
        int bioProductCount,
        int newProductCount,
        int popularProductCount,
        int totalStockQty,
        Integer maxDiscountPct,
        Double averageRating,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<String> tags
) {
}
