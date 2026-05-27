package com.elfaddoui.backend.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminOrderSummaryResponse(
        Long id,
        String reference,
        String status,
        BigDecimal total,
        String customerName,
        String customerPhone,
        String paymentMethod,
        String deliverySlot,
        String courierName,
        Instant createdAt
) {
}
