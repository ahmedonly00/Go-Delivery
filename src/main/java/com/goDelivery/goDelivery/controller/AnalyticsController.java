package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.analytics.CustomerTrendsDTO;
import com.goDelivery.goDelivery.dtos.analytics.SalesReportDTO;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESTAURANT_ADMIN')")
@CrossOrigin(origins = "*")
@Tag(name = "Analytics", description = "Analytics management")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get order history", description = "Retrieves paginated order history for a restaurant")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order history"),
    @ApiResponse(responseCode = "400", description = "Invalid parameters"),
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @GetMapping(value = "/getOrdersHistory")
    public ResponseEntity<Page<OrderResponse>> getOrderHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<OrderResponse> orders = analyticsService.getOrderHistory(
                restaurantId,
                startDate, 
                endDate, 
                pageable
        );
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Generate sales report", description = "Generates a sales report with time series data")
    @GetMapping(value = "/getSalesReport")
    public ResponseEntity<SalesReportDTO> generateSalesReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") String period) {
        
        SalesReportDTO report = analyticsService.generateSalesReport(
                restaurantId, 
                startDate, 
                endDate,
                period
        );
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Analyze customer trends", description = "Analyzes customer ordering patterns")
    @GetMapping(value = "/getCustomerTrends")
    public ResponseEntity<List<CustomerTrendsDTO>> analyzeCustomerTrends(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
       List<CustomerTrendsDTO> trends = analyticsService.analyzeCustomerTrends(
                restaurantId,
                startDate,
                endDate
        );
        return ResponseEntity.ok(trends);
    }
}
