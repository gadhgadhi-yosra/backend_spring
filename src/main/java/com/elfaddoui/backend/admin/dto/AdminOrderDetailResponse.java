package com.elfaddoui.backend.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminOrderDetailResponse(
        Long id,
        String reference,
        String status,
        String paymentMethod,
        String deliverySlot,
        String scheduledTime,
        String customerName,
        String customerPhone,
        String customerEmail,
        String note,
        String city,
        String area,
        String street,
        String extra,
        String postalCode,
        String addressHint,
        String placeType,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal total,
        String courierName,
        String courierPhone,
        String storePhone,
        Instant createdAt,
        Instant updatedAt,
        List<AdminOrderItemResponse> items
) {
}
