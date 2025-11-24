package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.momo.*;
import com.goDelivery.goDelivery.service.MomoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/momo")
@RequiredArgsConstructor
public class MomoPaymentController {

    private final MomoService momoService;

    @PostMapping("/collection/request")
    public ResponseEntity<MomoPaymentResponse> requestPayment(
            @Valid @RequestBody MomoPaymentRequest request) {
        return ResponseEntity.ok(momoService.requestPayment(request));
    }

    @GetMapping("/collection/status/{referenceId}")
    public ResponseEntity<MomoTransactionStatus> getTransactionStatus(
            @PathVariable String referenceId) {
        return ResponseEntity.ok(momoService.checkTransactionStatus(referenceId));
    }

    @PostMapping("/webhook/callback")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody MomoWebhookRequest webhookRequest) {
        momoService.handleWebhook(webhookRequest);
        return ResponseEntity.ok().build();
    }
}
