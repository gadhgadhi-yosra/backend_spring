package com.elfaddoui.backend.admin.dto;

import java.math.BigDecimal;

public record AdminDashboardSummaryResponse(
        long categoriesCount,
        long activeCategoriesCount,
        long productsCount,
        long activeProductsCount,
        long lowStockProductsCount,
        long outOfStockProductsCount,
        long clientsCount,
        long totalOrders,
        long pendingOrders,
        long confirmedOrders,
        long preparingOrders,
        long shippedOrders,
        long deliveredOrders,
        long cancelledOrders,
        BigDecimal totalRevenue,
        BigDecimal todayRevenue
) {
}
