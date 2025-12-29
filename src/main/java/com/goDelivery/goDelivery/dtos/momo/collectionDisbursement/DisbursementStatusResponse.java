package com.goDelivery.goDelivery.dtos.momo.collectionDisbursement;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementStatusResponse {

    private String referenceId;
    private String status;
    private Double amount;
    private String currency;
    private String financialTransactionId;
    private String externalId;
    private String reason;
    private String errorReason;
    
}
