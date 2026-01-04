package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Order", description = "Order management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/createOrder")
    public ResponseEntity<List<OrderResponse>> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            log.info("Creating order for customer: {}", orderRequest.getCustomerId());
            
            // Check if restaurantOrders is null
            if (orderRequest.getRestaurantOrders() == null) {
                log.error("Restaurant orders list is null in the request");
                return ResponseEntity.badRequest().build();
            }
            
            log.info("Order contains {} restaurant orders", orderRequest.getRestaurantOrders().size());
            
            List<OrderResponse> result = orderService.createOrder(orderRequest);
            log.info("Order created successfully with {} orders", result.size());
            
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("Invalid order request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.internalServerError().build();
        }
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
    
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(orderService.getOrdersByBranch(branchId));
    }
    
    @GetMapping("/branch/{branchId}/total")
    public ResponseEntity<Long> getTotalOrdersByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(orderService.getTotalOrdersByBranch(branchId));
    }
}
