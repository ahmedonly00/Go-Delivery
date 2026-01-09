package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "menu_audit_log")
@EntityListeners(AuditingEntityListener.class)
public class MenuAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "entity_type", nullable = false)
    private String entityType; // "MENU_CATEGORY" or "MENU_ITEM"

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "action", nullable = false)
    private String action; // "CREATE", "UPDATE", "DELETE", "PRICE_CHANGE"

    @Column(name = "field_name")
    private String fieldName; // "price", "isAvailable", etc.

    @Column(name = "old_value", length = 1000)
    private String oldValue;

    @Column(name = "new_value", length = 1000)
    private String newValue;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_role", nullable = false)
    private String userRole;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "reason")
    private String reason; // Optional reason for change
}
