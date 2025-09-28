package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.analytics.SalesReportDTO;
import com.goDelivery.goDelivery.repository.OrderAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SalesReportMapper {

    private final OrderAnalyticsRepository orderAnalyticsRepository;

    public SalesReportDTO toSalesReportDTO(Long restaurantId, 
                                          LocalDate startDate, 
                                          LocalDate endDate, 
                                          String period) {
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

        // Get total orders and revenue
        Long totalOrders = orderAnalyticsRepository.countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                restaurantId, startDateTime, endDateTime);
                
        Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(
                restaurantId, startDateTime, endDateTime);
        BigDecimal totalRevenue = revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO;
        
        // Get orders by status
        Map<String, Long> ordersByStatus = orderAnalyticsRepository
                .countOrdersByStatusAndRestaurantIdAndOrderPlacedAtBetween(
                    restaurantId, startDateTime, endDateTime)
                .stream()
                .filter(statusCount -> statusCount[0] != null && statusCount[1] != null)
                .collect(Collectors.toMap(
                    statusCount -> statusCount[0].toString(),
                    statusCount -> ((Number) statusCount[1]).longValue()
                ));

        return SalesReportDTO.builder()
                .restaurantId(restaurantId)
                .startDate(startDate != null ? startDate : startDateTime.toLocalDate())
                .endDate(endDate != null ? endDate : endDateTime.toLocalDate())
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .ordersByStatus(ordersByStatus)
                .build();
    }

    public SalesReportDTO.TimeSeriesDataPoint toTimeSeriesDataPoint(LocalDate date, Long totalOrders, 
                                                                  BigDecimal totalRevenue, String period) {
        return SalesReportDTO.buildTimeSeriesDataPoint(
            date,
            totalOrders != null ? totalOrders : 0L,
            totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
            period
        );
    }
}
