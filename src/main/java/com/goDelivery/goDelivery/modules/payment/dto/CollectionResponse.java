package com.goDelivery.goDelivery.modules.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionResponse {
    private String referenceId;
    private String status;
    private String message;
    private Float amount;
    private String currency;
    private String externalId;
    
}
