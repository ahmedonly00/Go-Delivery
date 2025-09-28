package com.goDelivery.goDelivery.dtos.analytics;

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
public class SalesReportDTO {
    // Report metadata
    private Long restaurantId;
    private String restaurantName;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Summary metrics
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Map<String, Long> ordersByStatus;
    
    // Time series data
    private List<TimeSeriesDataPoint> timeSeriesData;
    
    // Time series data point
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesDataPoint {
        private LocalDate date;
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private String period;
    }
    
    // Getters and setters for backward compatibility
    public LocalDate getDate() {
        return startDate;
    }
    
    public void setDate(LocalDate date) {
        this.startDate = date;
    }
    
    public BigDecimal getAverageOrderValue() {
        return totalOrders > 0 ? 
            totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
    }
    
    public String getPeriod() {
        return "CUSTOM";
    }
    
    // Static builder method for time series data points
    public static TimeSeriesDataPoint buildTimeSeriesDataPoint(
            LocalDate date, Long totalOrders, BigDecimal totalRevenue, String period) {
        return TimeSeriesDataPoint.builder()
            .date(date)
            .totalOrders(totalOrders)
            .totalRevenue(totalRevenue)
            .period(period)
            .build();
    }
}
