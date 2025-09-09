package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.NotificationType;
import com.goDelivery.goDelivery.Enum.RecipientType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;
    
    @Column(name = "recipient_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecipientType recipientType;
    
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", nullable = false)
    private String message;
    
    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    
    @Column(name = "data")
    private String data;
    
    @Column(name = "is_read", nullable = false)
    private boolean isRead;
    
    @Column(name = "sent_at", nullable = false)
    private LocalDate sentAt;
    
    @Column(name = "read_at")
    private LocalDate readAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

}
