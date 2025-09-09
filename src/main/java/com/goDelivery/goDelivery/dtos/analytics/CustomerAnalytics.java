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
public class CustomerAnalytics {
    private String period; // DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalCustomers;
    private int newCustomers;
    private int activeCustomers;
    private int repeatCustomers;
    private int churnedCustomers;
    private Map<String, Integer> customersBySource;
    private Map<String, Integer> customersByLocation;
    private BigDecimal averageOrderValue;
    private BigDecimal averageOrderFrequency;
    private BigDecimal customerLifetimeValue;
    private List<CustomerAcquisition> customerAcquisition;
    private List<CustomerRetention> customerRetention;
    private List<CustomerSegment> customerSegments;
    private List<CustomerActivity> customerActivities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAcquisition {
        private LocalDate date;
        private int newCustomers;
        private String source;
        private String campaign;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerRetention {
        private String cohort; // e.g., "2023-01"
        private int totalCustomers;
        private Map<Integer, Double> retentionRates; // Key: day/week/month number, Value: retention rate
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSegment {
        private String segmentName; // e.g., "High Value", "At Risk", "New"
        private int customerCount;
        private BigDecimal totalRevenue;
        private double averageOrderValue;
        private double averageOrderFrequency;
        private List<String> criteria; // Criteria used for segmentation
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerActivity {
        private String customerId;
        private String customerName;
        private String customerEmail;
        private int orderCount;
        private BigDecimal totalSpent;
        private LocalDate lastOrderDate;
        private int daysSinceLastOrder;
        private String customerTier; // e.g., "Gold", "Silver", "Bronze"
        private double averageRatingGiven;
        private String preferredPaymentMethod;
        private String favoriteCategory;
    }
}
