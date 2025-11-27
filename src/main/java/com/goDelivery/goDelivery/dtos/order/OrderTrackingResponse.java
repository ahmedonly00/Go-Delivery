package com.goDelivery.goDelivery.dtos.order;

import com.goDelivery.goDelivery.Enum.OrderStatus;
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
public class OrderTrackingResponse {
    private Long orderId;
    private String orderNumber;
    private OrderStatus currentStatus;
    private LocalDateTime lastUpdated;
    private List<StatusUpdate> statusHistory;
    private String deliveryPersonName;
    private String deliveryPersonContact;
    private Double deliveryPersonRating;
    private Double distanceRemaining; // in kilometers
    private Integer estimatedMinutesRemaining;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdate {
        private OrderStatus status;
        private LocalDateTime timestamp;
        private String message;
    }
}
