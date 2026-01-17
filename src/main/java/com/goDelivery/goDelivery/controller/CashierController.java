package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.service.CashierService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/cashier")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('CASHIER', 'RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
@CrossOrigin(origins = "*")
@Tag(name = "Cashier", description = "Cashier management")
public class CashierController {

    private final CashierService cashierService;

    @GetMapping(value = "/getPendingOrders")
    public ResponseEntity<Page<OrderResponse>> getPendingOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching pending orders");
        return ResponseEntity.ok(cashierService.getPendingOrders(pageable));
    }

    @Transactional(readOnly = true)
    @GetMapping(value = "/getAllOrders")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all orders with pagination");
        return ResponseEntity.ok(cashierService.getAllOrders(pageable));
    }

    @PostMapping(value = "/acceptOrder/{orderId}")
    public ResponseEntity<OrderResponse> acceptOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @RequestParam @NotNull(message = "Estimated preparation time is required") Integer estimatedPrepTime) {
        log.info("Accepting order ID: {} with estimated prep time: {} minutes", orderId, estimatedPrepTime);
        return ResponseEntity.ok(cashierService.acceptOrder(orderId, estimatedPrepTime));
    }

    @PutMapping(value = "/updateOrderStatus")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderStatusUpdate statusUpdate) {
        log.info("Updating status for order ID: {} to {}", statusUpdate.getOrderId(), statusUpdate.getStatus());
        return ResponseEntity.ok(cashierService.updateOrderStatus(statusUpdate));
    }

    @GetMapping(value = "/getOrderDetails/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Fetching details for order ID: {}", orderId);
        return ResponseEntity.ok(cashierService.getOrderDetails(orderId));
    }

    @GetMapping(value = "/getOrderTimeline/{orderId}")
    public ResponseEntity<OrderResponse> getOrderTimeline(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Fetching timeline for order ID: {}", orderId);
        return ResponseEntity.ok(cashierService.getOrderTimeline(orderId));
    }
    
    @PostMapping(value = "/markOrderReadyForPickup/{orderId}")
    public ResponseEntity<OrderResponse> markOrderReadyForPickup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Marking order ID: {} as ready for pickup", orderId);
        return ResponseEntity.ok(cashierService.markOrderReadyForPickup(orderId));
    }
    
    @PostMapping(value = "/confirmOrderDispatch/{orderId}")
    public ResponseEntity<OrderResponse> confirmOrderDispatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Confirming dispatch for order ID: {}", orderId);
        return ResponseEntity.ok(cashierService.confirmOrderDispatch(orderId));
    }

    @PostMapping(value = "/assignToDelivery/{orderId}")
    public ResponseEntity<OrderResponse> assignToDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @RequestParam Long bikerId) {
        log.info("Assigning order ID: {} to delivery person ID: {}", orderId, bikerId);
        return ResponseEntity.ok(cashierService.assignToDelivery(orderId, bikerId));
    }
}
