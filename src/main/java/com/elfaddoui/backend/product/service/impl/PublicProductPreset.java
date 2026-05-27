package com.elfaddoui.backend.product.service.impl;

import com.elfaddoui.backend.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public final class PublicProductPreset {

    public static final String ALL = "all";
    public static final String PROMO = "promo";
    public static final String RECOMMENDED = "recommended";
    public static final String TOP = "top";
    public static final String CLASSIC = "classic";
    public static final String NEW = "new";

    private PublicProductPreset() {
    }

    public static String normalize(String preset) {
        if (preset == null || preset.isBlank()) {
            return null;
        }
        return switch (preset.trim().toLowerCase()) {
            case "all", "tout" -> ALL;
            case "promo", "promos" -> PROMO;
            case "recommended", "recommande", "recommanded" -> RECOMMENDED;
            case "top", "tops" -> TOP;
            case "classic", "classics", "classique", "classiques" -> CLASSIC;
            case "new", "newest", "nouveau", "nouveaux" -> NEW;
            default -> throw new IllegalArgumentException("Unsupported preset value");
        };
    }

    public static Boolean resolvePromoOnly(String preset, Boolean promoOnly) {
        String normalized = normalize(preset);
        if (PROMO.equals(normalized)) {
            return true;
        }
        return promoOnly;
    }

    public static String resolveSort(String preset, String sort) {
        if (sort != null && !sort.isBlank()) {
            return sort;
        }
        String normalized = normalize(preset);
        if (RECOMMENDED.equals(normalized)) {
            return "recommended";
        }
        if (TOP.equals(normalized)) {
            return "top";
        }
        if (NEW.equals(normalized)) {
            return "newest";
        }
        return sort;
    }

    public static Specification<Product> resolveSpecification(String preset) {
        String normalized = normalize(preset);
        if (NEW.equals(normalized)) {
            return ProductSpecifications.isNew(true);
        }
        if (CLASSIC.equals(normalized)) {
            return ProductSpecifications.isNew(false);
        }
        return ProductSpecifications.noop();
    }
}
