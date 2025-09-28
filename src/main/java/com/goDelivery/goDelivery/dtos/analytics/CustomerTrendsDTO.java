package com.goDelivery.goDelivery.dtos.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTrendsDTO {
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Integer orderCount;
    private BigDecimal totalSpent;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    private BigDecimal averageOrderValue;
    private List<PopularItem> favoriteItems;
    private String favoriteCategory;
    private Long averageOrderTimeMinutes; // Time between order placement and delivery

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularItem {
        private Long menuItemId;
        private String itemName;
        private Integer orderCount;
    }
}
