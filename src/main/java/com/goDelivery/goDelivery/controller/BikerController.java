package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.delivery.CustomerInteractionDetails;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryAcceptanceRequest;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryAcceptanceResponse;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryConfirmationRequest;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryConfirmationResponse;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryRejectionRequest;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryTrackingResponse;
import com.goDelivery.goDelivery.dtos.delivery.LocationUpdateRequest;
import com.goDelivery.goDelivery.dtos.delivery.NavigationRequest;
import com.goDelivery.goDelivery.dtos.delivery.NavigationResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bikers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BikerController {

    private final BikerService bikerService;
    private final OrderMapper orderMapper;

    @PostMapping(value = "/acceptDelivery")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<DeliveryAcceptanceResponse> acceptDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeliveryAcceptanceRequest request) {
        log.info("Biker {} accepting delivery for order {}", request.getBikerId(), request.getOrderId());
        DeliveryAcceptanceResponse response = bikerService.acceptDelivery(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/rejectDelivery")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<String> rejectDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeliveryRejectionRequest request) {
        log.info("Biker {} rejecting delivery for order {} - Reason: {}", 
                userDetails.getUsername(), request.getOrderId(), request.getReason());
        bikerService.rejectDelivery(request);
        return ResponseEntity.ok("Delivery rejected successfully. Order has been broadcast to other bikers.");
    }

    @GetMapping(value = "/{bikerId}/availableOrders")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<List<OrderResponse>> getAvailableOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bikerId) {
        log.info("Fetching available orders for biker {}", userDetails.getUsername());
        List<Order> orders = bikerService.getAvailableOrdersForBiker(bikerId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping(value = "/{bikerId}/activeOrders")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bikerId) {
        log.info("Fetching active orders for biker {}", userDetails.getUsername());
        List<Order> orders = bikerService.getBikerActiveOrders(bikerId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    @PostMapping(value = "/confirmPickup")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<PickupConfirmationResponse> confirmPickup(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PickupConfirmationRequest request) {
        log.info("Biker {} confirming pickup for order {}", userDetails.getUsername(), request.getOrderId());
        PickupConfirmationResponse response = bikerService.confirmPickup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/updateLocation")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<String> updateLocation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LocationUpdateRequest request) {
        log.info("Updating location for biker {}", userDetails.getUsername());
        bikerService.updateLocation(request);
        return ResponseEntity.ok("Location updated successfully");
    }

    @PostMapping(value = "/getNavigation")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<NavigationResponse> getNavigation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NavigationRequest request) {
        log.info("Getting navigation for biker {} and order {}", userDetails.getUsername(), request.getOrderId());
        NavigationResponse response = bikerService.getNavigation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{bikerId}/navigation/{orderId}")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<NavigationResponse> startNavigation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bikerId,
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "CUSTOMER") String destinationType) {
        log.info("Starting navigation for biker {} to {} for order {}", userDetails.getUsername(), destinationType, orderId);
        NavigationResponse response = 
                bikerService.startNavigation(bikerId, orderId, destinationType);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/tracking/{orderId}")
    public ResponseEntity<DeliveryTrackingResponse> getDeliveryTracking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        log.info("Getting delivery tracking for order {}", orderId);
        DeliveryTrackingResponse response = 
                bikerService.getDeliveryTracking(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/confirmDelivery")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<DeliveryConfirmationResponse> confirmDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DeliveryConfirmationRequest request) {
        log.info("Biker {} confirming delivery for order {}", userDetails.getUsername(), request.getOrderId());
        DeliveryConfirmationResponse response = 
                bikerService.confirmDelivery(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{bikerId}/customerDetails/{orderId}")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<CustomerInteractionDetails> getCustomerDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bikerId,
            @PathVariable Long orderId) {
        log.info("Getting customer interaction details for biker {} and order {}", userDetails.getUsername(), orderId);
        CustomerInteractionDetails response = 
                bikerService.getCustomerInteractionDetails(orderId, bikerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{bikerId}/deliveryHistory")
    @PreAuthorize("hasRole('BIKER')")
    public ResponseEntity<List<OrderResponse>> getDeliveryHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bikerId) {
        log.info("Getting delivery history for biker {}", userDetails.getUsername());
        List<Order> orders = bikerService.getDeliveryHistory(bikerId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }

    
}
