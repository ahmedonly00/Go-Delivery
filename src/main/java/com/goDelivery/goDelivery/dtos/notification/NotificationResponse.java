package com.goDelivery.goDelivery.dtos.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String recipientId;
    private String title;
    private String message;
    private String type;
    private String relatedEntityId;
    private String relatedEntityType;
    private Map<String, String> data;
    private boolean isRead;
    private boolean isImportant;
    private boolean requiresAction;
    private String actionText;
    private String actionUrl;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String status; // SENT, DELIVERED, FAILED
    private String imageUrl;
    private String icon;
}
