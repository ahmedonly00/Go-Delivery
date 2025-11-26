package com.goDelivery.goDelivery.dtos.momo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementRequest {
    @NotBlank(message = "External ID is required")
    private String externalId;
    
    @NotBlank(message = "MSISDN is required")
    @Pattern(regexp = "^250\\d{9}$", message = "Invalid Rwanda phone number format")
    private String msisdn;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Float amount;
    
    private String callback;
    
    @NotBlank(message = "Payer message title is required")
    private String payerMessageTitle;
    
    @NotBlank(message = "Payer message description is required")
    private String payerMessageDescription;
}
