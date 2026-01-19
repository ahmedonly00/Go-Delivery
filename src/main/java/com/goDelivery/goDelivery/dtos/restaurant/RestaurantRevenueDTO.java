package com.goDelivery.goDelivery.dtos.restaurant;

import lombok.*;
import java.util.Map;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRevenueDTO {

    private Double totalRevenue;
    private Double todayRevenue;
    private Double thisWeekRevenue;
    private Double thisMonthRevenue;
    private Map<LocalDate, Double> dailyRevenue; // Last 30 days
    private Map<String, Double> revenueByPaymentMethod;
}
