package com.elfaddoui.backend.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String name,
        String imageUrl,
        BigDecimal price,
        Integer stockQty,
        Integer qty,
        BigDecimal lineTotal
) {
}
