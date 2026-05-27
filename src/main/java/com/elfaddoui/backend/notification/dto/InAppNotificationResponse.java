package com.elfaddoui.backend.notification.dto;

import java.time.Instant;

public record InAppNotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        String orderReference,
        boolean unread,
        Instant createdAt
) {
}
