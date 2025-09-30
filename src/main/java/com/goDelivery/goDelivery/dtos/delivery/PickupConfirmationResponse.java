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
public class PickupConfirmationResponse {
    
    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private Long bikerId;
    private String bikerName;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private Double orderAmount;
    private LocalDateTime pickedUpAt;
    private Integer estimatedDeliveryMinutes;
    private String message;
    private String navigationUrl;
}
