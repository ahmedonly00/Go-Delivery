package com.goDelivery.goDelivery.modules.analytics.service;

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
import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
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

        // ── Date resolution ───────────────────────────────────────────────────────

        private LocalDate[] resolveDateRange(Integer year, Integer month, Integer week) {
                int y = year != null ? year : LocalDate.now().getYear();

                if (week != null) {
                        LocalDate monday = LocalDate.of(y, 6, 1)
                                        .with(WeekFields.ISO.weekOfWeekBasedYear(), week)
                                        .with(DayOfWeek.MONDAY);
                        return new LocalDate[] { monday, monday.plusDays(6) };
                } else if (month != null) {
                        LocalDate start = LocalDate.of(y, month, 1);
                        return new LocalDate[] { start, start.withDayOfMonth(start.lengthOfMonth()) };
                } else {
                        return new LocalDate[] { LocalDate.of(y, 1, 1), LocalDate.of(y, 12, 31) };
                }
        }

        // ── Restaurant analytics ──────────────────────────────────────────────────

        public Page<OrderResponse> getOrderHistory(Long restaurantId, Integer year, Integer month, Integer week,
                        Pageable pageable) {
                LocalDate[] range = resolveDateRange(year, month, week);
                return orderAnalyticsRepository.findByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                                restaurantId,
                                range[0].atStartOfDay(),
                                range[1].atTime(LocalTime.MAX),
                                pageable)
                                .map(orderMapper::toOrderResponse);
        }

        public SalesReportDTO generateSalesReport(Long restaurantId, Integer year, Integer month, Integer week,
                        String period) {
                restaurantRepository.findById(restaurantId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found with id: " + restaurantId));

                LocalDate[] range = resolveDateRange(year, month, week);
                String resolvedPeriod = resolvePeriod(period, month, week);

                SalesReportDTO report = salesReportMapper.toSalesReportDTO(restaurantId, range[0], range[1],
                                resolvedPeriod);

                List<SalesReportDTO.TimeSeriesDataPoint> timeSeriesData = generateTimeSeriesData(
                                restaurantId, range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX), resolvedPeriod);
                report.setTimeSeriesData(timeSeriesData);

                return report;
        }

        public List<CustomerTrendsDTO> analyzeCustomerTrends(Long restaurantId, Integer year, Integer month,
                        Integer week) {
                LocalDate[] range = resolveDateRange(year, month, week);

                List<Order> orders = orderAnalyticsRepository.findByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
                                restaurantId, range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));

                return customerTrendsMapper.toCustomerTrendsDTOs(orders);
        }

        // ── Branch analytics ──────────────────────────────────────────────────────

        public Page<OrderResponse> getBranchOrderHistory(Long branchId, Integer year, Integer month, Integer week,
                        Pageable pageable) {
                LocalDate[] range = resolveDateRange(year, month, week);
                return orderAnalyticsRepository.findByBranch_BranchIdAndOrderPlacedAtBetween(
                                branchId,
                                range[0].atStartOfDay(),
                                range[1].atTime(LocalTime.MAX),
                                pageable)
                                .map(orderMapper::toOrderResponse);
        }

        public SalesReportDTO generateBranchSalesReport(Long branchId, Integer year, Integer month, Integer week,
                        String period) {
                LocalDate[] range = resolveDateRange(year, month, week);
                String resolvedPeriod = resolvePeriod(period, month, week);

                SalesReportDTO report = salesReportMapper.toBranchSalesReportDTO(branchId, range[0], range[1],
                                resolvedPeriod);

                List<SalesReportDTO.TimeSeriesDataPoint> timeSeriesData = generateBranchTimeSeriesData(
                                branchId, range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX), resolvedPeriod);
                report.setTimeSeriesData(timeSeriesData);

                return report;
        }

        public List<CustomerTrendsDTO> analyzeBranchCustomerTrends(Long branchId, Integer year, Integer month,
                        Integer week) {
                LocalDate[] range = resolveDateRange(year, month, week);

                List<Order> orders = orderAnalyticsRepository.findByBranch_BranchIdAndOrderPlacedAtBetween(
                                branchId, range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));

                return customerTrendsMapper.toCustomerTrendsDTOs(orders);
        }

        // ── Time series helpers ───────────────────────────────────────────────────

        private String resolvePeriod(String period, Integer month, Integer week) {
                if (period != null && !period.isBlank())
                        return period;
                if (week != null)
                        return "DAILY";
                if (month != null)
                        return "WEEKLY";
                return "MONTHLY";
        }

        private List<SalesReportDTO.TimeSeriesDataPoint> generateTimeSeriesData(Long restaurantId, LocalDateTime start,
                        LocalDateTime end, String period) {
                List<SalesReportDTO.TimeSeriesDataPoint> result = new ArrayList<>();

                if ("DAILY".equalsIgnoreCase(period)) {
                        for (LocalDate date = start.toLocalDate(); !date.isAfter(end.toLocalDate()); date = date
                                        .plusDays(1)) {
                                LocalDateTime dayStart = date.atStartOfDay();
                                LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
                                Long orders = orderAnalyticsRepository
                                                .countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(restaurantId,
                                                                dayStart, dayEnd);
                                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(restaurantId,
                                                dayStart, dayEnd);
                                result.add(SalesReportDTO.buildTimeSeriesDataPoint(date, orders != null ? orders : 0L,
                                                revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                                                period));
                        }
                } else if ("WEEKLY".equalsIgnoreCase(period)) {
                        LocalDate current = start.toLocalDate();
                        while (!current.isAfter(end.toLocalDate())) {
                                LocalDate weekStart = current.with(DayOfWeek.MONDAY);
                                LocalDate weekEnd = weekStart.plusDays(6);
                                LocalDateTime weekStartTime = weekStart.atStartOfDay();
                                LocalDateTime weekEndTime = weekEnd.atTime(LocalTime.MAX);
                                Long orders = orderAnalyticsRepository
                                                .countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(restaurantId,
                                                                weekStartTime, weekEndTime);
                                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(restaurantId,
                                                weekStartTime, weekEndTime);
                                result.add(SalesReportDTO.buildTimeSeriesDataPoint(weekStart,
                                                orders != null ? orders : 0L,
                                                revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                                                period));
                                current = weekEnd.plusDays(1);
                        }
                } else if ("MONTHLY".equalsIgnoreCase(period)) {
                        LocalDate current = start.toLocalDate().withDayOfMonth(1);
                        while (!current.isAfter(end.toLocalDate())) {
                                LocalDate monthStart = current.withDayOfMonth(1);
                                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                                LocalDateTime monthStartTime = monthStart.atStartOfDay();
                                LocalDateTime monthEndTime = monthEnd.atTime(LocalTime.MAX);
                                Long orders = orderAnalyticsRepository
                                                .countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(restaurantId,
                                                                monthStartTime, monthEndTime);
                                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(restaurantId,
                                                monthStartTime, monthEndTime);
                                result.add(SalesReportDTO.buildTimeSeriesDataPoint(monthStart,
                                                orders != null ? orders : 0L,
                                                revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                                                period));
                                current = monthStart.plusMonths(1);
                        }
                } else if ("YEARLY".equalsIgnoreCase(period)) {
                        LocalDate current = start.toLocalDate().withDayOfYear(1);
                        while (!current.isAfter(end.toLocalDate())) {
                                LocalDate yearStart = current.withDayOfYear(1);
                                LocalDate yearEnd = yearStart.plusYears(1).minusDays(1);
                                LocalDateTime yearStartTime = yearStart.atStartOfDay();
                                LocalDateTime yearEndTime = yearEnd.atTime(LocalTime.MAX);
                                Long orders = orderAnalyticsRepository
                                                .countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(restaurantId,
                                                                yearStartTime, yearEndTime);
                                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByDateRange(restaurantId,
                                                yearStartTime, yearEndTime);
                                result.add(SalesReportDTO.buildTimeSeriesDataPoint(yearStart,
                                                orders != null ? orders : 0L,
                                                revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                                                period));
                                current = yearStart.plusYears(1);
                        }
                }

                return result;
        }

        private List<SalesReportDTO.TimeSeriesDataPoint> generateBranchTimeSeriesData(Long branchId,
                        LocalDateTime start, LocalDateTime end, String period) {
                List<SalesReportDTO.TimeSeriesDataPoint> result = new ArrayList<>();

                if ("DAILY".equalsIgnoreCase(period)) {
                        for (LocalDate date = start.toLocalDate(); !date.isAfter(end.toLocalDate()); date = date
                                        .plusDays(1)) {
                                LocalDateTime dayStart = date.atStartOfDay();
                                LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
                                Long orders = orderAnalyticsRepository.countByBranch_BranchIdAndOrderPlacedAtBetween(
                                                branchId, dayStart, dayEnd);
                                Double revenue = orderAnalyticsRepository
                                                .calculateTotalRevenueByBranchAndDateRange(branchId, dayStart, dayEnd);
                                result.add(SalesReportDTO.buildTimeSeriesDataPoint(date, orders != null ? orders : 0L,
                                                revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                                                period));
                        }
                } else if ("WEEKLY".equalsIgnoreCase(period)) {
                        LocalDate current = start.toLocalDate();
                        while (!current.isAfter(end.toLocalDate())) {
                                LocalDate weekStart = current.with(DayOfWeek.MONDAY);
                                LocalDate weekEnd = weekStart.plusDays(6);
                                LocalDateTime weekStartTime = weekStart.atStartOfDay();
                                LocalDateTime weekEndTime = weekEnd.atTime(LocalTime.MAX);
                                Long orders = orderAnalyticsRepository.countByBranch_BranchIdAndOrderPlacedAtBetween(
                                                branchId, weekStartTime, weekEndTime);
                                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByBranchAndDateRange(
                                                branchId, weekStartTime, weekEndTime);
                                result.add(SalesReportDTO.buildTimeSeriesDataPoint(weekStart,
                                                orders != null ? orders : 0L,
                                                revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                                                period));
                                current = weekEnd.plusDays(1);
                        }
                } else if ("MONTHLY".equalsIgnoreCase(period)) {
                        LocalDate current = start.toLocalDate().withDayOfMonth(1);
                        while (!current.isAfter(end.toLocalDate())) {
                                LocalDate monthStart = current.withDayOfMonth(1);
                                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                                LocalDateTime monthStartTime = monthStart.atStartOfDay();
                                LocalDateTime monthEndTime = monthEnd.atTime(LocalTime.MAX);
                                Long orders = orderAnalyticsRepository.countByBranch_BranchIdAndOrderPlacedAtBetween(
                                                branchId, monthStartTime, monthEndTime);
                                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByBranchAndDateRange(
                                                branchId, monthStartTime, monthEndTime);
                                result.add(SalesReportDTO.buildTimeSeriesDataPoint(monthStart,
                                                orders != null ? orders : 0L,
                                                revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                                                period));
                                current = monthStart.plusMonths(1);
                        }
                } else if ("YEARLY".equalsIgnoreCase(period)) {
                        LocalDate current = start.toLocalDate().withDayOfYear(1);
                        while (!current.isAfter(end.toLocalDate())) {
                                LocalDate yearStart = current.withDayOfYear(1);
                                LocalDate yearEnd = yearStart.plusYears(1).minusDays(1);
                                LocalDateTime yearStartTime = yearStart.atStartOfDay();
                                LocalDateTime yearEndTime = yearEnd.atTime(LocalTime.MAX);
                                Long orders = orderAnalyticsRepository.countByBranch_BranchIdAndOrderPlacedAtBetween(
                                                branchId, yearStartTime, yearEndTime);
                                Double revenue = orderAnalyticsRepository.calculateTotalRevenueByBranchAndDateRange(
                                                branchId, yearStartTime, yearEndTime);
                                result.add(SalesReportDTO.buildTimeSeriesDataPoint(yearStart,
                                                orders != null ? orders : 0L,
                                                revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO,
                                                period));
                                current = yearStart.plusYears(1);
                        }
                }

                return result;
        }
}
