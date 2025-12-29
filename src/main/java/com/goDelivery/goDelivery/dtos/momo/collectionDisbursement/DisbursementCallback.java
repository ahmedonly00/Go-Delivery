package com.goDelivery.goDelivery.dtos.momo.collectionDisbursement;

 
import java.time.LocalDateTime;

import com.goDelivery.goDelivery.Enum.DisburseType;
import com.goDelivery.goDelivery.Enum.DisbursementStatus;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementCallback {
    private DisburseType type; // "COLLECTION" or "DISBURSEMENT"
    private String referenceId;
    private DisbursementStatus status; // "SUCCESSFUL", "FAILED", "PENDING"
    private String financialTransactionId;
    private String externalId;
    private String errorReason;
    private Double amount;
    private String currency;
    private LocalDateTime timestamp;
}
