package com.goDelivery.goDelivery.modules.payment.model;

import com.goDelivery.goDelivery.shared.enums.PaymentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;

import com.goDelivery.goDelivery.modules.ordering.model.Order;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mpesa_transactions")
public class MpesaTransaction {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    private String msisdn;

    private Float amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String thirdPartyRef;

    private String description;

    private String apiResponse;

    private String callbackPayload;

    @JoinColumn(name = "order_id")
    private Order order;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    public MpesaTransaction() {
        this.status = PaymentStatus.PENDING;
    }

    public void markAsCompleted() {
        this.status = PaymentStatus.PAID;
        this.completedAt = LocalDateTime.now();
    }

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

    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
