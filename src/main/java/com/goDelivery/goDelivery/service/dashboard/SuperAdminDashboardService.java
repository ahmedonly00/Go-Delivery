package com.goDelivery.goDelivery.service.dashboard;

import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.dashboard.SuperAdminDashboard;
import com.goDelivery.goDelivery.dtos.dashboard.SuperAdminDashboard.*;
import com.goDelivery.goDelivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminDashboardService {

    private final RestaurantRepository restaurantRepository;
    private final CustomerRepository customerRepository;
    private final BikersRepository bikersRepository;
    private final OrderRepository orderRepository;

    @Cacheable(value = "superAdminDashboard", key = "#period + '-' + #startDate + '-' + #endDate")
    public SuperAdminDashboard getSuperAdminDashboard(String period, LocalDate startDate, LocalDate endDate) {
        log.info("Generating super admin dashboard with period {}", period);

        DateRange dateRange = calculateDateRange(period, startDate, endDate);

        return SuperAdminDashboard.builder()
                .generatedAt(LocalDate.now())
                .period(period)
                .startDate(dateRange.start)
                .endDate(dateRange.end)
                .platformOverview(getPlatformOverview(dateRange))
                .revenueAnalytics(getRevenueAnalytics(dateRange))
                .orderAnalytics(getOrderAnalytics(dateRange))
                .restaurantAnalytics(getRestaurantAnalytics(dateRange))
                .customerAnalytics(getCustomerAnalytics(dateRange))
                .bikerAnalytics(getBikerAnalytics(dateRange))
                .geographicMetrics(new ArrayList<>())
                .build();
    }

    private PlatformOverview getPlatformOverview(DateRange dateRange) {
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
                .totalOrdersToday(orderRepository.countOrdersByDateRange(todayStart, todayEnd))
                .totalOrdersThisWeek(orderRepository.countOrdersByDateRange(weekStart, todayEnd))
                .totalOrdersThisMonth(orderRepository.countOrdersByDateRange(monthStart, todayEnd))
                .totalOrdersAllTime(orderRepository.count())
                .build();
    }

    private RevenueAnalytics getRevenueAnalytics(DateRange dateRange) {
        LocalDateTime start = dateRange.start.atStartOfDay();
        LocalDateTime end = dateRange.end.atTime(LocalTime.MAX);

        Double totalRevenue = orderRepository.sumRevenueByDateRange(start, end);

        // Get revenue by payment method
        List<Object[]> paymentMethodData = orderRepository.getOrdersByPaymentMethod(start, end);
        Map<String, BigDecimal> revenueByPaymentMethod = new HashMap<>();
        for (Object[] row : paymentMethodData) {
            String method = row[0] != null ? row[0].toString() : "UNKNOWN";
            BigDecimal amount = BigDecimal.valueOf(((Number) row[2]).doubleValue());
            revenueByPaymentMethod.put(method, amount);
        }

        // Get top restaurants by revenue
        List<Object[]> topRestaurants = orderRepository.getTopRestaurantsByRevenue(start, end, PageRequest.of(0, 10));
        List<RestaurantRevenue> topRevenueRestaurants = new ArrayList<>();
        for (Object[] row : topRestaurants) {
            topRevenueRestaurants.add(RestaurantRevenue.builder()
                    .restaurantId(((Number) row[0]).longValue())
                    .restaurantName((String) row[1])
                    .totalOrders(((Number) row[2]).longValue())
                    .totalRevenue(BigDecimal.valueOf(((Number) row[3]).doubleValue()))
                    .averageOrderValue(BigDecimal.valueOf(((Number) row[4]).doubleValue()))
                    .build());
        }

        return RevenueAnalytics.builder()
                .totalPlatformRevenue(BigDecimal.valueOf(totalRevenue != null ? totalRevenue : 0))
                .totalCommissionEarned(BigDecimal.valueOf(totalRevenue != null ? totalRevenue * 0.1 : 0))
                .averageOrderValue(BigDecimal.ZERO)
                .revenueByPaymentMethod(revenueByPaymentMethod)
                .topRevenueRestaurants(topRevenueRestaurants)
                .revenueTimeSeries(new ArrayList<>())
                .build();
    }

    private OrderAnalytics getOrderAnalytics(DateRange dateRange) {
        LocalDateTime start = dateRange.start.atStartOfDay();
        LocalDateTime end = dateRange.end.atTime(LocalTime.MAX);

        Long totalOrders = orderRepository.countOrdersByDateRange(start, end);
        Long completedOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.DELIVERED, start, end);
        Long cancelledOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.CANCELLED, start, end);
        Long pendingOrders = orderRepository.countOrdersByStatusAndDateRange(OrderStatus.PLACED, start, end);

        BigDecimal completionRate = totalOrders > 0
                ? BigDecimal.valueOf(completedOrders * 100.0 / totalOrders)
                : BigDecimal.ZERO;
        BigDecimal cancellationRate = totalOrders > 0
                ? BigDecimal.valueOf(cancelledOrders * 100.0 / totalOrders)
                : BigDecimal.ZERO;

        return OrderAnalytics.builder()
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .pendingOrders(pendingOrders)
                .orderCompletionRate(completionRate)
                .orderCancellationRate(cancellationRate)
                .ordersByStatus(new HashMap<>())
                .ordersByDeliveryType(new HashMap<>())
                .averagePreparationTime(0.0)
                .averageDeliveryTime(0.0)
                .build();
    }

    private RestaurantAnalytics getRestaurantAnalytics(DateRange dateRange) {
        LocalDate today = LocalDate.now();

        return RestaurantAnalytics.builder()
                .topPerformingRestaurants(new ArrayList<>())
                .restaurantsNeedingAttention(new ArrayList<>())
                .newRestaurantsToday(restaurantRepository.countRestaurantsByDateRange(today, today))
                .newRestaurantsThisWeek(restaurantRepository.countRestaurantsByDateRange(today.minusWeeks(1), today))
                .newRestaurantsThisMonth(restaurantRepository.countRestaurantsByDateRange(today.minusMonths(1), today))
                .restaurantsByCuisineType(new HashMap<>())
                .averageRestaurantRating(restaurantRepository.getAverageRestaurantRating())
                .build();
    }

    private CustomerAnalytics getCustomerAnalytics(DateRange dateRange) {
        LocalDate today = LocalDate.now();

        return CustomerAnalytics.builder()
                .newCustomersToday(customerRepository.countCustomersByDateRange(today, today))
                .newCustomersThisWeek(customerRepository.countCustomersByDateRange(today.minusWeeks(1), today))
                .newCustomersThisMonth(customerRepository.countCustomersByDateRange(today.minusMonths(1), today))
                .customerRetentionRate(BigDecimal.ZERO)
                .averageCustomerLifetimeValue(BigDecimal.ZERO)
                .averageCustomerSatisfactionScore(0.0)
                .totalReviews(0L)
                .ratingDistribution(new HashMap<>())
                .build();
    }

    private BikerAnalytics getBikerAnalytics(DateRange dateRange) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime weekStart = LocalDate.now().minusWeeks(1).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().minusMonths(1).atStartOfDay();

        return BikerAnalytics.builder()
                .totalActiveBikers(bikersRepository.countByIsActiveTrue())
                .bikersOnlineNow(bikersRepository.countByIsOnlineTrue())
                .deliveriesCompletedToday(0L) // TODO: Implement
                .deliveriesCompletedThisWeek(0L)
                .deliveriesCompletedThisMonth(0L)
                .averageDeliveryTime(0.0)
                .averageBikerRating(bikersRepository.getAverageBikerRating())
                .topPerformingBikers(new ArrayList<>())
                .build();
    }

    private DateRange calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        if ("CUSTOM".equalsIgnoreCase(period) && startDate != null && endDate != null) {
            return new DateRange(startDate, endDate);
        }

        LocalDate end = LocalDate.now();
        LocalDate start;

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
                start = end.minusMonths(1);
        }

        return new DateRange(start, end);
    }

    private static class DateRange {
        final LocalDate start;
        final LocalDate end;

        DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
    }
}
