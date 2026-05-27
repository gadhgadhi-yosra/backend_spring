package com.elfaddoui.backend.order.dto;

import java.math.BigDecimal;

public record DeliverySettingsResponse(
        BigDecimal deliveryFee,
        String deliveryEtaLabel,
        String courierName,
        String courierPhone,
        String storePhone
) {
}
