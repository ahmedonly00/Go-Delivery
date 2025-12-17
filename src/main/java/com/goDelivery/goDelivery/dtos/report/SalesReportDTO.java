package com.goDelivery.goDelivery.dtos.report;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalesReportDTO {
    private LocalDate date;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private String mostPopularItem;
    private Long mostPopularItemCount;
}
