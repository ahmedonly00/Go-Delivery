package com.goDelivery.goDelivery.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

import com.goDelivery.goDelivery.Enum.TransactionStatus;
import com.goDelivery.goDelivery.Enum.TransactionType;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
        
    @Column(unique = true, nullable = false)
    private String referenceId;
        
    @Column(unique = true, nullable = false)
    private String externalId;
        
    @Column(nullable = false)
    private String msisdn;
        
    @Column(nullable = false)
    private Double amount;
        
    @Column(nullable = false)
    private String currency;
        
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
        
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
        
    private String financialTransactionId;
        
    private String callback;
        
    @Column(length = 1000)
    private String errorReason;
        
    @Column(nullable = false)
    private LocalDateTime createdAt;
        
    private LocalDateTime updatedAt;
        
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

