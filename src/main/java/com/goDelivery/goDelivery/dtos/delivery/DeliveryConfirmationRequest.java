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
public class DeliveryConfirmationRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Biker ID is required")
    private Long bikerId;
    
    private String recipientName;
    
    private String recipientSignature; // Base64 encoded signature image
    
    private String deliveryProofImage; // Base64 encoded photo of delivery
    
    private String notes;
    
    private Boolean contactlessDelivery;
    
    private Double deliveryLatitude;
    
    private Double deliveryLongitude;
}
