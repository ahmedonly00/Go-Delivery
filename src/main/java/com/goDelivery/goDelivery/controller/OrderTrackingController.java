package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.order.OrderTrackingResponse;
import com.goDelivery.goDelivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingController {

    private final OrderService orderService;

    @GetMapping("/{orderId}/track")
    public ResponseEntity<OrderTrackingResponse> trackOrder(@PathVariable Long orderId) {
        log.info("Fetching tracking information for order ID: {}", orderId);
        OrderTrackingResponse trackingInfo = orderService.getOrderTrackingInfo(orderId);
        return ResponseEntity.ok(trackingInfo);
    }
}
