package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusCountsDTO;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantRevenueDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<List<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        try {
            log.info("=== ORDER REQUEST RECEIVED ===");
            log.info("Customer ID: {}", orderRequest.getCustomerId());
            log.info("Restaurant Orders: {}", orderRequest.getRestaurantOrders());
            log.info("Restaurant Orders is null: {}", orderRequest.getRestaurantOrders() == null);
            if (orderRequest.getRestaurantOrders() != null) {
                log.info("Restaurant Orders size: {}", orderRequest.getRestaurantOrders().size());
            }
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
    public ResponseEntity<List<OrderResponse>> getOrdersByRestaurant(
        @PathVariable Long restaurantId,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    @PutMapping("/updateOrderStatus/{orderId}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdate statusUpdate,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, statusUpdate));
    }

    @PostMapping("/cancelOrder/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String cancellationReason,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, cancellationReason));
    }

    @PutMapping("/{orderId}/updatePaymentStatus")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus,
            @RequestParam(required = false) String failureReason) {
        log.info("Updating payment status for order {} to {}", orderId, paymentStatus);
        return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, paymentStatus, failureReason));
    }

    @GetMapping("/getTotalOrdersByRestaurant/{restaurantId}/total")
    public ResponseEntity<Long> getTotalOrdersByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getTotalOrdersByRestaurant(restaurantId));
    }

    @GetMapping("/getRestaurantOrderStats/{restaurantId}/stats")
    public ResponseEntity<OrderService.RestaurantOrderStats> getRestaurantOrderStats(
        @PathVariable Long restaurantId,
        @AuthenticationPrincipal UserDetails userDetails

    ) {
        return ResponseEntity.ok(orderService.getRestaurantOrderStats(restaurantId));
    }
    
    @GetMapping("/getOrdersByBranch/{branchId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByBranch(
        @PathVariable Long branchId,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrdersByBranch(branchId));
    }
    
    @GetMapping("/getTotalOrdersByBranch/{branchId}/total")
    public ResponseEntity<Long> getTotalOrdersByBranch(
        @PathVariable Long branchId,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getTotalOrdersByBranch(branchId));
    }

    @GetMapping("/{restaurantId}/revenue")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<RestaurantRevenueDTO> getRestaurantRevenue(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getRestaurantRevenue(restaurantId));
    }

    @GetMapping("/restaurant/{restaurantId}/status-counts")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    public ResponseEntity<OrderStatusCountsDTO> getOrderStatusCountsByRestaurant(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrderStatusCountsByRestaurant(restaurantId));
    }
}
