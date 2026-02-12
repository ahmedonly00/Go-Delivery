package com.goDelivery.goDelivery.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminDashboard {

    // Metadata
    private LocalDate generatedAt;
    private String period; // TODAY, WEEK, MONTH, YEAR, CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;

    // Platform Overview
    private PlatformOverview platformOverview;

    // Revenue Analytics
    private RevenueAnalytics revenueAnalytics;

    // Order Analytics
    private OrderAnalytics orderAnalytics;

    // Restaurant Analytics
    private RestaurantAnalytics restaurantAnalytics;

    // Customer Analytics
    private CustomerAnalytics customerAnalytics;

    // Biker Analytics
    private BikerAnalytics bikerAnalytics;

    // Geographic Analytics
    private List<GeographicMetric> geographicMetrics;

    // ============ Nested Classes ============

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
        private BigDecimal averageOrderValue;
        private Map<String, BigDecimal> revenueByPaymentMethod; // MOMO, MPESA, CASH
        private List<RestaurantRevenue> topRevenueRestaurants;
        private List<TimeSeriesData> revenueTimeSeries;
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
        private Map<String, Long> ordersByStatus; // PLACED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
        private Map<String, Long> ordersByDeliveryType; // SELF_DELIVERY, SYSTEM_DELIVERY
        private Double averagePreparationTime;
        private Double averageDeliveryTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantAnalytics {
        private List<TopRestaurant> topPerformingRestaurants;
        private List<RestaurantAlert> restaurantsNeedingAttention;
        private Long newRestaurantsToday;
        private Long newRestaurantsThisWeek;
        private Long newRestaurantsThisMonth;
        private Map<String, Long> restaurantsByCuisineType;
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
        private BigDecimal customerRetentionRate;
        private BigDecimal averageCustomerLifetimeValue;
        private Double averageCustomerSatisfactionScore;
        private Long totalReviews;
        private Map<Integer, Long> ratingDistribution; // 1-5 stars
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BikerAnalytics {
        private Long totalActiveBikers;
        private Long bikersOnlineNow;
        private Long deliveriesCompletedToday;
        private Long deliveriesCompletedThisWeek;
        private Long deliveriesCompletedThisMonth;
        private Double averageDeliveryTime;
        private Double averageBikerRating;
        private List<TopBiker> topPerformingBikers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantRevenue {
        private Long restaurantId;
        private String restaurantName;
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRestaurant {
        private Long restaurantId;
        private String restaurantName;
        private String cuisineType;
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private Double rating;
        private Integer totalReviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantAlert {
        private Long restaurantId;
        private String restaurantName;
        private String alertType; // LOW_RATING, HIGH_CANCELLATION, INACTIVE
        private String alertMessage;
        private Double rating;
        private BigDecimal cancellationRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopBiker {
        private Long bikerId;
        private String bikerName;
        private Long deliveriesCompleted;
        private Double averageDeliveryTime;
        private Double rating;
        private BigDecimal totalEarnings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeographicMetric {
        private String location;
        private Long orderCount;
        private BigDecimal totalRevenue;
        private Long activeRestaurants;
        private Long activeCustomers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private LocalDate date;
        private Long orderCount;
        private BigDecimal revenue;
        private Long newCustomers;
        private Long newRestaurants;
    }
}
