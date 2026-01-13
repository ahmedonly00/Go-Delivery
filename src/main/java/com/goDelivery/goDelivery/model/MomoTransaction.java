package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.Enum.TransactionStatus;
import com.goDelivery.goDelivery.Enum.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "momo_transactions")
public class MomoTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String referenceId;
    
    @Column(unique = true, nullable = false, length = 100)
    private String externalId;
    
    @Column(nullable = false, length = 20)
    private String msisdn;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    
    @Column(name = "financial_transaction_id", length = 100)
    private String financialTransactionId;
    
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "RWF";
    
    @Column(name = "error_reason", length = 2000)
    private String errorReason;
    
    @Column(name = "callback_url", length = 500)
    private String callbackUrl;
    
    @Column(name = "payer_message", length = 500)
    private String payerMessage;
    
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMenthod paymentMethod;
    
    @Column(name = "is_refunded", columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isRefunded = false;
    
    @Column(name = "refund_reason", length = 500)
    private String refundReason;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "created_at", updatable = false, nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "api_response", columnDefinition = "TEXT")
    private String apiResponse;
    
    @Column(name = "status_code")
    private Integer statusCode;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // If status changed to a final state, set completedAt
        if (status != null && (status == TransactionStatus.SUCCESS || 
                              status == TransactionStatus.FAILED || 
                              status == TransactionStatus.CANCELLED) && 
            completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    // Helper methods for status management
    public void markAsSuccessful(String financialTransactionId) {
        this.status = TransactionStatus.SUCCESS;
        this.financialTransactionId = financialTransactionId;
        this.completedAt = LocalDateTime.now();
        if (this.payment != null) {
            this.payment.setPaymentStatus(PaymentStatus.PAID);
        }
    }
    
    public void markAsFailed(String errorReason) {
        this.status = TransactionStatus.FAILED;
        this.errorReason = errorReason;
        this.completedAt = LocalDateTime.now();
        if (this.payment != null) {
            this.payment.setPaymentStatus(PaymentStatus.FAILED);
        }
    }
    
    public void markAsRefunded(String reason) {
        this.isRefunded = true;
        this.refundReason = reason;
        this.refundedAt = LocalDateTime.now();
        if (this.payment != null) {
            this.payment.setPaymentStatus(PaymentStatus.REFUNDED);
        }
    }
    
    public boolean isFinalState() {
        return status == TransactionStatus.SUCCESS || 
               status == TransactionStatus.FAILED || 
               status == TransactionStatus.CANCELLED;
    }
    
    public boolean isSuccessful() {
        return status == TransactionStatus.SUCCESS;
    }
}
