package com.goDelivery.goDelivery.service.dashboard;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.dashboard.EnhancedRestaurantDashboard;
import com.goDelivery.goDelivery.dtos.dashboard.EnhancedRestaurantDashboard.*;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.*;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantDashboardService {

    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerRepository customerRepository;
    private final ReviewRepository reviewRepository;

    @Cacheable(value = "restaurantDashboard", key = "#restaurantId + '-' + #period + '-' + #startDate + '-' + #endDate")
    public EnhancedRestaurantDashboard getRestaurantDashboard(Long restaurantId, String period,
            LocalDate startDate, LocalDate endDate) {
        log.info("Generating dashboard for restaurant {} with period {}", restaurantId, period);

        // Verify restaurant exists
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        // Set date range based on period
        DateRange dateRange = calculateDateRange(period, startDate, endDate);

        return EnhancedRestaurantDashboard.builder()
                .restaurantId(restaurantId)
                .restaurantName(restaurant.getRestaurantName())
                .generatedAt(LocalDateTime.now())
                .period(period)
                .startDate(dateRange.getStartDate())
                .endDate(dateRange.getEndDate())
                .todaySnapshot(getTodaySnapshot(restaurantId))
                .orderMetrics(getOrderMetrics(restaurantId, dateRange))
                .revenueMetrics(getRevenueMetrics(restaurantId, dateRange))
                .menuPerformance(getMenuPerformance(restaurantId, dateRange))
                .customerMetrics(getCustomerMetrics(restaurantId, dateRange))
                .deliveryMetrics(getDeliveryMetrics(restaurantId, dateRange))
                .timeBasedAnalytics(getTimeBasedAnalytics(restaurantId, dateRange))
                .build();
    }

    private TodaySnapshot getTodaySnapshot(Long restaurantId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        Long ordersToday = orderRepository.countOrdersByRestaurantAndDateRange(restaurantId, todayStart, todayEnd);
        Double revenueToday = orderRepository.sumRevenueByRestaurantAndDateRange(restaurantId, todayStart, todayEnd);
        Long completedToday = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.DELIVERED, todayStart,
                todayEnd);
        Long cancelledToday = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CANCELLED, todayStart,
                todayEnd);

        // Calculate growth vs yesterday
        Double revenueYesterday = orderRepository.sumRevenueByRestaurantAndDateRange(restaurantId, yesterdayStart,
                yesterdayEnd);
        BigDecimal growthVsYesterday = calculateGrowthRate(revenueYesterday, revenueToday);

        BigDecimal avgOrderValue = ordersToday > 0
                ? BigDecimal.valueOf(revenueToday).divide(BigDecimal.valueOf(ordersToday), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Count active orders (not delivered or cancelled)
        Long activeOrders = (long) (orderRepository
                .findByRestaurant_RestaurantIdAndOrderStatus(restaurantId, OrderStatus.PLACED).size()
                + orderRepository.findByRestaurant_RestaurantIdAndOrderStatus(restaurantId, OrderStatus.CONFIRMED)
                        .size()
                + orderRepository.findByRestaurant_RestaurantIdAndOrderStatus(restaurantId, OrderStatus.PREPARING)
                        .size());

        return TodaySnapshot.builder()
                .ordersToday(ordersToday)
                .revenueToday(BigDecimal.valueOf(revenueToday != null ? revenueToday : 0))
                .averageOrderValueToday(avgOrderValue)
                .activeOrders(activeOrders)
                .completedOrdersToday(completedToday)
                .cancelledOrdersToday(cancelledToday)
                .growthVsYesterday(growthVsYesterday)
                .build();
    }

    private OrderMetrics getOrderMetrics(Long restaurantId, DateRange dateRange) {
        LocalDateTime start = dateRange.getStartDate().atStartOfDay();
        LocalDateTime end = dateRange.getEndDate().atTime(LocalTime.MAX);

        Long totalOrders = orderRepository.countOrdersByRestaurantAndDateRange(restaurantId, start, end);
        Long completedOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.DELIVERED, start, end);
        Long cancelledOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CANCELLED, start, end);
        Long pendingOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.PLACED, start, end);

        BigDecimal completionRate = totalOrders > 0
                ? BigDecimal.valueOf(completedOrders).divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal cancellationRate = totalOrders > 0
                ? BigDecimal.valueOf(cancelledOrders).divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Get orders by status
        List<Object[]> statusCounts = orderRepository.getOrderCountsByStatus(restaurantId, start, end);
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (Object[] row : statusCounts) {
            ordersByStatus.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        // Get time series data
        List<Object[]> dailyData = orderRepository.getDailyOrdersAndRevenue(restaurantId, start, end);
        List<TimeSeriesData> ordersTrend = dailyData.stream()
                .map(row -> TimeSeriesData.builder()
                        .date((LocalDate) row[0])
                        .value(((Number) row[1]).longValue())
                        .amount(BigDecimal.valueOf(((Number) row[2]).doubleValue()))
                        .build())
                .collect(Collectors.toList());

        return OrderMetrics.builder()
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .pendingOrders(pendingOrders)
                .orderCompletionRate(completionRate)
                .orderCancellationRate(cancellationRate)
                .averagePreparationTime(30.0) // TODO: Calculate from actual data
                .ordersByStatus(ordersByStatus)
                .ordersByType(new HashMap<>()) // TODO: Implement when order type is available
                .ordersTrend(ordersTrend)
                .build();
    }

    private RevenueMetrics getRevenueMetrics(Long restaurantId, DateRange dateRange) {
        LocalDateTime start = dateRange.getStartDate().atStartOfDay();
        LocalDateTime end = dateRange.getEndDate().atTime(LocalTime.MAX);

        Double totalRevenue = orderRepository.sumRevenueByRestaurantAndDateRange(restaurantId, start, end);
        Double avgOrderValue = orderRepository.getAverageOrderValueByRestaurant(restaurantId);

        // Get time series data
        List<Object[]> dailyData = orderRepository.getDailyOrdersAndRevenue(restaurantId, start, end);
        List<TimeSeriesData> revenueTrend = dailyData.stream()
                .map(row -> TimeSeriesData.builder()
                        .date((LocalDate) row[0])
                        .value(((Number) row[1]).longValue())
                        .amount(BigDecimal.valueOf(((Number) row[2]).doubleValue()))
                        .build())
                .collect(Collectors.toList());

        return RevenueMetrics.builder()
                .totalRevenue(BigDecimal.valueOf(totalRevenue != null ? totalRevenue : 0))
                .netRevenue(BigDecimal.valueOf(totalRevenue != null ? totalRevenue * 0.9 : 0)) // Assuming 10% fees
                .averageOrderValue(BigDecimal.valueOf(avgOrderValue != null ? avgOrderValue : 0))
                .totalTaxes(BigDecimal.ZERO) // TODO: Calculate from actual data
                .totalFees(BigDecimal.valueOf(totalRevenue != null ? totalRevenue * 0.1 : 0))
                .revenueByCategory(new HashMap<>()) // TODO: Implement
                .revenueByPaymentMethod(new HashMap<>()) // TODO: Implement
                .revenueTrend(revenueTrend)
                .growthRate(BigDecimal.ZERO) // TODO: Calculate vs previous period
                .build();
    }

    private MenuPerformance getMenuPerformance(Long restaurantId, DateRange dateRange) {
        // TODO: Implement menu performance analysis
        return MenuPerformance.builder()
                .topSellingItems(new ArrayList<>())
                .lowPerformingItems(new ArrayList<>())
                .categoryPerformance(new ArrayList<>())
                .totalMenuItems(0)
                .activeMenuItems(0)
                .averageItemRating(0.0)
                .build();
    }

    private CustomerMetrics getCustomerMetrics(Long restaurantId, DateRange dateRange) {
        // TODO: Implement customer metrics
        return CustomerMetrics.builder()
                .totalCustomers(0L)
                .newCustomers(0L)
                .returningCustomers(0L)
                .customerRetentionRate(BigDecimal.ZERO)
                .averageCustomerRating(0.0)
                .totalReviews(0)
                .ratingDistribution(new HashMap<>())
                .topCustomers(new ArrayList<>())
                .build();
    }

    private DeliveryMetrics getDeliveryMetrics(Long restaurantId, DateRange dateRange) {
        // TODO: Implement delivery metrics
        return DeliveryMetrics.builder()
                .averageDeliveryTime(0.0)
                .onTimeDeliveryRate(0.0)
                .deliveryZones(new HashMap<>())
                .totalDeliveryFees(BigDecimal.ZERO)
                .selfDeliveryCount(0L)
                .systemDeliveryCount(0L)
                .build();
    }

    private TimeBasedAnalytics getTimeBasedAnalytics(Long restaurantId, DateRange dateRange) {
        LocalDateTime start = dateRange.getStartDate().atStartOfDay();
        LocalDateTime end = dateRange.getEndDate().atTime(LocalTime.MAX);

        // Get orders by hour
        List<Object[]> hourlyData = orderRepository.getOrdersByHourOfDay(restaurantId, start, end);
        Map<Integer, Long> ordersByHour = new HashMap<>();
        for (Object[] row : hourlyData) {
            ordersByHour.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        // Get orders by day of week
        List<Object[]> weeklyData = orderRepository.getOrdersByDayOfWeek(restaurantId, start, end);
        Map<String, Long> ordersByDayOfWeek = new HashMap<>();
        String[] days = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        for (Object[] row : weeklyData) {
            int dayIndex = ((Number) row[0]).intValue() - 1; // MySQL DAYOFWEEK returns 1-7
            ordersByDayOfWeek.put(days[dayIndex], ((Number) row[1]).longValue());
        }

        // Find peak hour
        String peakHour = ordersByHour.isEmpty() ? "N/A"
                : ordersByHour.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(e -> String.format("%02d:00", e.getKey()))
                        .orElse("N/A");

        // Find peak day
        String peakDay = ordersByDayOfWeek.isEmpty() ? "N/A"
                : ordersByDayOfWeek.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("N/A");

        return TimeBasedAnalytics.builder()
                .peakHour(peakHour)
                .peakDay(peakDay)
                .ordersByHour(ordersByHour)
                .ordersByDayOfWeek(ordersByDayOfWeek)
                .timeSlotPerformance(new ArrayList<>()) // TODO: Implement
                .build();
    }

    // Helper methods

    private DateRange calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        LocalDate start, end;

        if ("CUSTOM".equalsIgnoreCase(period) && startDate != null && endDate != null) {
            start = startDate;
            end = endDate;
        } else {
            end = LocalDate.now();
            switch (period.toUpperCase()) {
                case "TODAY":
                    start = end;
                    break;
                case "WEEK":
                    start = end.minusWeeks(1);
                    break;
                case "MONTH":
                    start = end.minusMonths(1);
                    break;
                case "YEAR":
                    start = end.minusYears(1);
                    break;
                default:
                    start = end.minusMonths(1); // Default to last month
            }
        }

        return new DateRange(start, end);
    }

    private BigDecimal calculateGrowthRate(Double previousValue, Double currentValue) {
        if (previousValue == null || previousValue == 0) {
            return BigDecimal.ZERO;
        }
        double growth = ((currentValue - previousValue) / previousValue) * 100;
        return BigDecimal.valueOf(growth).setScale(2, RoundingMode.HALF_UP);
    }

    // Inner class for date range
    private static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }
    }
}
