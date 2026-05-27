package com.elfaddoui.backend.category.service.impl;

import java.math.BigDecimal;
import java.util.List;

record CategoryAdminStats(
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
    static CategoryAdminStats empty() {
        return new CategoryAdminStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, null, null, List.of());
    }
}
