package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.mpesa.MpesaWebhookRequest;
import com.goDelivery.goDelivery.service.MpesaPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class MpesaWebhookController {
    
    private final MpesaPaymentService mpesaPaymentService;
    
    @PostMapping("/mpesa-payment")
    public ResponseEntity<?> handleMpesaWebhook(
            @RequestBody MpesaWebhookRequest webhookRequest,
            @RequestHeader(value = "X-MPESA-Signature", required = false) String signature) {
        
        log.info("Received MPESA webhook for transaction: {}", 
                webhookRequest.getTransactionId());
        
        try {
            // Process the webhook asynchronously
            mpesaPaymentService.handlePaymentWebhook(webhookRequest, signature);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing MPESA webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
