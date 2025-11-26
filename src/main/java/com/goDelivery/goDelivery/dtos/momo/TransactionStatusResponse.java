package com.goDelivery.goDelivery.dtos.momo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatusResponse {
    private String referenceId;
    private String status;
    private Float amount;
    private String currency;
    private String financialTransactionId;
    private String externalId;
    private String reason;
    private String errorReason;
    
}
