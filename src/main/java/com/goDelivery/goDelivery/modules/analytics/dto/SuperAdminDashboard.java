package com.goDelivery.goDelivery.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminDashboard {

    // Metadata
    private LocalDateTime generatedAt;
    private Integer year;
    private Integer month;
    private Integer week;
    private LocalDate startDate;
    private LocalDate endDate;

    // Sections
    private PlatformOverview platformOverview;
    private RevenueAnalytics revenueAnalytics;
    private OrderAnalytics orderAnalytics;
    private RestaurantAnalytics restaurantAnalytics;
    private CustomerAnalytics customerAnalytics;
    private BikerAnalytics bikerAnalytics;

    // ── Nested classes ────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformOverview {
        private Long totalRestaurants;
        private Long approvedRestaurants;
        private Long pendingRestaurants;
        private Long rejectedRestaurants;
        private Long totalCustomers;
        private Long totalBikers;
        private Long activeBikers;
        private Long inactiveBikers;
        private Long totalOrdersToday;
        private Long totalOrdersThisWeek;
        private Long totalOrdersThisMonth;
        private Long totalOrdersAllTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueAnalytics {
        private BigDecimal totalPlatformRevenue;
        private BigDecimal totalCommissionEarned;
        private Map<String, BigDecimal> revenueByPaymentMethod;
        private List<RestaurantRevenue> topRevenueRestaurants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderAnalytics {
        private Long totalOrders;
        private Long completedOrders;
        private Long cancelledOrders;
        private Long pendingOrders;
        private BigDecimal orderCompletionRate;
        private BigDecimal orderCancellationRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantAnalytics {
        private Long newRestaurantsToday;
        private Long newRestaurantsThisWeek;
        private Long newRestaurantsThisMonth;
        private Double averageRestaurantRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAnalytics {
        private Long newCustomersToday;
        private Long newCustomersThisWeek;
        private Long newCustomersThisMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BikerAnalytics {
        private Long totalActiveBikers;
        private Long bikersOnlineNow;
        private Double averageBikerRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantRevenue {
        private Long restaurantId;
        private String restaurantName;
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
    }
}
