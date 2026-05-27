package com.elfaddoui.backend.home.mapper;

import com.elfaddoui.backend.category.service.CategoryCatalog;
import com.elfaddoui.backend.home.dto.ProductDto;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.upload.service.PublicImageUrlResolver;
import org.springframework.stereotype.Component;

@Component
public class HomeProductMapper {

    private final CategoryCatalog categoryCatalog;
    private final PublicImageUrlResolver publicImageUrlResolver;

    public HomeProductMapper(CategoryCatalog categoryCatalog, PublicImageUrlResolver publicImageUrlResolver) {
        this.categoryCatalog = categoryCatalog;
        this.publicImageUrlResolver = publicImageUrlResolver;
    }

    public ProductDto toDto(Product product) {
        String categoryDisplayName = product.getCategory().getDisplayName();
        return new ProductDto(
                String.valueOf(product.getId()),
                product.getName(),
                product.getDescription(),
                publicImageUrlResolver.resolve(product.getImageUrl()),
                product.getPrice(),
                product.getOldPrice(),
                product.getDiscountPct(),
                product.getPromoLabel(),
                product.getPromoStartsAt(),
                product.getPromoEndsAt(),
                product.getRating(),
                estimateReviews(product),
                (categoryDisplayName == null || categoryDisplayName.isBlank())
                        ? categoryCatalog.displayNameFor(product.getCategory().getName())
                        : categoryDisplayName
        );
    }

    private int estimateReviews(Product product) {
        int fromSales = product.getSalesCount() == null ? 0 : Math.toIntExact(product.getSalesCount());
        int fromRating = product.getRating() == null ? 0 : (int) Math.round(product.getRating() * 30);
        return Math.max(fromSales, fromRating);
    }
}
