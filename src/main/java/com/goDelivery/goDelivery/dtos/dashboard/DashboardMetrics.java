package com.goDelivery.goDelivery.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Common dashboard metrics used across different dashboard types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetrics {

    // Order Metrics
    private Long totalOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    private BigDecimal orderCompletionRate;

    // Revenue Metrics
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private BigDecimal growthRate;

    // Performance Metrics
    private Double averagePreparationTime;
    private Double averageDeliveryTime;
    private Double customerSatisfactionScore;

    // Comparison Metrics
    private ComparisonMetrics comparisonWithPreviousPeriod;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonMetrics {
        private BigDecimal revenueChange; // percentage
        private Long orderChange; // percentage
        private BigDecimal averageOrderValueChange; // percentage
        private String trend; // UP, DOWN, STABLE
    }
}
