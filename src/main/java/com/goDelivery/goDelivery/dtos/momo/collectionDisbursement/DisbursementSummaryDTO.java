package com.goDelivery.goDelivery.dtos.momo.collectionDisbursement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementSummaryDTO {
    private Long transactionId;
    private String referenceId;
    private Long orderId;
    private String orderNumber;
    private Long restaurantId;
    private String restaurantName;
    private BigDecimal amount;
    private BigDecimal commission;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Explicit constructor for JPQL queries
    public DisbursementSummaryDTO(Long transactionId, String referenceId, Long orderId, 
                                  String orderNumber, Long restaurantId, String restaurantName,
                                  BigDecimal amount, BigDecimal commission, String status,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.amount = amount;
        this.commission = commission;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
