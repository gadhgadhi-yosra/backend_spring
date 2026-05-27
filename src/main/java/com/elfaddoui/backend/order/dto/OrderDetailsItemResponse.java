package com.elfaddoui.backend.order.dto;

import java.math.BigDecimal;

public record OrderDetailsItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
