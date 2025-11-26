package com.goDelivery.goDelivery.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoTransactionStatus {
    private String referenceId;
    private String status;
    private Float amount;
    private String currency;
    private String financialTransactionId;
    private String externalId;
    private String reason;
    private String errorReason;
    private LocalDateTime timestamp;
}
