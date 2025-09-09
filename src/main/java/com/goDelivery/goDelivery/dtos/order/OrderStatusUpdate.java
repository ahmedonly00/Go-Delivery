package com.goDelivery.goDelivery.dtos.order;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdate {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Status is required")
    private OrderStatus status;
}
