package com.goDelivery.goDelivery.modules.branch.controller;

import com.goDelivery.goDelivery.modules.ordering.dto.OrderResponse;
import com.goDelivery.goDelivery.modules.ordering.dto.OrderStatusUpdate;
import com.goDelivery.goDelivery.modules.branch.service.BranchCashierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/branch-cashier")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('BRANCH_CASHIER', 'BRANCH_MANAGER', 'RESTAURANT_ADMIN')")
@CrossOrigin(origins = "*")
@Tag(name = "Branch Cashier", description = "Branch cashier order management")
public class BranchCashierController {

    private final BranchCashierService branchCashierService;

    @GetMapping("/getPendingOrders")
    public ResponseEntity<Page<OrderResponse>> getPendingOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching pending orders for branch cashier: {}", userDetails.getUsername());
        return ResponseEntity.ok(branchCashierService.getPendingOrders(pageable));
    }

    @GetMapping("/getAllOrders")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all orders for branch cashier: {}", userDetails.getUsername());
        return ResponseEntity.ok(branchCashierService.getAllOrders(pageable));
    }

    @PostMapping("/acceptOrder/{orderId}")
    public ResponseEntity<OrderResponse> acceptOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @RequestParam @NotNull(message = "Estimated preparation time is required") Integer estimatedPrepTime) {
        log.info("Accepting order ID: {} by branch cashier: {}", orderId, userDetails.getUsername());
        return ResponseEntity.ok(branchCashierService.acceptOrder(orderId, estimatedPrepTime));
    }

    @PutMapping("/updateOrderStatus")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderStatusUpdate statusUpdate) {
        log.info("Updating status for order ID: {} to {}", statusUpdate.getOrderId(), statusUpdate.getStatus());
        return ResponseEntity.ok(branchCashierService.updateOrderStatus(statusUpdate));
    }

    @GetMapping("/getOrderDetails/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Fetching details for order ID: {}", orderId);
        return ResponseEntity.ok(branchCashierService.getOrderDetails(orderId));
    }

    @GetMapping("/getOrderTimeline/{orderId}")
    public ResponseEntity<OrderResponse> getOrderTimeline(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Fetching timeline for order ID: {}", orderId);
        return ResponseEntity.ok(branchCashierService.getOrderTimeline(orderId));
    }

    @PostMapping("/markOrderReadyForPickup/{orderId}")
    public ResponseEntity<OrderResponse> markOrderReadyForPickup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Marking order ID: {} as ready for pickup", orderId);
        return ResponseEntity.ok(branchCashierService.markOrderReadyForPickup(orderId));
    }

    @PostMapping("/confirmOrderDispatch/{orderId}")
    public ResponseEntity<OrderResponse> confirmOrderDispatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Confirming dispatch for order ID: {}", orderId);
        return ResponseEntity.ok(branchCashierService.confirmOrderDispatch(orderId));
    }

    @PostMapping("/assignToDelivery/{orderId}")
    public ResponseEntity<OrderResponse> assignToDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @RequestParam Long bikerId) {
        log.info("Assigning order ID: {} to biker ID: {}", orderId, bikerId);
        return ResponseEntity.ok(branchCashierService.assignToDelivery(orderId, bikerId));
    }
}
