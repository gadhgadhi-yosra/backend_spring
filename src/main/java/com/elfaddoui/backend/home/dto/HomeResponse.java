package com.elfaddoui.backend.home.dto;

import java.time.Instant;
import java.util.List;

public record HomeResponse(
        String locationLabel,
        String etaLabel,
        List<ProductDto> deals,
        List<ProductDto> forYou,
        List<ProductDto> recent,
        List<String> deliveryAreas,
        List<HomeCatalogueDto> catalogues,
        List<HomePromotionDto> currentPromotions
) {
    public record HomeCatalogueDto(
            Long id,
            String key,
            String title,
            String imageUrl,
            int sortOrder
    ) {
    }

    public record HomePromotionDto(
            String id,
            String title,
            String subtitle,
            String imageUrl,
            Integer discountPct,
            Instant startsAt,
            Instant endsAt,
            Long productId,
            String productName
    ) {
    }
}
