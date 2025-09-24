package com.abonex.abonexbackend.dto.subs.response;

import com.abonex.abonexbackend.entity.Notification;
import com.abonex.abonexbackend.entity.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String body;
    private boolean isRead;
    private NotificationType type;
    private Long relatedSubscriptionId;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .isRead(notification.isRead())
                .type(notification.getType())
                .relatedSubscriptionId(notification.getRelatedSubscriptionId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}