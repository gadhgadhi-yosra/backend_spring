package com.elfaddoui.backend.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderHistoryItemResponse(
        Long id,
        String orderReference,
        String status,
        BigDecimal total,
        Instant createdAt,
        int itemsCount
) {
}
