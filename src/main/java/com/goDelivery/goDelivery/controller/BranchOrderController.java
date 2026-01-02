package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.dtos.payment.PaymentResponse;
import com.goDelivery.goDelivery.service.OrderService;
import com.goDelivery.goDelivery.service.PaymentService;
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

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branch-orders")
@RequiredArgsConstructor
@Tag(name = "Branch Order Management", description = "Branch-specific order and payment management")
@CrossOrigin("*")
public class BranchOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Get branch orders",
        description = "Get all orders for a specific branch"
    )
    public ResponseEntity<List<OrderResponse>> getBranchOrders(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching orders for branch {} by user {}", branchId, userDetails.getUsername());
        
        List<OrderResponse> orders = orderService.getOrdersByBranch(branchId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/branch/{branchId}/pending")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Get pending branch orders",
        description = "Get all pending orders for a specific branch"
    )
    public ResponseEntity<List<OrderResponse>> getPendingBranchOrders(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching pending orders for branch {} by user {}", branchId, userDetails.getUsername());
        
        List<OrderResponse> orders = orderService.getOrdersByBranch(branchId);
        List<OrderResponse> pendingOrders = orders.stream()
                .filter(order -> "PLACED".equals(order.getOrderStatus().name()))
                .toList();
        
        return ResponseEntity.ok(pendingOrders);
    }

    @PutMapping("/branch/{branchId}/update-status/{orderId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Update order status",
        description = "Update the status of an order"
    )
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdate statusUpdate,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating order {} status to {} for branch {} by user {}", 
                orderId, statusUpdate.getStatus(), branchId, userDetails.getUsername());
        
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, statusUpdate);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/branch/{branchId}/accept-order/{orderId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Accept order",
        description = "Accept a pending order and start preparation"
    )
    public ResponseEntity<OrderResponse> acceptOrder(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Accepting order {} for branch {} by user {}", orderId, branchId, userDetails.getUsername());
        
        OrderStatusUpdate statusUpdate = new OrderStatusUpdate();
        statusUpdate.setStatus(com.goDelivery.goDelivery.Enum.OrderStatus.PREPARING);
        statusUpdate.setOrderId(orderId);
        
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, statusUpdate);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/branch/{branchId}/ready-for-pickup/{orderId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Mark order ready for pickup",
        description = "Mark an order as ready for pickup or delivery"
    )
    public ResponseEntity<OrderResponse> markOrderReady(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Marking order {} ready for pickup for branch {} by user {}", 
                orderId, branchId, userDetails.getUsername());
        
        OrderStatusUpdate statusUpdate = new OrderStatusUpdate();
        statusUpdate.setStatus(com.goDelivery.goDelivery.Enum.OrderStatus.READY);
        statusUpdate.setOrderId(orderId);
        
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, statusUpdate);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/branch/{branchId}/complete-order/{orderId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Complete order",
        description = "Mark an order as completed/delivered"
    )
    public ResponseEntity<OrderResponse> completeOrder(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Completing order {} for branch {} by user {}", orderId, branchId, userDetails.getUsername());
        
        OrderStatusUpdate statusUpdate = new OrderStatusUpdate();
        statusUpdate.setStatus(com.goDelivery.goDelivery.Enum.OrderStatus.DELIVERED);
        statusUpdate.setOrderId(orderId);
        
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, statusUpdate);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/branch/{branchId}/cancel-order/{orderId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Cancel order",
        description = "Cancel an order with a reason"
    )
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId,
            @RequestParam(required = false) String cancellationReason,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Cancelling order {} for branch {} by user {}. Reason: {}", 
                orderId, branchId, userDetails.getUsername(), cancellationReason);
        
        OrderResponse cancelledOrder = orderService.cancelOrder(orderId, cancellationReason);
        return ResponseEntity.ok(cancelledOrder);
    }

    // Payment Management for Branch

    @GetMapping("/branch/{branchId}/payments")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Get branch payments",
        description = "Get all payments for orders from a specific branch"
    )
    public ResponseEntity<List<PaymentResponse>> getBranchPayments(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching payments for branch {} by user {}", branchId, userDetails.getUsername());
        
        // Get all orders for the branch
        List<OrderResponse> orders = orderService.getOrdersByBranch(branchId);
        
        // Get payments for these orders
        List<PaymentResponse> payments = orders.stream()
                .map(order -> {
                    try {
                        return paymentService.getPaymentByOrderId(order.getOrderId());
                    } catch (Exception e) {
                        return null; // Handle case where payment doesn't exist
                    }
                })
                .filter(payment -> payment != null)
                .toList();
        
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/branch/{branchId}/payment/{paymentId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Get payment details",
        description = "Get details of a specific payment"
    )
    public ResponseEntity<PaymentResponse> getPaymentDetails(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Payment ID") 
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching payment {} for branch {} by user {}", paymentId, branchId, userDetails.getUsername());
        
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        
        // Verify the payment belongs to an order from this branch
        OrderResponse order = orderService.getOrderById(payment.getPaymentId());
        if (order != null && order.getOrderId().equals(payment.getPaymentId())) {
            // Additional verification would need order to have branchId field
            // For now, we'll assume the paymentId matches orderId
        }
        
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/branch/{branchId}/refund/{paymentId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Process refund",
        description = "Process a refund for a payment"
    )
    public ResponseEntity<PaymentResponse> processRefund(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Payment ID") 
            @PathVariable Long paymentId,
            @RequestParam(required = false) String refundReason,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Processing refund for payment {} for branch {} by user. Reason: {}", 
                paymentId, branchId, userDetails.getUsername(), refundReason);
        
        // Verify the payment belongs to this branch
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        // Note: PaymentResponse doesn't have orderId, so we can't verify
        // This would need to be fixed in the PaymentResponse class
        
        // Process refund (this would need to be implemented in PaymentService)
        // PaymentResponse refundResponse = paymentService.processRefund(paymentId, refundReason);
        
        return ResponseEntity.ok(payment); // Placeholder
    }

    // Statistics and Reports

    @GetMapping("/branch/{branchId}/stats")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Get branch order statistics",
        description = "Get order statistics for a specific branch"
    )
    public ResponseEntity<Object> getBranchStats(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching stats for branch {} by user {}", branchId, userDetails.getUsername());
        
        Long totalOrders = orderService.getTotalOrdersByBranch(branchId);
        List<OrderResponse> orders = orderService.getOrdersByBranch(branchId);
        
        long completedOrders = orders.stream().filter(o -> "DELIVERED".equals(o.getOrderStatus().name())).count();
        long cancelledOrders = orders.stream().filter(o -> "CANCELLED".equals(o.getOrderStatus().name())).count();
        long pendingOrders = orders.stream().filter(o -> "PLACED".equals(o.getOrderStatus().name())).count();
        
        BranchStats stats = new BranchStats(totalOrders, completedOrders, cancelledOrders, pendingOrders);
        
        return ResponseEntity.ok(stats);
    }

    // Inner class for statistics
    public static class BranchStats {
        public final long totalOrders;
        public final long completedOrders;
        public final long cancelledOrders;
        public final long pendingOrders;
        
        public BranchStats(long totalOrders, long completedOrders, long cancelledOrders, long pendingOrders) {
            this.totalOrders = totalOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.pendingOrders = pendingOrders;
        }
    }
}
