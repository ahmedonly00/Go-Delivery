package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mpesa_transactions")
public class MpesaTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = true)
    private String transactionId;
    
    @Column(nullable = false)
    private String msisdn;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Column(name = "third_party_ref")
    private String thirdPartyRef;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "api_response", columnDefinition = "TEXT")
    private String apiResponse;
    
    @Column(name = "callback_payload", columnDefinition = "TEXT")
    private String callbackPayload;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Default constructor
    public MpesaTransaction() {
        this.status = PaymentStatus.PENDING;
    }
    
    // Helper method to mark transaction as completed
    public void markAsCompleted() {
        this.status = PaymentStatus.PAID;
        this.completedAt = LocalDateTime.now();
    }
    
    // Helper method to mark transaction as failed
    public void markAsFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.description = failureReason;
        this.completedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
