package com.elfaddoui.backend.admin.dto;

import java.math.BigDecimal;

public record AdminDeliverySettingsResponse(
        String courierName,
        String courierPhone,
        String storePhone,
        BigDecimal deliveryFee,
        String etaLabel
) {
}
