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
public class DeliveryAcceptanceRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Biker ID is required")
    private Long bikerId;
    
    private String message;
    
    private Integer estimatedDeliveryMinutes;
}
