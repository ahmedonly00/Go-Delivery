package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/createOrder")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.createOrder(orderRequest));
    }

    @GetMapping("/getOrderById/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/getOrdersByCustomer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/getOrdersByRestaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    @PutMapping("/updateOrderStatus/{orderId}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdate statusUpdate) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, statusUpdate));
    }

    @PostMapping("/cancelOrder/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String cancellationReason) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, cancellationReason));
    }

    @GetMapping("/restaurant/{restaurantId}/total")
    public ResponseEntity<Long> getTotalOrdersByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getTotalOrdersByRestaurant(restaurantId));
    }

    @GetMapping("/restaurant/{restaurantId}/stats")
    public ResponseEntity<OrderService.RestaurantOrderStats> getRestaurantOrderStats(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getRestaurantOrderStats(restaurantId));
    }
}
