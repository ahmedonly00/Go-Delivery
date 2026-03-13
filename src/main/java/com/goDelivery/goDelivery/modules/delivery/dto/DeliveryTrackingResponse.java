package com.goDelivery.goDelivery.modules.delivery.dto;

import com.goDelivery.goDelivery.shared.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingResponse {

    // Used by DeliveryTrackingService (builder pattern)
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

    // Used by BikerService (setter pattern)
    private String currentStatus;
    private boolean delivered;
    private String deliveryPersonId;
    private String deliveryPersonName;
    private String deliveryPersonPhone;
    private List<DeliveryStatusHistory> statusHistory;
    private LocalDateTime deliveredAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryStatusHistory {
        private String status;
        private LocalDateTime timestamp;
        private String actorId;
        private String description;
        private Double latitude;
        private Double longitude;
    }
}
