package com.goDelivery.goDelivery.service.dashboard;

import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.dashboard.SuperAdminDashboard;
import com.goDelivery.goDelivery.dtos.dashboard.SuperAdminDashboard.*;
import com.goDelivery.goDelivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminDashboardService {

    private final RestaurantRepository restaurantRepository;
    private final CustomerRepository customerRepository;
    private final BikersRepository bikersRepository;
    private final OrderRepository orderRepository;

    public SuperAdminDashboard getSuperAdminDashboard(Integer year, Integer month, Integer week) {
        log.info("Generating super admin dashboard year={} month={} week={}", year, month, week);

        LocalDate[] range = resolveDateRange(year, month, week);
        LocalDate startDate = range[0];
        LocalDate endDate = range[1];

        return SuperAdminDashboard.builder()
                .generatedAt(LocalDateTime.now())
                .year(year != null ? year : LocalDate.now().getYear())
                .month(month)
                .week(week)
                .startDate(startDate)
                .endDate(endDate)
                .platformOverview(buildPlatformOverview())
                .revenueAnalytics(buildRevenueAnalytics(startDate, endDate))
                .orderAnalytics(buildOrderAnalytics(startDate, endDate))
                .restaurantAnalytics(buildRestaurantAnalytics())
                .customerAnalytics(buildCustomerAnalytics())
                .bikerAnalytics(buildBikerAnalytics())
                .build();
    }

    // ── Sections ──────────────────────────────────────────────────────────────

    private PlatformOverview buildPlatformOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime weekStart = LocalDate.now().minusWeeks(1).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().minusMonths(1).atStartOfDay();

        return PlatformOverview.builder()
                .totalRestaurants(restaurantRepository.count())
                .approvedRestaurants(restaurantRepository.countByIsApprovedTrue())
                .pendingRestaurants(restaurantRepository.countByApprovalStatus(ApprovalStatus.PENDING))
                .rejectedRestaurants(restaurantRepository.countByApprovalStatus(ApprovalStatus.REJECTED))
                .totalCustomers(customerRepository.count())
                .totalBikers(bikersRepository.count())
                .activeBikers(bikersRepository.countByIsActiveTrue())
                .inactiveBikers(bikersRepository.count() - bikersRepository.countByIsActiveTrue())
                .totalOrdersToday(orZero(orderRepository.countOrdersByDateRange(todayStart, todayEnd)))
                .totalOrdersThisWeek(orZero(orderRepository.countOrdersByDateRange(weekStart, todayEnd)))
                .totalOrdersThisMonth(orZero(orderRepository.countOrdersByDateRange(monthStart, todayEnd)))
                .totalOrdersAllTime(orderRepository.count())
                .build();
    }

    private RevenueAnalytics buildRevenueAnalytics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Double totalRevenue = orderRepository.sumRevenueByDateRange(start, end);
        BigDecimal total = bd(totalRevenue);

        Map<String, BigDecimal> revenueByPaymentMethod = new HashMap<>();
        for (Object[] row : orderRepository.getOrdersByPaymentMethod(start, end)) {
            String method = row[0] != null ? row[0].toString() : "UNKNOWN";
            revenueByPaymentMethod.put(method, BigDecimal.valueOf(((Number) row[2]).doubleValue()));
        }

        List<RestaurantRevenue> topRevenueRestaurants = new ArrayList<>();
        for (Object[] row : orderRepository.getTopRestaurantsByRevenue(start, end, PageRequest.of(0, 10))) {
            topRevenueRestaurants.add(RestaurantRevenue.builder()
                    .restaurantId(((Number) row[0]).longValue())
                    .restaurantName((String) row[1])
                    .totalOrders(((Number) row[2]).longValue())
                    .totalRevenue(BigDecimal.valueOf(((Number) row[3]).doubleValue()))
                    .averageOrderValue(BigDecimal.valueOf(((Number) row[4]).doubleValue()))
                    .build());
        }

        return RevenueAnalytics.builder()
                .totalPlatformRevenue(total)
                .totalCommissionEarned(total.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP))
                .revenueByPaymentMethod(revenueByPaymentMethod)
                .topRevenueRestaurants(topRevenueRestaurants)
                .build();
    }

    private OrderAnalytics buildOrderAnalytics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Long total = orZero(orderRepository.countOrdersByDateRange(start, end));
        Long completed = orZero(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.DELIVERED, start, end));
        Long cancelled = orZero(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CANCELLED, start, end));
        Long pending = orZero(orderRepository.countOrdersByStatusAndDateRange(OrderStatus.PLACED, start, end));

        BigDecimal completionRate = total > 0
                ? BigDecimal.valueOf(completed * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal cancellationRate = total > 0
                ? BigDecimal.valueOf(cancelled * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return OrderAnalytics.builder()
                .totalOrders(total)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .pendingOrders(pending)
                .orderCompletionRate(completionRate)
                .orderCancellationRate(cancellationRate)
                .build();
    }

    private RestaurantAnalytics buildRestaurantAnalytics() {
        LocalDate today = LocalDate.now();

        return RestaurantAnalytics.builder()
                .newRestaurantsToday(orZero(restaurantRepository.countRestaurantsByDateRange(today, today)))
                .newRestaurantsThisWeek(orZero(restaurantRepository.countRestaurantsByDateRange(today.minusWeeks(1), today)))
                .newRestaurantsThisMonth(orZero(restaurantRepository.countRestaurantsByDateRange(today.minusMonths(1), today)))
                .averageRestaurantRating(restaurantRepository.getAverageRestaurantRating())
                .build();
    }

    private CustomerAnalytics buildCustomerAnalytics() {
        LocalDate today = LocalDate.now();

        return CustomerAnalytics.builder()
                .newCustomersToday(orZero(customerRepository.countCustomersByDateRange(today, today)))
                .newCustomersThisWeek(orZero(customerRepository.countCustomersByDateRange(today.minusWeeks(1), today)))
                .newCustomersThisMonth(orZero(customerRepository.countCustomersByDateRange(today.minusMonths(1), today)))
                .build();
    }

    private BikerAnalytics buildBikerAnalytics() {
        return BikerAnalytics.builder()
                .totalActiveBikers(bikersRepository.countByIsActiveTrue())
                .bikersOnlineNow(bikersRepository.countByIsOnlineTrue())
                .averageBikerRating(bikersRepository.getAverageBikerRating())
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

    private Long orZero(Long value) {
        return value != null ? value : 0L;
    }

    private BigDecimal bd(Double value) {
        return BigDecimal.valueOf(value != null ? value : 0);
    }
}
