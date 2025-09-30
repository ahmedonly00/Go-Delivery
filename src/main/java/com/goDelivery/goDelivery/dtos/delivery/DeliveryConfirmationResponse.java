package com.goDelivery.goDelivery.dtos.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryConfirmationResponse {
    
    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private Long bikerId;
    private String bikerName;
    private String customerName;
    private Double orderAmount;
    private LocalDateTime deliveredAt;
    private String recipientName;
    private Boolean contactlessDelivery;
    private String message;
    private DeliveryEarnings earnings;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliveryEarnings {
        private Double deliveryFee;
        private Double tip;
        private Double totalEarnings;
        private String paymentStatus;
    }
}
