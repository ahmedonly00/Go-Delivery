package com.goDelivery.goDelivery.dtos.order;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private String deliveryAddress;
    private String contactNumber;
    private String specialInstructions;
    private Float subTotal;
    private Float deliveryFee;
    private Float discountAmount;
    private Float finalAmount;
    private PaymentMenthod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDate orderPlacedAt;
    private LocalDate orderConfirmedAt;
    private LocalDate foodReadyAt;
    private LocalDate pickedUpAt;
    private LocalDate deliveredAt;
    private LocalDate cancelledAt;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private LocalDate estimatedDeliveryTime;
    private String cancellationReason;
    
    // Related entity IDs
    private Long customerId;
    private Long restaurantId;
    private Long bikerId;
    
    // Order items
    private List<OrderItemResponse> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long itemId;
        private Long menuItemId;
        private String itemName;
        private Integer quantity;
        private Float unitPrice;
        private Float totalPrice;
        private String specialInstructions;
        private List<Long> variantIds;
    }
}
