package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.momo.*;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementCallback;
import com.goDelivery.goDelivery.service.DisbursementService;
import com.goDelivery.goDelivery.service.MomoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/payments/momo")
@RequiredArgsConstructor
@Tag(name = "MoMo Payments", description = "Endpoints for handling MoMo mobile money payments")
public class MomoPaymentController {

    private final MomoService momoService;
    private final DisbursementService disbursementService;

    @PostMapping(value = "/request")
    @Operation(
        summary = "Initiate MoMo payment",
        description = "Initiates a payment request via MoMo mobile money",
        responses = {
            @ApiResponse(responseCode = "200", description = "Payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = MomoPaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<MomoPaymentResponse> requestPayment(
            @Valid @RequestBody MomoPaymentRequest request) {
        try {
            log.info("Received MoMo payment request for external ID: {}", request.getExternalId());
            
            // Generate a unique external ID if not provided
            if (request.getExternalId() == null || request.getExternalId().trim().isEmpty()) {
                request.setExternalId("MOZ_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
            
            // Set default callback if not provided
            if (request.getCallback() == null || request.getCallback().trim().isEmpty()) {
                // This should be configured in your application properties
                request.setCallback("https://delivery.apis.ivas.rw:8085/api/v1/payments/momo/webhook");
            }
            
            MomoPaymentResponse response = momoService.requestPayment(request);
            log.info("MoMo payment initiated successfully. Reference ID: {}", response.getReferenceId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing MoMo payment request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MomoPaymentResponse.error("Failed to process payment: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/status/{referenceId}")
    @Operation(
        summary = "Check payment status",
        description = "Checks the status of a MoMo payment transaction",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MomoTransactionStatus.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
        }
    )
    public ResponseEntity<MomoTransactionStatus> getTransactionStatus(
            @PathVariable String referenceId) {
        try {
            log.debug("Fetching status for MoMo transaction: {}", referenceId);
            MomoTransactionStatus status = momoService.checkTransactionStatus(referenceId);
            
            if (status == null) {
                log.warn("Transaction not found with reference ID: {}", referenceId);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error fetching transaction status for reference {}: {}", referenceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/webhook")
    @Operation(
        summary = "MoMo Payment Webhook",
        description = "Webhook endpoint for MoMo payment callbacks",
        responses = {
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload")
        }
    )
    public ResponseEntity<Void> handleWebhook(
            @RequestBody MomoWebhookRequest webhookRequest) {
        try {
            log.info("Received MoMo webhook for reference ID: {}", webhookRequest.getReferenceId());
            log.debug("Webhook payload: {}", webhookRequest);
            
            momoService.handleWebhook(webhookRequest);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing MoMo webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

   @PostMapping("/processOrderDisbursement")
   @Operation(
        summary = "Process order disbursement",
        description = "Processes disbursement for a completed order to the respective restaurants",
        responses = {
            @ApiResponse(responseCode = "200", 
                description = "Disbursement processed successfully",
                content = @Content(schema = @Schema(implementation = CollectionDisbursementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid order or order not eligible for disbursement"),
            @ApiResponse(responseCode = "500", description = "Error processing disbursement")
        }
    )
    public ResponseEntity<?> processOrderDisbursement(
            @Parameter(description = "Order details for disbursement") 
            @Valid @RequestBody Order order) {
        
        try {
            log.info("Processing disbursement for order: {}", order.getOrderNumber());
            
            // Validate order exists and is eligible for disbursement
            Order existingOrder = orderRepository.findByOrderId(order.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + order.getOrderId()));
                
            if (existingOrder.getPaymentStatus() != PaymentStatus.PAID) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Order is not paid", "orderStatus", existingOrder.getPaymentStatus()));
            }
            
            // Process the disbursement
            CollectionDisbursementResponse response = disbursementService.processOrderDisbursement(existingOrder);
            log.info("Disbursement initiated successfully for order: {}. Reference: {}", 
                    order.getOrderNumber(), response.getReferenceId());
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Invalid disbursement request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing disbursement for order {}: {}", 
                    order.getOrderNumber(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to process disbursement: " + e.getMessage()));
        }
    }

    @GetMapping("/disbursement/status/{referenceId}")
    @Operation(
        summary = "Check disbursement status",
        description = "Checks the status of a disbursement using the reference ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Disbursement not found")
        }
    )
    public ResponseEntity<?> getDisbursementStatus(
            @Parameter(description = "Disbursement reference ID") 
            @PathVariable String referenceId) {
        
        try {
            log.info("Fetching status for disbursement: {}", referenceId);
            
            // Check if it's a collection or disbursement reference
            if (referenceId.startsWith("COLL_")) {
                // Handle collection status check
                return ResponseEntity.ok(disbursementService.getCollectionStatus(referenceId));
            } else {
                // Handle disbursement status check
                return ResponseEntity.ok(disbursementService.getDisbursementStatus(referenceId));
            }
            
        } catch (ResourceNotFoundException e) {
            log.error("Disbursement not found: {}", referenceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Disbursement not found: " + referenceId));
        } catch (Exception e) {
            log.error("Error fetching disbursement status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch disbursement status: " + e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}/disbursements")
    @Operation(
        summary = "Get all disbursements for an order",
        description = "Retrieves all disbursement transactions for a specific order"
    )
    public ResponseEntity<?> getOrderDisbursements(
            @Parameter(description = "Order ID") 
            @PathVariable Long orderId) {
        
        try {
            List<DisbursementTransaction> transactions = 
                transactionRepository.findByOrder_OrderId(orderId);
                
            if (transactions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No disbursements found for order: " + orderId));
            }
            
            // Convert to DTOs if needed
            List<Map<String, Object>> result = transactions.stream()
                .map(tx -> Map.of(
                    "referenceId", tx.getReferenceId(),
                    "status", tx.getStatus().name(),
                    "amount", tx.getAmount(),
                    "restaurant", tx.getRestaurant().getRestaurantName(),
                    "createdAt", tx.getCreatedAt()
                ))
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error fetching disbursements for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch disbursements: " + e.getMessage()));
        }
    }

    
    @PostMapping("/disbursement")
    @Operation(
        summary = "MoMo Disbursement Callback",
        description = "Webhook endpoint for MoMo disbursement callbacks",
        responses = {
            @ApiResponse(responseCode = "200", description = "Disbursement callback processed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<Void> handleDisbursementCallback(
            @RequestBody DisbursementCallback callback) {
        log.info("Received MoMo disbursement callback: {}", callback);
        
        try {
            disbursementService.handleDisbursementCallback(callback);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing disbursement callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
