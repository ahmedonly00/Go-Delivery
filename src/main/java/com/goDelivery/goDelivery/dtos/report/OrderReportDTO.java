package com.goDelivery.goDelivery.dtos.report;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OrderReportDTO {
    private LocalDate date;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
    private Integer pendingOrders;
    private Double averagePreparationTime; // in minutes
    private String peakHour;
}
