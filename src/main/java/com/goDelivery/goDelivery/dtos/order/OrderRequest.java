package com.goDelivery.goDelivery.dtos.order;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
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
public class OrderRequest {

    @NotNull(message = "Customer ID is required")
    @JsonAlias("customer_id")
    private Long customerId;

    @NotNull(message = "Delivery address ID is required")
    @JsonAlias("delivery_address_id")
    private Long deliveryAddressId;

    @JsonAlias("promotion_id")
    private Long promotionId;

    // Optional: Will default to PENDING if not provided
    @JsonAlias("order_status")
    private OrderStatus orderStatus;

    @NotNull(message = "Delivery address is required")
    @JsonAlias("delivery_address")
    private String deliveryAddress;

    // Optional: Will default to UNPAID if not provided
    @JsonAlias("payment_status")
    private PaymentStatus paymentStatus;

    @NotNull(message = "Subtotal is required")
    @PositiveOrZero(message = "Subtotal must be zero or positive")
    @JsonAlias("sub_total")
    private Float subTotal;

    @NotNull(message = "Delivery fee is required")
    @PositiveOrZero(message = "Delivery fee must be zero or positive")
    @JsonAlias("delivery_fee")
    private Float deliveryFee;

    @NotNull(message = "Discount amount is required")
    @PositiveOrZero(message = "Discount amount must be zero or positive")
    @JsonAlias("discount_amount")
    private Float discountAmount;

    @NotNull(message = "Final amount is required")
    @PositiveOrZero(message = "Final amount must be positive")
    @JsonAlias("final_amount")
    private Float finalAmount;

    @NotNull(message = "Payment method is required")
    @JsonAlias("payment_method")
    private PaymentMenthod paymentMethod;

    @JsonAlias("special_instructions")
    private String specialInstructions;

    @JsonAlias("cancellation_reason")
    private String cancellationReason;

    @JsonAlias("order_placed_at")
    private LocalDateTime orderPlacedAt;

    @NotNull(message = "Restaurant orders are required")
    @NotEmpty(message = "At least one restaurant order is required")
    @JsonAlias("restaurant_orders")
    private List<RestaurantOrderRequest> restaurantOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantOrderRequest {
        @NotNull(message = "Restaurant ID is required")
        @JsonAlias("restaurant_id")
        private Long restaurantId;

        // Optional: Only required if restaurant has branches
        @JsonAlias("branch_id")
        private Long branchId;

        @NotEmpty(message = "At least one order item is required")
        @JsonAlias("order_items")
        private List<OrderItemRequest> orderItems;

        // Optional: Delivery fee specific to this restaurant order
        // If not provided, will use restaurant's default delivery fee
        @JsonAlias("delivery_fee")
        private Float deliveryFee;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Menu item ID is required")
        @JsonAlias("menu_item_id")
        private Long menuItemId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @JsonAlias("special_instructions")
        private String specialInstructions;

        @JsonAlias("variant_ids")
        private List<Long> variantIds;
    }
}
