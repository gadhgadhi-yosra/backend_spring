package com.elfaddoui.backend.notification.controller;

import com.elfaddoui.backend.notification.dto.DeviceTokenRequest;
import com.elfaddoui.backend.notification.dto.InAppNotificationResponse;
import com.elfaddoui.backend.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<InAppNotificationResponse> list(Authentication auth) {
        return notificationService.getUserNotifications(auth.getName());
    }

    @PatchMapping("/mark-all-read")
    public Map<String, Object> markAllRead(Authentication auth) {
        int updated = notificationService.markAllRead(auth.getName());
        return Map.of("updated", updated);
    }

    @PostMapping(value = "/device-token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> saveDeviceToken(Authentication auth, @Valid @RequestBody DeviceTokenRequest request) {
        notificationService.saveDeviceToken(auth.getName(), request.token(), request.platform());
        return Map.of("status", "ok");
    }
}
