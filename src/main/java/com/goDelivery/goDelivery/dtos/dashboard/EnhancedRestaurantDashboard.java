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
public class EnhancedRestaurantDashboard {

    // Metadata
    private Long restaurantId;
    private String restaurantName;
    private LocalDateTime generatedAt;
    private Integer year;
    private Integer month;
    private Integer week;
    private LocalDate startDate;
    private LocalDate endDate;

    // Today's quick snapshot (always current day, unaffected by filters)
    private TodaySnapshot todaySnapshot;

    // Order summary for the filtered period
    private OrderSummary orders;

    // Revenue summary for the filtered period
    private RevenueSummary revenue;

    // Peak-time analytics for the filtered period
    private TimeAnalytics timeAnalytics;

    // ── Nested classes ────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodaySnapshot {
        private Long ordersToday;
        private BigDecimal revenueToday;
        private BigDecimal avgOrderValue;
        private Long activeOrders;
        private Long completedOrders;
        private Long cancelledOrders;
        private BigDecimal growthVsYesterday; // percentage
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private Long total;
        private Long completed;
        private Long cancelled;
        private Long pending;
        private BigDecimal completionRate;   // percentage
        private BigDecimal cancellationRate; // percentage
        private List<TrendPoint> trend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSummary {
        private BigDecimal total;
        private BigDecimal avgOrderValue;
        private List<TrendPoint> trend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeAnalytics {
        private String peakHour;
        private String peakDay;
        private Map<Integer, Long> ordersByHour;      // hour 0-23 → count
        private Map<String, Long> ordersByDayOfWeek;  // "Monday" → count
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private LocalDate date;
        private Long orderCount;
        private BigDecimal revenue;
    }

    // Kept for backward compatibility with OrderMetrics/RevenueMetrics references
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderMetrics { private OrderSummary summary; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RevenueMetrics { private RevenueSummary summary; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TimeBasedAnalytics { private TimeAnalytics analytics; }
}
