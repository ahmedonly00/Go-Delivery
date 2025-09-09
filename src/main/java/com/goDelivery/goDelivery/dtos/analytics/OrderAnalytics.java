package com.goDelivery.goDelivery.dtos.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAnalytics {
    private String period; // DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalOrders;
    private int completedOrders;
    private int cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private int newCustomers;
    private int returningCustomers;
    private Map<String, Integer> ordersByStatus;
    private Map<String, BigDecimal> revenueByPaymentMethod;
    private List<DailyOrderStats> dailyStats;
    private List<OrderByHour> ordersByHour;
    private List<PopularItem> popularItems;
    private List<OrderByDeliveryZone> ordersByDeliveryZone;
    private List<OrderByCustomerType> ordersByCustomerType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyOrderStats {
        private LocalDate date;
        private int orderCount;
        private BigDecimal totalRevenue;
        private int completedOrders;
        private int cancelledOrders;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderByHour {
        private int hour;
        private int orderCount;
        private BigDecimal totalRevenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularItem {
        private String itemId;
        private String itemName;
        private int quantitySold;
        private BigDecimal totalRevenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderByDeliveryZone {
        private String zoneId;
        private String zoneName;
        private int orderCount;
        private BigDecimal totalRevenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderByCustomerType {
        private String customerType; // NEW, RETURNING, LOYAL, etc.
        private int orderCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
    }
}
