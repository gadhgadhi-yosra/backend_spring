package com.elfaddoui.backend.category.dto;

import java.util.List;

public record PublicCategoryResponse(
        Long id,
        String key,
        String name,
        boolean isActive,
        boolean isPromo,
        boolean isBio,
        boolean isNew,
        boolean isPopular,
        List<String> customTags,
        int productCount,
        int promoCount,
        String imageUrl,
        int reviews,
        List<String> tags,
        List<PublicFilterOptionResponse> tabs,
        List<PublicFilterOptionResponse> chips,
        int sortOrder
) {
}
