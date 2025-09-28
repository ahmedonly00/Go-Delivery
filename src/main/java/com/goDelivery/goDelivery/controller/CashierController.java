package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.service.CashierService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/cashier")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('CASHIER', 'ADMIN')")
public class CashierController {

    private final CashierService cashierService;

    @GetMapping("/orders/pending")
    public ResponseEntity<Page<OrderResponse>> getPendingOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching pending orders");
        return ResponseEntity.ok(cashierService.getPendingOrders(pageable));
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(
            @PathVariable Long orderId,
            @RequestParam @NotNull(message = "Estimated preparation time is required") Integer estimatedPrepTime) {
        log.info("Accepting order ID: {} with estimated prep time: {} minutes", orderId, estimatedPrepTime);
        return ResponseEntity.ok(cashierService.acceptOrder(orderId, estimatedPrepTime));
    }

    @PutMapping("/orders/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Valid @RequestBody OrderStatusUpdate statusUpdate) {
        log.info("Updating status for order ID: {} to {}", statusUpdate.getOrderId(), statusUpdate.getStatus());
        return ResponseEntity.ok(cashierService.updateOrderStatus(statusUpdate));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long orderId) {
        log.info("Fetching details for order ID: {}", orderId);
        return ResponseEntity.ok(cashierService.getOrderDetails(orderId));
    }

    @GetMapping("/orders/{orderId}/timeline")
    public ResponseEntity<OrderResponse> getOrderTimeline(@PathVariable Long orderId) {
        log.info("Fetching timeline for order ID: {}", orderId);
        return ResponseEntity.ok(cashierService.getOrderTimeline(orderId));
    }
    
    @PostMapping("/orders/{orderId}/ready-for-pickup")
    public ResponseEntity<OrderResponse> markOrderReadyForPickup(@PathVariable Long orderId) {
        log.info("Marking order ID: {} as ready for pickup", orderId);
        return ResponseEntity.ok(cashierService.markOrderReadyForPickup(orderId));
    }
    
    @PostMapping("/orders/{orderId}/confirm-dispatch")
    public ResponseEntity<OrderResponse> confirmOrderDispatch(@PathVariable Long orderId) {
        log.info("Confirming dispatch for order ID: {}", orderId);
        return ResponseEntity.ok(cashierService.confirmOrderDispatch(orderId));
    }

    @PostMapping("/orders/{orderId}/assign-delivery")
    public ResponseEntity<OrderResponse> assignToDelivery(
            @PathVariable Long orderId,
            @RequestParam Long deliveryPersonId) {
        log.info("Assigning order ID: {} to delivery person ID: {}", orderId, deliveryPersonId);
        return ResponseEntity.ok(cashierService.assignToDelivery(orderId, deliveryPersonId));
    }
}
