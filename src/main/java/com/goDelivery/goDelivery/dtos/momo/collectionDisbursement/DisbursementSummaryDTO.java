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
    
}
