package com.goDelivery.goDelivery.dtos.order;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import jakarta.validation.constraints.*;
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
public class OrderRequest {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    @NotNull(message = "Branch ID is required")
    private Long branchId;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;
    
    private Long promotionId;
    
    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;
    
    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
    
    @NotNull(message = "Subtotal is required")
    @PositiveOrZero(message = "Subtotal must be zero or positive")
    private Float subTotal;
    
    @NotNull(message = "Delivery fee is required")
    @PositiveOrZero(message = "Delivery fee must be zero or positive")
    private Float deliveryFee;
    
    @NotNull(message = "Discount amount is required")
    @PositiveOrZero(message = "Discount amount must be zero or positive")
    private Float discountAmount;
    
    @NotNull(message = "Final amount is required")
    @PositiveOrZero(message = "Final amount must be positive")
    private Float finalAmount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMenthod paymentMethod;
    
    private String specialInstructions;
    
    @NotNull(message = "Estimated delivery date is required")
    @FutureOrPresent(message = "Estimated delivery date must be in the present or future")
    private LocalDate estimatedDelivery;
    
    private String cancellationReason;

    private String orderNumber;

    private LocalDate orderPlacedAt;
    
    @NotEmpty(message = "At least one order item is required")
    private List<OrderItemRequest> orderItems;
    
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        private String specialInstructions;
        
        private List<Long> variantIds;
    }
}
