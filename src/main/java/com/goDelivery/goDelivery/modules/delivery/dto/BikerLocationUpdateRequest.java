package com.goDelivery.goDelivery.modules.delivery.dto;

import com.goDelivery.goDelivery.shared.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BikerLocationUpdateRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Biker ID is required")
    private Long bikerId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private DeliveryStatus status;

    private String statusMessage;
}
