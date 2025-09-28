package com.goDelivery.goDelivery.dtos.order;

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
public class OrderItemResponse {
    private Long orderItemId;
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private Float unitPrice;
    private Float totalPrice;
    private String specialRequests;
    private LocalDate createdAt;
    
    // Simple list of variant IDs instead of nested DTOs
    private List<Long> variantIds;
    
    // Optional: Basic variant info if needed
    private String variantInfo; // e.g., "Large, Spicy"
}
