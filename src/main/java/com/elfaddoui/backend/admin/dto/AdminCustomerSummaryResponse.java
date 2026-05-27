package com.elfaddoui.backend.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminCustomerSummaryResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        boolean enabled,
        long ordersCount,
        BigDecimal totalSpent,
        Instant lastOrderAt
) {
}
