package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DeliverySettingsRequest {
    @NotNull(message = "Delivery type is required")
    private DeliveryType deliveryType;
    
    @Positive(message = "Delivery fee must be a positive number")
    private Float deliveryFee;  // Only required if deliveryType is SELF_DELIVERY
    
    @Positive(message = "Delivery radius must be a positive number")
    private Double deliveryRadius;  // In kilometers, only required if deliveryType is SELF_DELIVERY
}
