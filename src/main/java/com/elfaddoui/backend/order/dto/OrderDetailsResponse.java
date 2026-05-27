package com.elfaddoui.backend.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDetailsResponse(
        String orderReference,
        String status,
        BigDecimal total,
        Instant createdAt,
        String paymentMethod,
        String deliveryAddress,
        String deliverySlotLabel,
        BigDecimal deliveryFee,
        String courierName,
        String courierPhone,
        String storePhone,
        List<OrderDetailsItemResponse> items
) {
}
