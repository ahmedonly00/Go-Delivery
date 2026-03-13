package com.goDelivery.goDelivery.modules.analytics.dto;

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
    private Long restaurantId;
    private Long branchId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate date;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private String mostPopularItem;
    private Long mostPopularItemCount;
    private Map<String, Long> ordersByStatus;
    private List<TimeSeriesDataPoint> timeSeriesData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesDataPoint {
        private LocalDate date;
        private Long orderCount;
        private BigDecimal revenue;
        private String period;
    }

    public static TimeSeriesDataPoint buildTimeSeriesDataPoint(
            LocalDate date, Long orderCount, BigDecimal revenue, String period) {
        return TimeSeriesDataPoint.builder()
                .date(date)
                .orderCount(orderCount)
                .revenue(revenue)
                .period(period)
                .build();
    }
}
