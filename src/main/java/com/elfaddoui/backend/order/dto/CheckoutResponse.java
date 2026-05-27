package com.elfaddoui.backend.order.dto;

import java.math.BigDecimal;

public record CheckoutResponse(
        String orderId,
        String status,
        BigDecimal total
) {
}
