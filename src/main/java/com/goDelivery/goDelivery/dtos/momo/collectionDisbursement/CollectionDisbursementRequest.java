package com.goDelivery.goDelivery.dtos.momo.collectionDisbursement;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDisbursementRequest {

    @NotBlank
    private String collectionExternalId;
    
    @NotBlank
    private String collectionMsisdn;
    
    @Positive
    private Double collectionAmount;
    
    private String collectionPayerMessageTitle;
    private String collectionPayerMessageDescription;
    private String callback;
    
    @NotEmpty
    private List<DisbursementRecipient> disbursementRecipients;
}

