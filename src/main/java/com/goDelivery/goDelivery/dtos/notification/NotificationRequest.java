package com.goDelivery.goDelivery.dtos.notification;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class NotificationRequest {
    @NotBlank(message = "Recipient ID is required")
    private String recipientId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    @NotBlank(message = "Notification type is required")
    private String type; // ORDER_STATUS, PROMOTION, PAYMENT, SYSTEM, etc.
    
    private String relatedEntityId; // Order ID, Promotion ID, etc.
    private String relatedEntityType; // ORDER, PROMOTION, PAYMENT, etc.
    private Map<String, String> data; // Additional data for the notification
    private boolean isImportant = false;
    private boolean requiresAction = false;
    private String actionText;
    private String actionUrl;
}
