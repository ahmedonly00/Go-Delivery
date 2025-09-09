package com.goDelivery.goDelivery.dtos.order;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Integer orderNumber;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private Float subTotal;
    private Float deliveryFee;
    private Float discountAmount;
    private Float totalAmount;
    private PaymentMenthod paymentMethod;
    private String specialInstructions;
    private LocalDate estimatedDelivery;
    private LocalDate orderPlacedAt;
    private LocalDate orderConfirmedAt;
    private LocalDate foodReadyAt;
    private LocalDate pickedUpAt;
    private LocalDate deliveredAt;
    private LocalDate cancelledAt;
    private String cancellationReason;
    private LocalDate createdAt;
    
    // Related entity IDs
    private Long customerId;
    private Long restaurantId;
    private Long branchId;
    private Long bikerId;
    private Long deliveryAddressId;
    private Long promotionId;
    private Long paymentId;
    private Long reviewId;
    
    // Basic related entity info (optional, can be loaded separately)
    private String customerName;
    private String restaurantName;
    private String bikerName;
    
    // Order items
    private List<OrderItemResponse> orderItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long orderItemId;
        private Long menuItemId;
        private String menuItemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String specialInstructions;
        private List<Long> variantIds;
    }
}
