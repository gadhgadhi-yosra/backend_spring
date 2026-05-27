package com.elfaddoui.backend.product.service.impl;

import com.elfaddoui.backend.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collection;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> activeOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> activeEquals(Boolean active) {
        return (root, query, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }

    public static Specification<Product> noop() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Product> nameOrDescriptionContains(String queryValue) {
        return (root, query, cb) -> {
            if (queryValue == null || queryValue.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + queryValue.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? cb.conjunction() : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasCategoryIds(Collection<Long> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Product> hasCategoryName(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isBlank()) {
                return cb.conjunction();
            }
            String raw = categoryName.trim().toLowerCase();
            String normalized = normalizeCategoryValue(categoryName);
            return cb.or(
                    cb.equal(cb.lower(root.get("category").get("name")), raw),
                    cb.equal(cb.lower(root.get("category").get("displayName")), raw),
                    cb.equal(cb.lower(root.get("category").get("key")), raw),
                    cb.equal(cb.lower(root.get("category").get("name")), normalized),
                    cb.equal(cb.lower(root.get("category").get("displayName")), normalized),
                    cb.equal(cb.lower(root.get("category").get("key")), normalized),
                    cb.equal(cb.lower(root.get("category").get("key")), normalized.replace(' ', '-'))
            );
        };
    }

    public static Specification<Product> hasCategoryKey(String categoryKey) {
        return (root, query, cb) -> {
            if (categoryKey == null || categoryKey.isBlank()) {
                return cb.conjunction();
            }
            String normalized = normalizeCategoryValue(categoryKey).replace(' ', '-');
            return cb.equal(cb.lower(root.get("category").get("key")), normalized);
        };
    }

    public static Specification<Product> promoOnly(Boolean promoOnly) {
        return (root, query, cb) ->
                Boolean.TRUE.equals(promoOnly) ? cb.isTrue(root.get("promo")) : cb.conjunction();
    }

    public static Specification<Product> minDiscountPct(Integer minDiscountPct) {
        return (root, query, cb) ->
                minDiscountPct == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("discountPct"), minDiscountPct);
    }

    public static Specification<Product> maxDiscountPct(Integer maxDiscountPct) {
        return (root, query, cb) ->
                maxDiscountPct == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("discountPct"), maxDiscountPct);
    }

    public static Specification<Product> bioOnly(Boolean bioOnly) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(bioOnly)) {
                return cb.conjunction();
            }
            return cb.isTrue(root.get("bio"));
        };
    }

    public static Specification<Product> popularOnly(Boolean popularOnly) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(popularOnly)) {
                return cb.conjunction();
            }
            return cb.isTrue(root.get("popular"));
        };
    }

    public static Specification<Product> isNew(Boolean isNew) {
        return (root, query, cb) ->
                isNew == null ? cb.conjunction() : cb.equal(root.get("isNew"), isNew);
    }

    public static Specification<Product> minPrice(BigDecimal minPrice) {
        return (root, query, cb) ->
                minPrice == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    private static String normalizeCategoryValue(String value) {
        return value.trim().toLowerCase()
                .replace('à', 'a')
                .replace('á', 'a')
                .replace('â', 'a')
                .replace('ä', 'a')
                .replace('ã', 'a')
                .replace('å', 'a')
                .replace('è', 'e')
                .replace('é', 'e')
                .replace('ê', 'e')
                .replace('ë', 'e')
                .replace('ì', 'i')
                .replace('í', 'i')
                .replace('î', 'i')
                .replace('ï', 'i')
                .replace('ò', 'o')
                .replace('ó', 'o')
                .replace('ô', 'o')
                .replace('ö', 'o')
                .replace('õ', 'o')
                .replace('ù', 'u')
                .replace('ú', 'u')
                .replace('û', 'u')
                .replace('ü', 'u')
                .replace('ç', 'c')
                .replace('ñ', 'n')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
