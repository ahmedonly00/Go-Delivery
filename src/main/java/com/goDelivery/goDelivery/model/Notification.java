package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.NotificationType;
import com.goDelivery.goDelivery.Enum.RecipientType;

import java.time.LocalDate;

public class Notification {
    private Long notificationId;
    private RecipientType recipientType;
    private Long recipientId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private String data;
    private boolean isRead;
    private LocalDate sentAt;
    private LocalDate readAt;

}
