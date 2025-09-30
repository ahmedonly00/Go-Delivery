package com.goDelivery.goDelivery.dtos.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInteractionDetails {
    
    private Long orderId;
    private String orderNumber;
    
    // Customer Information
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    
    // Delivery Information
    private String deliveryAddress;
    private String apartmentUnit;
    private String buildingNumber;
    private String specialInstructions;
    
    // Preferences
    private Boolean contactlessDelivery;
    private String preferredContactMethod; // CALL, TEXT, DOORBELL
    private Boolean leaveAtDoor;
    private String gateCode;
    
    // Order Details
    private List<OrderItem> items;
    private Double totalAmount;
    private String paymentMethod;
    private Boolean paymentCompleted;
    
    // Communication Templates
    private String arrivalMessage;
    private String completionMessage;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem {
        private String itemName;
        private Integer quantity;
        private String specialRequests;
    }
}
