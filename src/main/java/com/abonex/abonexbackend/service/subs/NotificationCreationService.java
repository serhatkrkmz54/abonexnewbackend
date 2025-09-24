package com.abonex.abonexbackend.service.subs;

import com.abonex.abonexbackend.entity.Notification;
import com.abonex.abonexbackend.entity.User;
import com.abonex.abonexbackend.entity.enums.NotificationType;
import com.abonex.abonexbackend.repository.NotificationRepository;
import com.abonex.abonexbackend.service.fcm.FCMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationCreationService {
    private final NotificationRepository notificationRepository;
    private final FCMService fcmService;

    @Transactional
    public void createAndSendNotification(User user, String title, String body, NotificationType type, Long relatedSubscriptionId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .type(type)
                .relatedSubscriptionId(relatedSubscriptionId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        if (user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
            Map<String, String> data = Map.of(
                    "notificationType", type.name(),
                    "subscriptionId", relatedSubscriptionId != null ? relatedSubscriptionId.toString() : ""
            );
            fcmService.sendNotificationWithData(user.getFcmToken(), title, body, data);
        }
    }
}