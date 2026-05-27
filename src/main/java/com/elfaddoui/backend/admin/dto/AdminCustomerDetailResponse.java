package com.elfaddoui.backend.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record AdminCustomerDetailResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        String address,
        boolean enabled,
        Set<String> roles,
        long ordersCount,
        BigDecimal totalSpent,
        Instant lastOrderAt
) {
}
