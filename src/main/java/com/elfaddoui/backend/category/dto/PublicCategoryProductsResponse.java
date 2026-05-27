package com.elfaddoui.backend.category.dto;

import com.elfaddoui.backend.product.dto.ProductResponse;

import java.util.List;

public record PublicCategoryProductsResponse(
        Long id,
        String key,
        String name,
        int productCount,
        int promoCount,
        String imageUrl,
        int reviews,
        List<String> tags,
        List<PublicFilterOptionResponse> tabs,
        List<PublicFilterOptionResponse> chips,
        List<String> subCategories,
        List<ProductResponse> products
) {
}
