package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.dashboard.EnhancedRestaurantDashboard;
import com.goDelivery.goDelivery.dtos.dashboard.SuperAdminDashboard;
import com.goDelivery.goDelivery.service.dashboard.RestaurantDashboardService;
import com.goDelivery.goDelivery.service.dashboard.SuperAdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Enterprise dashboard endpoints for all user roles")
@Slf4j
public class DashboardController {

    private final SuperAdminDashboardService superAdminDashboardService;
    private final RestaurantDashboardService restaurantDashboardService;

    // ============ Super Admin Dashboard Endpoints ============

    @Operation(summary = "Get Super Admin Dashboard", description = "Retrieves comprehensive platform-wide analytics for super admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Super admin only")
    })
    @GetMapping("/super-admin/overview")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SuperAdminDashboard> getSuperAdminDashboard(
            @Parameter(description = "Period: TODAY, WEEK, MONTH, YEAR, CUSTOM") @RequestParam(defaultValue = "MONTH") String period,

            @Parameter(description = "Start date for CUSTOM period") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date for CUSTOM period") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Super admin dashboard requested with period: {}", period);
        SuperAdminDashboard dashboard = superAdminDashboardService.getSuperAdminDashboard(period, startDate, endDate);
        return ResponseEntity.ok(dashboard);
    }

    // ============ Restaurant Admin Dashboard Endpoints ============

    @Operation(summary = "Get Restaurant Dashboard", description = "Retrieves comprehensive restaurant-specific analytics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/restaurant/{restaurantId}/overview")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard> getRestaurantDashboard(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,

            @Parameter(description = "Period: TODAY, WEEK, MONTH, YEAR, CUSTOM") @RequestParam(defaultValue = "MONTH") String period,

            @Parameter(description = "Start date for CUSTOM period") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date for CUSTOM period") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Restaurant dashboard requested for restaurant {} with period: {}", restaurantId, period);
        EnhancedRestaurantDashboard dashboard = restaurantDashboardService.getRestaurantDashboard(
                restaurantId, period, startDate, endDate);
        return ResponseEntity.ok(dashboard);
    }

    @Operation(summary = "Get Today's Snapshot", description = "Quick overview of today's performance for a restaurant")
    @GetMapping("/restaurant/{restaurantId}/today")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard.TodaySnapshot> getTodaySnapshot(
            @PathVariable Long restaurantId) {

        log.info("Today's snapshot requested for restaurant {}", restaurantId);
        EnhancedRestaurantDashboard dashboard = restaurantDashboardService.getRestaurantDashboard(
                restaurantId, "TODAY", null, null);
        return ResponseEntity.ok(dashboard.getTodaySnapshot());
    }

    @Operation(summary = "Get Order Metrics", description = "Detailed order analytics for a restaurant")
    @GetMapping("/restaurant/{restaurantId}/orders")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard.OrderMetrics> getOrderMetrics(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        EnhancedRestaurantDashboard dashboard = restaurantDashboardService.getRestaurantDashboard(
                restaurantId, period, startDate, endDate);
        return ResponseEntity.ok(dashboard.getOrderMetrics());
    }

    @Operation(summary = "Get Revenue Metrics", description = "Detailed revenue analytics for a restaurant")
    @GetMapping("/restaurant/{restaurantId}/revenue")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard.RevenueMetrics> getRevenueMetrics(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        EnhancedRestaurantDashboard dashboard = restaurantDashboardService.getRestaurantDashboard(
                restaurantId, period, startDate, endDate);
        return ResponseEntity.ok(dashboard.getRevenueMetrics());
    }

    @Operation(summary = "Get Menu Performance", description = "Menu item and category performance analytics")
    @GetMapping("/restaurant/{restaurantId}/menu-performance")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard.MenuPerformance> getMenuPerformance(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        EnhancedRestaurantDashboard dashboard = restaurantDashboardService.getRestaurantDashboard(
                restaurantId, period, startDate, endDate);
        return ResponseEntity.ok(dashboard.getMenuPerformance());
    }

    @Operation(summary = "Get Customer Metrics", description = "Customer analytics and behavior insights")
    @GetMapping("/restaurant/{restaurantId}/customers")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard.CustomerMetrics> getCustomerMetrics(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        EnhancedRestaurantDashboard dashboard = restaurantDashboardService.getRestaurantDashboard(
                restaurantId, period, startDate, endDate);
        return ResponseEntity.ok(dashboard.getCustomerMetrics());
    }

    @Operation(summary = "Get Delivery Metrics", description = "Delivery performance and zone analytics")
    @GetMapping("/restaurant/{restaurantId}/delivery")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard.DeliveryMetrics> getDeliveryMetrics(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        EnhancedRestaurantDashboard dashboard = restaurantDashboardService.getRestaurantDashboard(
                restaurantId, period, startDate, endDate);
        return ResponseEntity.ok(dashboard.getDeliveryMetrics());
    }

    @Operation(summary = "Get Time-Based Analytics", description = "Peak hours, day of week, and time slot performance")
    @GetMapping("/restaurant/{restaurantId}/time-analytics")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard.TimeBasedAnalytics> getTimeBasedAnalytics(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        EnhancedRestaurantDashboard dashboard = restaurantDashboardService.getRestaurantDashboard(
                restaurantId, period, startDate, endDate);
        return ResponseEntity.ok(dashboard.getTimeBasedAnalytics());
    }
}
