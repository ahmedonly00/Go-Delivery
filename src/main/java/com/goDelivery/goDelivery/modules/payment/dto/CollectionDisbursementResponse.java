package com.goDelivery.goDelivery.modules.payment.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDisbursementResponse {
    private String referenceId;
    private String status;
    private String message;
    private Double amount;
    private String currency;
    private String externalId;
    
}
