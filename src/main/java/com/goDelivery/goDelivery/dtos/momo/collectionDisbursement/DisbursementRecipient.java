package com.goDelivery.goDelivery.dtos.momo.collectionDisbursement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementRecipient {

    @NotBlank
    private String externalId;
    
    @NotBlank
    private String msisdn;
    
    @Positive
    private Double amount;
    
    private String payerMessageTitle;
    private String payerMessageDescription;
    
}
