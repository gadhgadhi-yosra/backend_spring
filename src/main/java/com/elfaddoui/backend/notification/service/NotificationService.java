package com.elfaddoui.backend.notification.service;

import com.elfaddoui.backend.notification.dto.InAppNotificationResponse;
import com.elfaddoui.backend.order.entity.Order;

import java.util.List;

public interface NotificationService {
    List<InAppNotificationResponse> getUserNotifications(String userEmail);
    int markAllRead(String userEmail);
    void saveDeviceToken(String userEmail, String token, String platform);
    void notifyOrderCreated(Order order);
    void notifyOrderStatusChanged(Order order);
}
