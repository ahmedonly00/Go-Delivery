package com.goDelivery.goDelivery.dtos.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BikerEarningsResponse {
    // Biker information
    private Long bikerId;
    private String bikerName;
    
    // Time period information (for aggregated data)
    private String period; // DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Individual earnings records (if needed)
    private List<BikerEarningRecord> earnings;
    
    // Aggregated earnings data
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AggregatedEarnings {
        private int totalDeliveries;
        private BigDecimal totalBaseFee;
        private BigDecimal totalDistanceFee;
        private BigDecimal totalTips;
        private BigDecimal totalBonuses;
        private BigDecimal totalEarnings;
        private LocalDate fromDate;
        private LocalDate toDate;
    }
    
    // Individual earning record
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BikerEarningRecord {
        private Long earningId;
        private Long orderId;
        private String orderReference;
        private LocalDate earningDate;
        private BigDecimal baseFee;
        private BigDecimal distanceFee;
        private BigDecimal tipAmount;
        private BigDecimal bonusAmount;
        private BigDecimal totalEarning;
        private LocalDate createdAt;
    }
    
    // Current aggregated data
    private AggregatedEarnings currentPeriod;
    
    // Previous period for comparison (optional)
    private AggregatedEarnings previousPeriod;
    
    // Additional metrics
    private BigDecimal averageEarningsPerDelivery;
    private BigDecimal averageEarningsPerHour;
    private int totalHoursWorked;
    
    // Payment information (if applicable)
    private String paymentStatus; // PENDING, PROCESSING, PAID, FAILED
    private LocalDate paymentDate;
    private String paymentReference;
}
