package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.report.OrderReportDTO;
import com.goDelivery.goDelivery.dtos.report.SalesReportDTO;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public SalesReportDTO generateSalesReport(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Order> orders = reportRepository.findOrdersByRestaurantAndDateRange(
            restaurantId, startDateTime, endDateTime);

        // Calculate sales metrics
        int totalOrders = orders.size();
        BigDecimal totalRevenue = orders.stream()
            .map(order -> BigDecimal.valueOf(order.getFinalAmount()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        
        BigDecimal averageOrderValue = totalOrders > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // Get most popular item
        List<Object[]> popularItems = reportRepository.findMostPopularItems(
            restaurantId, startDateTime, endDateTime);
        
        String mostPopularItem = popularItems.isEmpty() ? "N/A" : (String) popularItems.get(0)[0];
        Long mostPopularItemCount = popularItems.isEmpty() ? 0L : (Long) popularItems.get(0)[1];

        SalesReportDTO report = new SalesReportDTO();
        report.setDate(LocalDate.now());
        report.setTotalOrders(totalOrders);
        report.setTotalRevenue(totalRevenue);
        report.setAverageOrderValue(averageOrderValue);
        report.setMostPopularItem(mostPopularItem);
        report.setMostPopularItemCount(mostPopularItemCount);

        return report;
    }

    @Transactional(readOnly = true)
    public OrderReportDTO generateOrderReport(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Order> orders = reportRepository.findOrdersByRestaurantAndDateRange(
            restaurantId, startDateTime, endDateTime);

        // Calculate order metrics
        Map<OrderStatus, Long> statusCounts = orders.stream()
            .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        // Get peak hour
        List<Object[]> peakHours = reportRepository.findPeakOrderHours(
            restaurantId, startDateTime, endDateTime);
        
        String peakHour = peakHours.isEmpty() 
            ? "No orders" 
            : String.format("%02d:00", peakHours.get(0)[1]);

        // Calculate average preparation time (in minutes)
        double avgPrepTime = orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED || o.getOrderStatus()  == OrderStatus.DELIVERED)
            .mapToLong(order -> {
                if (order.getDeliveredAt() != null && order.getOrderPreparedAt() != null) {
                    return java.time.Duration.between(
                        order.getOrderPreparedAt(), 
                        order.getDeliveredAt()
                    ).toMinutes();
                }
                return 0;
            })
            .average()
            .orElse(0.0);

        OrderReportDTO report = new OrderReportDTO();
        report.setDate(LocalDate.now());
        report.setTotalOrders(orders.size());
        report.setCompletedOrders(statusCounts.getOrDefault(OrderStatus.DELIVERED, 0L).intValue());
        report.setCancelledOrders(statusCounts.getOrDefault(OrderStatus.CANCELLED, 0L).intValue());
        report.setPendingOrders(statusCounts.getOrDefault(OrderStatus.PLACED, 0L).intValue());
        report.setAveragePreparationTime(avgPrepTime);
        report.setPeakHour(peakHour);

        return report;
    }
}