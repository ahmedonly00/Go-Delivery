package com.goDelivery.goDelivery.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Restaurant Dashboard with comprehensive metrics
 * for restaurant admins to monitor their business performance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedRestaurantDashboard {

    // Metadata
    private Long restaurantId;
    private String restaurantName;
    private LocalDateTime generatedAt;
    private String period; // TODAY, WEEK, MONTH, YEAR, CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;

    // Today's Snapshot
    private TodaySnapshot todaySnapshot;

    // Order Metrics
    private OrderMetrics orderMetrics;

    // Revenue Metrics
    private RevenueMetrics revenueMetrics;

    // Menu Performance
    private MenuPerformance menuPerformance;

    // Customer Metrics
    private CustomerMetrics customerMetrics;

    // Delivery Metrics
    private DeliveryMetrics deliveryMetrics;

    // Time-based Analytics
    private TimeBasedAnalytics timeBasedAnalytics;

    // Branch Performance (if multi-branch)
    private List<BranchPerformance> branchPerformance;

    // ============ Nested Classes ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodaySnapshot {
        private Long ordersToday;
        private BigDecimal revenueToday;
        private BigDecimal averageOrderValueToday;
        private Long activeOrders;
        private Long completedOrdersToday;
        private Long cancelledOrdersToday;
        private BigDecimal growthVsYesterday; // percentage
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderMetrics {
        private Long totalOrders;
        private Long completedOrders;
        private Long cancelledOrders;
        private Long pendingOrders;
        private BigDecimal orderCompletionRate;
        private BigDecimal orderCancellationRate;
        private Double averagePreparationTime; // in minutes
        private Map<String, Long> ordersByStatus;
        private Map<String, Long> ordersByType; // DINE_IN, DELIVERY, PICKUP
        private List<TimeSeriesData> ordersTrend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueMetrics {
        private BigDecimal totalRevenue;
        private BigDecimal netRevenue;
        private BigDecimal averageOrderValue;
        private BigDecimal totalTaxes;
        private BigDecimal totalFees;
        private Map<String, BigDecimal> revenueByCategory;
        private Map<String, BigDecimal> revenueByPaymentMethod;
        private List<TimeSeriesData> revenueTrend;
        private BigDecimal growthRate; // percentage vs previous period
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuPerformance {
        private List<MenuItem> topSellingItems;
        private List<MenuItem> lowPerformingItems;
        private List<CategoryPerformance> categoryPerformance;
        private Integer totalMenuItems;
        private Integer activeMenuItems;
        private Double averageItemRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItem {
        private Long itemId;
        private String itemName;
        private String category;
        private Long quantitySold;
        private BigDecimal totalRevenue;
        private BigDecimal price;
        private Double averageRating;
        private Integer totalReviews;
        private BigDecimal profitMargin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPerformance {
        private String categoryName;
        private Long itemsSold;
        private BigDecimal totalRevenue;
        private Integer totalOrders;
        private Double averageRating;
        private BigDecimal revenuePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerMetrics {
        private Long totalCustomers;
        private Long newCustomers;
        private Long returningCustomers;
        private BigDecimal customerRetentionRate;
        private Double averageCustomerRating;
        private Integer totalReviews;
        private Map<Integer, Long> ratingDistribution; // 1-5 stars
        private List<TopCustomer> topCustomers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private Long customerId;
        private String customerName;
        private Long totalOrders;
        private BigDecimal totalSpent;
        private LocalDateTime lastOrderDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryMetrics {
        private Double averageDeliveryTime; // in minutes
        private Double onTimeDeliveryRate; // percentage
        private Map<String, DeliveryZoneMetric> deliveryZones;
        private BigDecimal totalDeliveryFees;
        private Long selfDeliveryCount;
        private Long systemDeliveryCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryZoneMetric {
        private String zoneName;
        private Long orderCount;
        private Double averageDeliveryTime;
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeBasedAnalytics {
        private String peakHour;
        private String peakDay;
        private Map<Integer, Long> ordersByHour; // 0-23
        private Map<String, Long> ordersByDayOfWeek;
        private List<TimeSlotPerformance> timeSlotPerformance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotPerformance {
        private String timeSlot; // BREAKFAST, LUNCH, DINNER, LATE_NIGHT
        private Long orderCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
        private Double averagePreparationTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchPerformance {
        private Long branchId;
        private String branchName;
        private String location;
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private Double rating;
        private BigDecimal orderCompletionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private LocalDate date;
        private Long value;
        private BigDecimal amount;
    }
}
