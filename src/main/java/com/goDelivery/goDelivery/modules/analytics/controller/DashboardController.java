package com.goDelivery.goDelivery.modules.analytics.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            @Parameter(description = "Year e.g. 2025") @RequestParam(required = false) Integer year,
            @Parameter(description = "Month 1-12") @RequestParam(required = false) Integer month,
            @Parameter(description = "ISO week 1-53") @RequestParam(required = false) Integer week) {

        log.info("Super admin dashboard requested year={} month={} week={}", year, month, week);
        return ResponseEntity.ok(superAdminDashboardService.getSuperAdminDashboard(year, month, week));
    }

    // ============ Restaurant Admin Dashboard Endpoints ============

    @Operation(summary = "Get Restaurant Dashboard",
               description = "Filter by year, month (1-12), or week (1-53). Defaults to current year if none provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/restaurant/{restaurantId}/overview")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard> getRestaurantDashboard(
            @PathVariable Long restaurantId,
            @Parameter(description = "Year e.g. 2025") @RequestParam(required = false) Integer year,
            @Parameter(description = "Month 1-12") @RequestParam(required = false) Integer month,
            @Parameter(description = "ISO week 1-53") @RequestParam(required = false) Integer week) {

        log.info("Restaurant dashboard for {} year={} month={} week={}", restaurantId, year, month, week);
        return ResponseEntity.ok(restaurantDashboardService.getRestaurantDashboard(restaurantId, year, month, week));
    }

    @Operation(summary = "Get Today's Snapshot", description = "Quick overview of today's performance")
    @GetMapping("/restaurant/{restaurantId}/today")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<EnhancedRestaurantDashboard.TodaySnapshot> getTodaySnapshot(
            @PathVariable Long restaurantId) {

        log.info("Today's snapshot for restaurant {}", restaurantId);
        return ResponseEntity.ok(restaurantDashboardService.getRestaurantDashboard(restaurantId, null, null, null).getTodaySnapshot());
    }
}
