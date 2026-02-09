package com.goDelivery.goDelivery.dtos.delivery;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryFeeCalculationResponse {
    private Float deliveryFee;
    private Double distanceKm;
    private String distanceDisplay; // e.g., "2.5 km" or "500 m"
    private Boolean withinRadius;
    private String message; // e.g., "Outside delivery area"
}
