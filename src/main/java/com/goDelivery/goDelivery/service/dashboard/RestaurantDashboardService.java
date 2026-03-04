package com.goDelivery.goDelivery.service.dashboard;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.dashboard.EnhancedRestaurantDashboard;
import com.goDelivery.goDelivery.dtos.dashboard.EnhancedRestaurantDashboard.*;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.*;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantDashboardService {

    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    // ── Main entry point ──────────────────────────────────────────────────────

    public EnhancedRestaurantDashboard getRestaurantDashboard(Long restaurantId,
            Integer year, Integer month, Integer week) {
        log.info("Generating dashboard for restaurant {} year={} month={} week={}", restaurantId, year, month, week);

        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        LocalDate[] range = resolveDateRange(year, month, week);
        LocalDate startDate = range[0];
        LocalDate endDate = range[1];

        return EnhancedRestaurantDashboard.builder()
                .restaurantId(restaurantId)
                .restaurantName(restaurant.getRestaurantName())
                .generatedAt(LocalDateTime.now())
                .year(year != null ? year : LocalDate.now().getYear())
                .month(month)
                .week(week)
                .startDate(startDate)
                .endDate(endDate)
                .todaySnapshot(buildTodaySnapshot(restaurantId))
                .orders(buildOrderSummary(restaurantId, startDate, endDate))
                .revenue(buildRevenueSummary(restaurantId, startDate, endDate))
                .timeAnalytics(buildTimeAnalytics(restaurantId, startDate, endDate))
                .build();
    }

    // ── Sections ──────────────────────────────────────────────────────────────

    private TodaySnapshot buildTodaySnapshot(Long restaurantId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        Long ordersToday = orZero(orderRepository.countOrdersByRestaurantAndDateRange(restaurantId, todayStart, todayEnd));
        Double revenueToday = orderRepository.sumRevenueByRestaurantAndDateRange(restaurantId, todayStart, todayEnd);
        Double revenueYesterday = orderRepository.sumRevenueByRestaurantAndDateRange(restaurantId, yesterdayStart, yesterdayEnd);

        Long completedToday = orZero(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.DELIVERED, todayStart, todayEnd));
        Long cancelledToday = orZero(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CANCELLED, todayStart, todayEnd));

        Long activeOrders = (long) (
                orderRepository.findByRestaurant_RestaurantIdAndOrderStatus(restaurantId, OrderStatus.PLACED).size()
                + orderRepository.findByRestaurant_RestaurantIdAndOrderStatus(restaurantId, OrderStatus.CONFIRMED).size()
                + orderRepository.findByRestaurant_RestaurantIdAndOrderStatus(restaurantId, OrderStatus.PREPARING).size());

        BigDecimal rev = bd(revenueToday);
        BigDecimal avgOrderValue = ordersToday > 0
                ? rev.divide(BigDecimal.valueOf(ordersToday), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return TodaySnapshot.builder()
                .ordersToday(ordersToday)
                .revenueToday(rev)
                .avgOrderValue(avgOrderValue)
                .activeOrders(activeOrders)
                .completedOrders(completedToday)
                .cancelledOrders(cancelledToday)
                .growthVsYesterday(growthRate(revenueYesterday, revenueToday))
                .build();
    }

    private OrderSummary buildOrderSummary(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Long total = orZero(orderRepository.countOrdersByRestaurantAndDateRange(restaurantId, start, end));
        Long completed = orZero(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.DELIVERED, start, end));
        Long cancelled = orZero(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CANCELLED, start, end));
        Long pending = orZero(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.PLACED, start, end));

        BigDecimal completionRate = total > 0
                ? BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        BigDecimal cancellationRate = total > 0
                ? BigDecimal.valueOf(cancelled).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<TrendPoint> trend = orderRepository.getDailyOrdersAndRevenue(restaurantId, start, end).stream()
                .map(row -> TrendPoint.builder()
                        .date((LocalDate) row[0])
                        .orderCount(((Number) row[1]).longValue())
                        .revenue(BigDecimal.valueOf(((Number) row[2]).doubleValue()))
                        .build())
                .collect(Collectors.toList());

        return OrderSummary.builder()
                .total(total)
                .completed(completed)
                .cancelled(cancelled)
                .pending(pending)
                .completionRate(completionRate.setScale(2, RoundingMode.HALF_UP))
                .cancellationRate(cancellationRate.setScale(2, RoundingMode.HALF_UP))
                .trend(trend)
                .build();
    }

    private RevenueSummary buildRevenueSummary(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Double total = orderRepository.sumRevenueByRestaurantAndDateRange(restaurantId, start, end);
        Double avg = orderRepository.getAverageOrderValueByRestaurant(restaurantId);

        List<TrendPoint> trend = orderRepository.getDailyOrdersAndRevenue(restaurantId, start, end).stream()
                .map(row -> TrendPoint.builder()
                        .date((LocalDate) row[0])
                        .orderCount(((Number) row[1]).longValue())
                        .revenue(BigDecimal.valueOf(((Number) row[2]).doubleValue()))
                        .build())
                .collect(Collectors.toList());

        return RevenueSummary.builder()
                .total(bd(total))
                .avgOrderValue(bd(avg))
                .trend(trend)
                .build();
    }

    private TimeAnalytics buildTimeAnalytics(Long restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Map<Integer, Long> ordersByHour = new HashMap<>();
        for (Object[] row : orderRepository.getOrdersByHourOfDay(restaurantId, start, end)) {
            ordersByHour.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        Map<String, Long> ordersByDayOfWeek = new LinkedHashMap<>();
        String[] days = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        for (Object[] row : orderRepository.getOrdersByDayOfWeek(restaurantId, start, end)) {
            int idx = ((Number) row[0]).intValue() - 1;
            ordersByDayOfWeek.put(days[idx], ((Number) row[1]).longValue());
        }

        String peakHour = ordersByHour.isEmpty() ? "N/A"
                : ordersByHour.entrySet().stream().max(Map.Entry.comparingByValue())
                        .map(e -> String.format("%02d:00", e.getKey())).orElse("N/A");

        String peakDay = ordersByDayOfWeek.isEmpty() ? "N/A"
                : ordersByDayOfWeek.entrySet().stream().max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey).orElse("N/A");

        return TimeAnalytics.builder()
                .peakHour(peakHour)
                .peakDay(peakDay)
                .ordersByHour(ordersByHour)
                .ordersByDayOfWeek(ordersByDayOfWeek)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    private BigDecimal growthRate(Double previous, Double current) {
        if (previous == null || previous == 0) return BigDecimal.ZERO;
        double growth = ((current != null ? current : 0) - previous) / previous * 100;
        return BigDecimal.valueOf(growth).setScale(2, RoundingMode.HALF_UP);
    }

    private Long orZero(Long value) {
        return value != null ? value : 0L;
    }

    private BigDecimal bd(Double value) {
        return BigDecimal.valueOf(value != null ? value : 0);
    }
}
