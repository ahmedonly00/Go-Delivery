package com.goDelivery.goDelivery.dtos.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryTrackingRequest {
    @NotNull(message = "Order ID is required")
    private String orderId;
    
    @NotNull(message = "Delivery status is required")
    private String status; // PICKED_UP, IN_TRANSIT, DELIVERED, FAILED, etc.
    
    private Double latitude;
    private Double longitude;
    private String locationName;
    private String notes;
    private String deliveryPersonId;
    private String deliveryPersonName;
}
