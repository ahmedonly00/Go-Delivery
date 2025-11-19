package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.mpesa.MpesaPaymentRequest;
import com.goDelivery.goDelivery.dto.mpesa.MpesaPaymentResponse;
import com.goDelivery.goDelivery.dto.mpesa.MpesaWebhookRequest;
import com.goDelivery.goDelivery.service.MpesaPaymentService;
import com.goDelivery.goDelivery.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/payments/mpesa")
@RequiredArgsConstructor
public class MpesaPaymentController {

    private final MpesaPaymentService mpesaPaymentService;
    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public Mono<ResponseEntity<MpesaPaymentResponse>> initiatePayment(
            @Valid @RequestBody MpesaPaymentRequest request) {
        return mpesaPaymentService.initiatePayment(request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @Valid @RequestBody MpesaWebhookRequest webhookRequest) {
        // Delegate webhook handling to PaymentService
        paymentService.handleMpesaWebhook(webhookRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{transactionId}")
    public Mono<ResponseEntity<MpesaPaymentResponse>> getTransactionStatus(
            @PathVariable String transactionId) {
        return mpesaPaymentService.queryTransactionStatus(transactionId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
