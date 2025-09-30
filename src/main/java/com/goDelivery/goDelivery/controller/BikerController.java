package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.delivery.DeliveryAcceptanceRequest;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryAcceptanceResponse;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryRejectionRequest;
import com.goDelivery.goDelivery.dtos.delivery.PickupConfirmationRequest;
import com.goDelivery.goDelivery.dtos.delivery.PickupConfirmationResponse;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.service.BikerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bikers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class BikerController {

    private final BikerService bikerService;
    private final OrderMapper orderMapper;

    @PostMapping("/acceptDelivery")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<DeliveryAcceptanceResponse> acceptDelivery(
            @Valid @RequestBody DeliveryAcceptanceRequest request) {
        log.info("Biker {} accepting delivery for order {}", request.getBikerId(), request.getOrderId());
        DeliveryAcceptanceResponse response = bikerService.acceptDelivery(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rejectDelivery")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<String> rejectDelivery(
            @Valid @RequestBody DeliveryRejectionRequest request) {
        log.info("Biker {} rejecting delivery for order {} - Reason: {}", 
                request.getBikerId(), request.getOrderId(), request.getReason());
        bikerService.rejectDelivery(request);
        return ResponseEntity.ok("Delivery rejected successfully. Order has been broadcast to other bikers.");
    }

    @GetMapping("/{bikerId}/availableOrders")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<List<OrderResponse>> getAvailableOrders(@PathVariable Long bikerId) {
        log.info("Fetching available orders for biker {}", bikerId);
        List<Order> orders = bikerService.getAvailableOrdersForBiker(bikerId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/{bikerId}/activeOrders")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(@PathVariable Long bikerId) {
        log.info("Fetching active orders for biker {}", bikerId);
        List<Order> orders = bikerService.getBikerActiveOrders(bikerId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @PostMapping("/confirmPickup")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<PickupConfirmationResponse> confirmPickup(
            @Valid @RequestBody PickupConfirmationRequest request) {
        log.info("Biker {} confirming pickup for order {}", request.getBikerId(), request.getOrderId());
        PickupConfirmationResponse response = bikerService.confirmPickup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateLocation")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<String> updateLocation(
            @Valid @RequestBody com.goDelivery.goDelivery.dtos.delivery.LocationUpdateRequest request) {
        log.info("Updating location for biker {}", request.getBikerId());
        bikerService.updateLocation(request);
        return ResponseEntity.ok("Location updated successfully");
    }

    @PostMapping("/getNavigation")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<com.goDelivery.goDelivery.dtos.delivery.NavigationResponse> getNavigation(
            @Valid @RequestBody com.goDelivery.goDelivery.dtos.delivery.NavigationRequest request) {
        log.info("Getting navigation for biker {} and order {}", request.getBikerId(), request.getOrderId());
        com.goDelivery.goDelivery.dtos.delivery.NavigationResponse response = bikerService.getNavigation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bikerId}/navigation/{orderId}")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<com.goDelivery.goDelivery.dtos.delivery.NavigationResponse> startNavigation(
            @PathVariable Long bikerId,
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "CUSTOMER") String destinationType) {
        log.info("Starting navigation for biker {} to {} for order {}", bikerId, destinationType, orderId);
        com.goDelivery.goDelivery.dtos.delivery.NavigationResponse response = 
                bikerService.startNavigation(bikerId, orderId, destinationType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tracking/{orderId}")
    public ResponseEntity<com.goDelivery.goDelivery.dtos.delivery.DeliveryTrackingResponse> getDeliveryTracking(
            @PathVariable Long orderId) {
        log.info("Getting delivery tracking for order {}", orderId);
        com.goDelivery.goDelivery.dtos.delivery.DeliveryTrackingResponse response = 
                bikerService.getDeliveryTracking(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirmDelivery")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<com.goDelivery.goDelivery.dtos.delivery.DeliveryConfirmationResponse> confirmDelivery(
            @Valid @RequestBody com.goDelivery.goDelivery.dtos.delivery.DeliveryConfirmationRequest request) {
        log.info("Biker {} confirming delivery for order {}", request.getBikerId(), request.getOrderId());
        com.goDelivery.goDelivery.dtos.delivery.DeliveryConfirmationResponse response = 
                bikerService.confirmDelivery(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bikerId}/customerDetails/{orderId}")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<com.goDelivery.goDelivery.dtos.delivery.CustomerInteractionDetails> getCustomerDetails(
            @PathVariable Long bikerId,
            @PathVariable Long orderId) {
        log.info("Getting customer interaction details for biker {} and order {}", bikerId, orderId);
        com.goDelivery.goDelivery.dtos.delivery.CustomerInteractionDetails response = 
                bikerService.getCustomerInteractionDetails(orderId, bikerId);
        return ResponseEntity.ok(response);
    }
}
