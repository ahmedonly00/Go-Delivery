package com.goDelivery.goDelivery.modules.analytics.controller;

import com.goDelivery.goDelivery.dtos.analytics.CustomerTrendsDTO;
import com.goDelivery.goDelivery.dtos.analytics.SalesReportDTO;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Analytics", description = "Analytics management")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ── Restaurant analytics ──────────────────────────────────────────────────

    @GetMapping("/getOrdersHistory")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(summary = "Get restaurant order history",
               description = "Filter by year, month (1-12), or week (1-53). Defaults to current year if none provided.")
    public ResponseEntity<Page<OrderResponse>> getOrderHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long restaurantId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(analyticsService.getOrderHistory(restaurantId, year, month, week, pageable));
    }

    @GetMapping("/getSalesReport")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(summary = "Generate restaurant sales report",
               description = "Filter by year, month, or week. Period auto-defaults: week→DAILY, month→WEEKLY, year→MONTHLY.")
    public ResponseEntity<SalesReportDTO> generateSalesReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long restaurantId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String period) {

        return ResponseEntity.ok(analyticsService.generateSalesReport(restaurantId, year, month, week, period));
    }

    @GetMapping("/getCustomerTrends")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(summary = "Analyze restaurant customer trends",
               description = "Filter by year, month, or week.")
    public ResponseEntity<List<CustomerTrendsDTO>> analyzeCustomerTrends(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long restaurantId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week) {

        return ResponseEntity.ok(analyticsService.analyzeCustomerTrends(restaurantId, year, month, week));
    }

    // ── Branch analytics ──────────────────────────────────────────────────────

    @GetMapping("/branches/{branchId}/getOrdersHistory")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get branch order history",
               description = "Filter by year, month (1-12), or week (1-53). Defaults to current year if none provided.")
    public ResponseEntity<Page<OrderResponse>> getBranchOrderHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(analyticsService.getBranchOrderHistory(branchId, year, month, week, pageable));
    }

    @GetMapping("/branches/{branchId}/getSalesReport")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Generate branch sales report",
               description = "Filter by year, month, or week. Period auto-defaults: week→DAILY, month→WEEKLY, year→MONTHLY.")
    public ResponseEntity<SalesReportDTO> generateBranchSalesReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String period) {

        return ResponseEntity.ok(analyticsService.generateBranchSalesReport(branchId, year, month, week, period));
    }

    @GetMapping("/branches/{branchId}/getCustomerTrends")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Analyze branch customer trends",
               description = "Filter by year, month, or week.")
    public ResponseEntity<List<CustomerTrendsDTO>> analyzeBranchCustomerTrends(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week) {

        return ResponseEntity.ok(analyticsService.analyzeBranchCustomerTrends(branchId, year, month, week));
    }
}
