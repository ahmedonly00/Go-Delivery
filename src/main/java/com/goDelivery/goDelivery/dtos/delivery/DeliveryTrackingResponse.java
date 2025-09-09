package com.goDelivery.goDelivery.dtos.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingResponse {
    private String trackingId;
    private String orderId;
    private String currentStatus;
    private LocalDateTime currentStatusTime;
    private String deliveryPersonId;
    private String deliveryPersonName;
    private String deliveryPersonPhone;
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentLocationName;
    private LocalDateTime estimatedDeliveryTime;
    private List<DeliveryStatusHistory> statusHistory;
    private boolean isDelivered;
    private LocalDateTime deliveredAt;
    private String deliveryProofImage;
    private String recipientName;
    private String recipientSignature;
    private String notes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryStatusHistory {
        private String status;
        private LocalDateTime timestamp;
        private String locationName;
        private String notes;
        private Double latitude;
        private Double longitude;
    }
}
