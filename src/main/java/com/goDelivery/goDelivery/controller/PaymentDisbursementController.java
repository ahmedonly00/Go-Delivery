package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.service.DisbursementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Disbursement", description = "Endpoints for managing payment disbursements")
public class PaymentDisbursementController {

    private final DisbursementService disbursementService;
    private final OrderRepository orderRepository;

    @PostMapping("/orders/{orderId}/disburse")
    @Operation(
        summary = "Manually trigger disbursement for an order",
        description = "Manually trigger disbursement of funds to restaurants for a paid order",
        responses = {
            @ApiResponse(responseCode = "200", description = "Disbursement initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Order not eligible for disbursement"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
        }
    )
    @PreAuthorize("hasAuthority('DISBURSEMENT_COLLECTION')")
    public ResponseEntity<?> triggerOrderDisbursement(
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId) {
        
        try {
            log.info("Manual disbursement trigger requested for order: {}", orderId);
            
            // Find the order
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            
            // Validate order is paid
            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Order must be paid before disbursement", 
                                "paymentStatus", order.getPaymentStatus()));
            }
            
            // Check if disbursement already exists
            if (order.getDisbursementReference() != null && !order.getDisbursementReference().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Disbursement already initiated for this order",
                                "disbursementReference", order.getDisbursementReference()));
            }
            
            // Process the disbursement
            CollectionDisbursementResponse response = disbursementService.processOrderDisbursement(order);
            log.info("Manual disbursement initiated for order: {}. Reference: {}", 
                    orderId, response.getReferenceId());
            
            return ResponseEntity.ok(Map.of(
                "message", "Disbursement initiated successfully",
                "orderId", orderId,
                "referenceId", response.getReferenceId(),
                "status", response.getStatus()
            ));
            
        } catch (ResourceNotFoundException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error triggering disbursement for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to trigger disbursement: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/{orderId}/disbursement-status")
    @Operation(
        summary = "Check disbursement status for an order",
        description = "Check the status of disbursement for a specific order",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
        }
    )
    @PreAuthorize("hasAuthority('DISBURSEMENT_STATUS')")
    public ResponseEntity<?> getOrderDisbursementStatus(
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId) {
        
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("paymentStatus", order.getPaymentStatus());
            response.put("disbursementStatus", order.getDisbursementStatus());
            response.put("disbursementReference", order.getDisbursementReference());
            response.put("disbursementCompletedAt", order.getDisbursementCompletedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching disbursement status for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch disbursement status: " + e.getMessage()));
        }
    }
}
