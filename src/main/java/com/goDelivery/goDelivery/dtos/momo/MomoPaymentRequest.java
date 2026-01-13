package com.goDelivery.goDelivery.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoPaymentRequest {
    @NotBlank(message = "External ID is required")
    private String externalId;
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @Pattern(regexp = "^250\\d{9}$", message = "Invalid phone number format. Must be 250XXXXXXXXX")
    private String msisdn;
    
    @Positive(message = "Amount must be a positive number")
    private Float amount;
    
    @NotBlank(message = "Payer message title is required")
    private String payerMessageTitle;
    
    @NotBlank(message = "Payer message description is required")
    private String payerMessageDescription;
    
    @URL(message = "Callback URL must be a valid URL")
    private String callback;
}
