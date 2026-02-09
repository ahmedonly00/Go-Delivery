package com.goDelivery.goDelivery.dtos.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryFeeCalculationRequest {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotNull(message = "Customer latitude is required")
    private Double customerLatitude;

    @NotNull(message = "Customer longitude is required")
    private Double customerLongitude;
}
