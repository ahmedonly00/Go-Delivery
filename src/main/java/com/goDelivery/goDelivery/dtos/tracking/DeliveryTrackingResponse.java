package com.goDelivery.goDelivery.dtos.tracking;

import com.goDelivery.goDelivery.Enum.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingResponse {

    private Long trackingId;
    private Long orderId;
    private DeliveryStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private String statusMessage;
    private LocalDateTime estimatedArrivalTime;
    private Double distanceToDestinationKm;
    private BikerInfo bikerInfo;
    private LocalDateTime lastUpdated;
}
