package com.elfaddoui.backend.order.dto;

import java.math.BigDecimal;

public record DeliveryTrackingResponse(
        Long id,
        String orderCode,
        String orderId,
        String status,
        int step,
        int totalSteps,
        Integer etaMinutes,
        String deliveryAddress,
        String deliverySlotLabel,
        BigDecimal deliveryFee,
        String courierName,
        String courierPhone,
        String storePhone
) {
}
