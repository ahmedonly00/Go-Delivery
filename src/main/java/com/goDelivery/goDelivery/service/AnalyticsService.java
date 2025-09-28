package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.analytics.CustomerTrendsDTO;
import com.goDelivery.goDelivery.dtos.analytics.SalesReportDTO;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.mapper.CustomerTrendsMapper;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.mapper.SalesReportMapper;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.repository.OrderAnalyticsRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OrderAnalyticsRepository orderAnalyticsRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderMapper orderMapper;
    private final SalesReportMapper salesReportMapper;
    private final CustomerTrendsMapper customerTrendsMapper;

    public Page<OrderResponse> getOrderHistory(Long restaurantId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        
        return orderAnalyticsRepository.findByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                restaurantId, startDateTime, endDateTime, pageable)
                .map(orderMapper::toOrderResponse);
    }

    public SalesReportDTO generateSalesReport(Long restaurantId, LocalDate startDate, LocalDate endDate, String period) {
        // Verify restaurant exists
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
                
        // Generate the report using the mapper
        SalesReportDTO report = salesReportMapper.toSalesReportDTO(restaurantId, startDate, endDate, period);
        
        // Add time series data
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        
        List<SalesReportDTO.TimeSeriesDataPoint> timeSeriesData = generateTimeSeriesData(
                restaurantId, startDateTime, endDateTime, period);
        report.setTimeSeriesData(timeSeriesData);
        
        return report;
    }

    private List<SalesReportDTO.TimeSeriesDataPoint> generateTimeSeriesData(Long restaurantId, LocalDateTime start, LocalDateTime end, String period) {
        List<SalesReportDTO.TimeSeriesDataPoint> result = new ArrayList<>();
        
        if ("DAILY".equalsIgnoreCase(period)) {
            for (LocalDate date = start.toLocalDate(); !date.isAfter(end.toLocalDate()); date = date.plusDays(1)) {
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
                
                Long orders = orderAnalyticsRepository.countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                        restaurantId, dayStart, dayEnd);
                
                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(
                        restaurantId, dayStart, dayEnd);
                
                result.add(SalesReportDTO.buildTimeSeriesDataPoint(
                        date,
                        orders != null ? orders : 0L,
                        revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                        period
                ));
            }
        } else if ("WEEKLY".equalsIgnoreCase(period)) {
            // Group by week
            LocalDate current = start.toLocalDate();
            while (current.isBefore(end.toLocalDate()) || current.isEqual(end.toLocalDate())) {
                LocalDate weekStart = current.with(java.time.DayOfWeek.MONDAY);
                LocalDate weekEnd = weekStart.plusDays(6);
                
                LocalDateTime weekStartTime = weekStart.atStartOfDay();
                LocalDateTime weekEndTime = weekEnd.atTime(LocalTime.MAX);
                
                Long orders = orderAnalyticsRepository.countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                        restaurantId, weekStartTime, weekEndTime);
                
                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(
                        restaurantId, weekStartTime, weekEndTime);
                
                result.add(SalesReportDTO.buildTimeSeriesDataPoint(
                        weekStart,
                        orders != null ? orders : 0L,
                        revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                        period
                ));
                
                current = weekEnd.plusDays(1);
            }
        } else if ("MONTHLY".equalsIgnoreCase(period)) {
            // Group by month
            LocalDate current = start.toLocalDate().withDayOfMonth(1);
            while (current.isBefore(end.toLocalDate()) || current.isEqual(end.toLocalDate())) {
                LocalDate monthStart = current.withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                
                LocalDateTime monthStartTime = monthStart.atStartOfDay();
                LocalDateTime monthEndTime = monthEnd.atTime(LocalTime.MAX);
                
                Long orders = orderAnalyticsRepository.countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                        restaurantId, monthStartTime, monthEndTime);
                
                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(
                        restaurantId, monthStartTime, monthEndTime);
                
                result.add(SalesReportDTO.buildTimeSeriesDataPoint(
                        monthStart,
                        orders != null ? orders : 0L,
                        revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                        period
                ));
                
                current = monthStart.plusMonths(1);
            }
        } else if ("YEARLY".equalsIgnoreCase(period)) {
            // Group by year
            LocalDate current = start.toLocalDate().withDayOfYear(1);
            while (current.isBefore(end.toLocalDate()) || current.isEqual(end.toLocalDate())) {
                LocalDate yearStart = current.withDayOfYear(1);
                LocalDate yearEnd = yearStart.plusYears(1).minusDays(1);
                
                LocalDateTime yearStartTime = yearStart.atStartOfDay();
                LocalDateTime yearEndTime = yearEnd.atTime(LocalTime.MAX);
                
                Long orders = orderAnalyticsRepository.countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                        restaurantId, yearStartTime, yearEndTime);
                
                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(
                        restaurantId, yearStartTime, yearEndTime);
                
                result.add(SalesReportDTO.buildTimeSeriesDataPoint(
                        yearStart,
                        orders != null ? orders : 0L,
                        revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                        period
                ));
                
                current = yearStart.plusYears(1);
            }
        }
        
        return result;
    }

    public List<CustomerTrendsDTO> analyzeCustomerTrends(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        // Set default date range if not provided
        if (startDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusMonths(3);
        } else if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get customer orders
        List<Order> orders = orderAnalyticsRepository.findByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                restaurantId, startDateTime, endDateTime);
                
        // Use the mapper to convert orders to customer trends DTOs
        return customerTrendsMapper.toCustomerTrendsDTOs(orders);
    }
}
