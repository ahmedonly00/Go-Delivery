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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BikerDashboard {

    // Metadata
    private Long bikerId;
    private String bikerName;
    private String currentStatus; // AVAILABLE, BUSY, OFFLINE
    private LocalDateTime generatedAt;
    private LocalDate date;

    // Today's Summary
    private TodaySummary todaySummary;

    // Performance Metrics
    private PerformanceMetrics performanceMetrics;

    // Earnings
    private EarningsBreakdown earningsBreakdown;

    // Active Deliveries
    private List<ActiveDelivery> activeDeliveries;

    // Delivery History
    private List<DeliveryHistory> recentDeliveries;

    // ============ Nested Classes ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodaySummary {
        private Long deliveriesCompletedToday;
        private BigDecimal earningsToday;
        private Double averageDeliveryTimeToday;
        private Long activeDeliveries;
        private Double distanceCoveredToday; // in km
        private Double onTimeDeliveryRateToday;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private String period; // TODAY, WEEK, MONTH, ALL_TIME
        private LocalDate startDate;
        private LocalDate endDate;
        private Long totalDeliveries;
        private Long completedDeliveries;
        private Long cancelledDeliveries;
        private Double averageDeliveryTime; // in minutes
        private Double onTimeDeliveryRate; // percentage
        private Double averageRating;
        private Integer totalRatings;
        private Map<Integer, Long> ratingDistribution; // 1-5 stars
        private Double totalDistanceCovered; // in km
        private Map<String, Long> deliveriesByTimeSlot; // Morning, Afternoon, Evening, Night
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EarningsBreakdown {
        private String period; // TODAY, WEEK, MONTH, ALL_TIME
        private BigDecimal totalEarnings;
        private BigDecimal completedPayments;
        private BigDecimal pendingPayments;
        private BigDecimal averageEarningsPerDelivery;
        private List<EarningsByPeriod> earningsTimeSeries;
        private Map<String, BigDecimal> earningsByRestaurant;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EarningsByPeriod {
        private LocalDate date;
        private Long deliveryCount;
        private BigDecimal earnings;
        private Double distanceCovered;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveDelivery {
        private Long orderId;
        private Long deliveryTrackingId;
        private String restaurantName;
        private String customerName;
        private String pickupAddress;
        private String deliveryAddress;
        private Double pickupLatitude;
        private Double pickupLongitude;
        private Double deliveryLatitude;
        private Double deliveryLongitude;
        private String currentStatus;
        private LocalDateTime estimatedArrivalTime;
        private Double distanceToDestination; // in km
        private BigDecimal deliveryFee;
        private String customerPhone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryHistory {
        private Long orderId;
        private String restaurantName;
        private String customerName;
        private LocalDateTime pickupTime;
        private LocalDateTime deliveryTime;
        private Double deliveryDuration; // in minutes
        private Double distance; // in km
        private BigDecimal earnings;
        private Integer customerRating;
        private String customerFeedback;
        private Boolean onTime;
    }
}
