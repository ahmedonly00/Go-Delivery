package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.mpesa.MpesaPaymentRequest;
import com.goDelivery.goDelivery.dtos.mpesa.MpesaPaymentResponse;
import com.goDelivery.goDelivery.dtos.mpesa.MpesaWebhookRequest;
import com.goDelivery.goDelivery.service.MpesaPaymentService;
import com.goDelivery.goDelivery.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/payments/mpesa")
@RequiredArgsConstructor
@Tag(name = "MPESA Payments", description = "Endpoints for handling MPESA mobile money payments")
public class MpesaPaymentController {

    private final MpesaPaymentService mpesaPaymentService;
    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(
        summary = "Initiate MPESA payment",
        description = "Initiates a payment request via MPESA mobile money",
        responses = {
            @ApiResponse(responseCode = "200", description = "Payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = MpesaPaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public Mono<ResponseEntity<MpesaPaymentResponse>> initiatePayment(
            @Valid @RequestBody MpesaPaymentRequest request) {
        return mpesaPaymentService.initiatePayment(request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PostMapping("/webhook")
    @Operation(
        summary = "Handle MPESA payment webhook",
        description = "Handles incoming webhook notifications from MPESA payment system"
    )
    public ResponseEntity<Void> handleWebhook(
            @Valid @RequestBody MpesaWebhookRequest webhookRequest) {
        // Delegate webhook handling to PaymentService
        paymentService.handleMpesaWebhook(webhookRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{transactionId}")
    @Operation(
        summary = "Get MPESA payment status",
        description = "Retrieves the status of a MPESA payment transaction",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MpesaPaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
        }
    )
    public Mono<ResponseEntity<MpesaPaymentResponse>> getTransactionStatus(
            @PathVariable String transactionId) {
        return mpesaPaymentService.queryTransactionStatus(transactionId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
