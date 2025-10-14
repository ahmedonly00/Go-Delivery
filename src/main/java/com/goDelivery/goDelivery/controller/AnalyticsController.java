package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.analytics.CustomerTrendsDTO;
import com.goDelivery.goDelivery.dtos.analytics.SalesReportDTO;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.service.AnalyticsService;
import lombok.RequiredArgsConstructor;

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
public class AnalyticsController {

    private final AnalyticsService analyticsService;

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
