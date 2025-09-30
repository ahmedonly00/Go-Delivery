package com.goDelivery.goDelivery.dtos.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PickupConfirmationRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Biker ID is required")
    private Long bikerId;
    
    private String verificationCode;
    
    private String notes;
    
    private Boolean orderVerified;
}
