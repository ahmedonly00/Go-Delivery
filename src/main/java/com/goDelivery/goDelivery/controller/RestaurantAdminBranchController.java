package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchEnabledRestaurantService;
import com.goDelivery.goDelivery.service.BranchSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurant-admin")
@RequiredArgsConstructor
@Tag(name = "Restaurant Admin Branch Management", description = "Endpoints for restaurant admins to manage branches")
@CrossOrigin("*")
public class RestaurantAdminBranchController {

    private final BranchEnabledRestaurantService branchEnabledService;
    private final BranchSecurityService branchSecurity;

    @GetMapping("/branches")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Get all restaurant branches",
        description = "Retrieve all branches belonging to the restaurant admin's restaurant"
    )
    public ResponseEntity<List<BranchesDTO>> getAllBranches(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long restaurantId = branchSecurity.getCurrentRestaurantUser().getRestaurant().getRestaurantId();
        log.info("Getting all branches for restaurant {} by admin {}", restaurantId, userDetails.getUsername());
        
        List<BranchesDTO> branches = branchEnabledService.getRestaurantBranches(restaurantId);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/branches/{branchId}/analytics")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)")
    @Operation(
        summary = "Get branch analytics",
        description = "Retrieve analytics for a specific branch"
    )
    public ResponseEntity<?> getBranchAnalytics(
            @PathVariable Long branchId,
            @Parameter(description = "Report type: sales, orders, customers") @RequestParam(required = false, defaultValue = "sales") String reportType,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting {} analytics for branch {} by admin {}", reportType, branchId, userDetails.getUsername());
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        Object analytics = branchEnabledService.getBranchAnalytics(branchId, reportType, start, end);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Get restaurant-wide analytics",
        description = "Retrieve analytics for the entire restaurant (all branches combined)"
    )
    public ResponseEntity<?> getRestaurantAnalytics(
            @Parameter(description = "Report type: sales, orders, customers, performance") @RequestParam(required = false, defaultValue = "sales") String reportType,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long restaurantId = branchSecurity.getCurrentRestaurantUser().getRestaurant().getRestaurantId();
        log.info("Getting {} analytics for restaurant {} by admin {}", reportType, restaurantId, userDetails.getUsername());
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        Object analytics = branchEnabledService.getRestaurantAnalytics(restaurantId, reportType, start, end);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/branches/{branchId}/menu-items/{menuItemId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)")
    @Operation(
        summary = "Add menu item to branch",
        description = "Copy a restaurant menu item to a specific branch"
    )
    public ResponseEntity<String> addMenuItemToBranch(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Adding menu item {} to branch {} by admin {}", menuItemId, branchId, userDetails.getUsername());
        
        branchEnabledService.createMenuItemForBranch(menuItemId, branchId);
        return ResponseEntity.ok("Menu item successfully added to branch");
    }

    @GetMapping("/branches-comparison")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Compare branches performance",
        description = "Get performance comparison of all branches"
    )
    public ResponseEntity<?> compareBranches(
            @Parameter(description = "Metric to compare: revenue, orders, rating") @RequestParam(required = false, defaultValue = "revenue") String metric,
            @Parameter(description = "Period: daily, weekly, monthly") @RequestParam(required = false, defaultValue = "monthly") String period,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long restaurantId = branchSecurity.getCurrentRestaurantUser().getRestaurant().getRestaurantId();
        log.info("Comparing branches for restaurant {} by admin {} using metric: {}", restaurantId, userDetails.getUsername(), metric);
        
        // This would implement branch comparison logic
        // For now, return a placeholder
        return ResponseEntity.ok("Branch comparison data for restaurant " + restaurantId);
    }
}
