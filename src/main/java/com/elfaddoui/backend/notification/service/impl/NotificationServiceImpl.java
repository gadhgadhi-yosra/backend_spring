package com.elfaddoui.backend.notification.service.impl;

import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.notification.dto.InAppNotificationResponse;
import com.elfaddoui.backend.notification.entity.InAppNotification;
import com.elfaddoui.backend.notification.entity.UserDeviceToken;
import com.elfaddoui.backend.notification.repository.InAppNotificationRepository;
import com.elfaddoui.backend.notification.repository.UserDeviceTokenRepository;
import com.elfaddoui.backend.notification.service.NotificationService;
import com.elfaddoui.backend.order.entity.Order;
import com.elfaddoui.backend.order.entity.OrderStatus;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final InAppNotificationRepository inAppNotificationRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public NotificationServiceImpl(
            UserRepository userRepository,
            InAppNotificationRepository inAppNotificationRepository,
            UserDeviceTokenRepository userDeviceTokenRepository
    ) {
        this.userRepository = userRepository;
        this.inAppNotificationRepository = inAppNotificationRepository;
        this.userDeviceTokenRepository = userDeviceTokenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InAppNotificationResponse> getUserNotifications(String userEmail) {
        return inAppNotificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(notification -> new InAppNotificationResponse(
                        notification.getId(),
                        notification.getType(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getOrderReference(),
                        notification.isUnread(),
                        notification.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public int markAllRead(String userEmail) {
        return inAppNotificationRepository.markAllRead(userEmail);
    }

    @Override
    public void saveDeviceToken(String userEmail, String token, String platform) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String normalizedToken = requireValue(token, "token");
        String normalizedPlatform = requireValue(platform, "platform").toLowerCase(Locale.ROOT);

        UserDeviceToken deviceToken = userDeviceTokenRepository.findByToken(normalizedToken)
                .orElseGet(UserDeviceToken::new);
        deviceToken.setUser(user);
        deviceToken.setToken(normalizedToken);
        deviceToken.setPlatform(normalizedPlatform);
        userDeviceTokenRepository.save(deviceToken);
    }

    @Override
    public void notifyOrderCreated(Order order) {
        saveNotification(
                order,
                "order_confirmed",
                "Commande confirmee",
                "Votre commande " + order.getReference() + " a ete confirmee et passe en preparation."
        );
    }

    @Override
    public void notifyOrderStatusChanged(Order order) {
        NotificationContent content = switch (order.getStatus()) {
            case PENDING -> new NotificationContent(
                    "order_pending",
                    "Commande en attente",
                    "Votre commande " + order.getReference() + " est en attente de confirmation."
            );
            case CONFIRMED -> new NotificationContent(
                    "order_confirmed",
                    "Commande confirmee",
                    "Votre commande " + order.getReference() + " a ete confirmee."
            );
            case PREPARING -> new NotificationContent(
                    "order_preparing",
                    "Preparation en cours",
                    "Votre commande " + order.getReference() + " est en cours de preparation."
            );
            case SHIPPED -> new NotificationContent(
                    "order_shipped",
                    "Commande en route",
                    "Votre commande " + order.getReference() + " est en route."
            );
            case DELIVERED -> new NotificationContent(
                    "order_delivered",
                    "Commande livree",
                    "Votre commande " + order.getReference() + " a ete livree."
            );
            case CANCELLED -> new NotificationContent(
                    "order_cancelled",
                    "Commande annulee",
                    "Votre commande " + order.getReference() + " a ete annulee."
            );
        };

        saveNotification(order, content.type(), content.title(), content.message());
    }

    private void saveNotification(Order order, String type, String title, String message) {
        if (order == null || order.getUser() == null) {
            return;
        }

        InAppNotification notification = new InAppNotification();
        notification.setUser(order.getUser());
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setOrderReference(order.getReference());
        notification.setUnread(true);
        inAppNotificationRepository.save(notification);
    }

    private String requireValue(String value, String fieldName) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private record NotificationContent(String type, String title, String message) {
    }
}
