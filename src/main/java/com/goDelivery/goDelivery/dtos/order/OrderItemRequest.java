package com.goDelivery.goDelivery.dtos.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private Float unitPrice;
    
    private String specialInstructions;
    
    // Simple list of variant IDs instead of nested DTOs
    private List<Long> variantIds;
    
    // Optional: Additional price for all variants combined
    private Float totalAdditionalPrice;
}
