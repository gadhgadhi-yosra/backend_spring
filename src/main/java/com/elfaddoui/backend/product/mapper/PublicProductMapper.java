package com.elfaddoui.backend.product.mapper;

import com.elfaddoui.backend.category.service.CategoryCatalog;
import com.elfaddoui.backend.product.dto.ProductResponse;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.upload.service.PublicImageUrlResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicProductMapper {

    private final CategoryCatalog categoryCatalog;
    private final PublicImageUrlResolver publicImageUrlResolver;

    public PublicProductMapper(CategoryCatalog categoryCatalog, PublicImageUrlResolver publicImageUrlResolver) {
        this.categoryCatalog = categoryCatalog;
        this.publicImageUrlResolver = publicImageUrlResolver;
    }

    public ProductResponse toResponse(Product product) {
        int reviews = estimateReviews(product);
        double rating = product.getRating() == null ? 0.0 : product.getRating();
        int discountPct = product.getDiscountPct() == null ? 0 : product.getDiscountPct();
        String categoryName = product.getCategory().getName();
        String categoryKey = product.getCategory().getKey();
        String displayCategoryName = product.getCategory().getDisplayName();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getOldPrice(),
                product.getDiscountPct(),
                product.getCategory().getId(),
                (categoryKey == null || categoryKey.isBlank()) ? categoryCatalog.keyFor(categoryName) : categoryCatalog.keyFor(categoryKey),
                categoryName,
                (displayCategoryName == null || displayCategoryName.isBlank())
                        ? categoryCatalog.displayNameFor(categoryName)
                        : displayCategoryName,
                publicImageUrlResolver.resolve(product.getImageUrl()),
                product.getStockQty(),
                product.isActive(),
                product.isPromo(),
                product.isBio(),
                product.isNew(),
                product.isPopular(),
                splitTags(product.getCustomTags()),
                product.getPromoLabel(),
                product.getPromoStartsAt(),
                product.getPromoEndsAt(),
                rating,
                reviews,
                Math.max(1, Math.min(100, (int) Math.round((rating * 20) + discountPct))),
                product.getSalesCount(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private int estimateReviews(Product product) {
        int fromSales = product.getSalesCount() == null ? 0 : Math.toIntExact(product.getSalesCount());
        int fromRating = product.getRating() == null ? 0 : (int) Math.round(product.getRating() * 30);
        return Math.max(fromSales, fromRating);
    }

    private List<String> splitTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();
    }
}
