package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.momo.*;
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
                request.setCallback("http://129.151.188.8:8085/api/v1/payments/momo/webhook");
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
}
