package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.order.OrderTrackingResponse;
import com.goDelivery.goDelivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Order Tracking", description = "Order tracking management")
public class OrderTrackingController {

    private final OrderService orderService;

    @GetMapping("/{orderId}/track")
    public ResponseEntity<OrderTrackingResponse> trackOrder(@PathVariable Long orderId) {
        log.info("Fetching tracking information for order ID: {}", orderId);
        OrderTrackingResponse trackingInfo = orderService.getOrderTrackingInfo(orderId);
        return ResponseEntity.ok(trackingInfo);
    }
}
