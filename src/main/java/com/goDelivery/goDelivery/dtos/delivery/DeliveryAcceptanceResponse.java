package com.goDelivery.goDelivery.dtos.delivery;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAcceptanceResponse {
    
    private Long orderId;
    private String orderNumber;
    private Long bikerId;
    private String bikerName;
    private OrderStatus orderStatus;
    private String restaurantName;
    private String pickupAddress;
    private String deliveryAddress;
    private String customerName;
    private String customerPhone;
    private Double orderAmount;
    private LocalDateTime acceptedAt;
    private Integer estimatedDeliveryMinutes;
    private String message;
}
