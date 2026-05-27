package com.elfaddoui.backend.admin.dto;

import java.math.BigDecimal;

public record AdminOrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
