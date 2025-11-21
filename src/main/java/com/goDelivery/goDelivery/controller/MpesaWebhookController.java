package com.goDelivery.goDelivery.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goDelivery.goDelivery.service.MpesaPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class MpesaWebhookController {
    
    private final MpesaPaymentService mpesaPaymentService;
    private final ObjectMapper objectMapper;
    
    @PostMapping("/mpesa-payment")
    public ResponseEntity<?> handleMpesaWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-MPESA-Signature", required = false) String signature,
            @RequestHeader(value = "X-MPESA-Timestamp", required = false) String timestamp) {
        
        String transactionId = "unknown";
        
        try {
            // Parse the payload to get transaction ID for logging
            JsonNode rootNode = objectMapper.readTree(payload);
            transactionId = rootNode.path("transactionId").asText("unknown");
            
            log.info("Received MPESA webhook for transaction: {}", transactionId);
            log.debug("Webhook payload: {}", payload);
            
            // Validate timestamp to prevent replay attacks
            if (timestamp != null && !isValidTimestamp(timestamp)) {
                log.warn("Rejected webhook with expired timestamp: {}", timestamp);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Invalid or expired timestamp"));
            }
            
            // Process the webhook asynchronously
            mpesaPaymentService.processWebhook(payload, signature);
            
            // Always return 200 OK to acknowledge receipt
            return ResponseEntity.ok(createSuccessResponse(transactionId));
            
        } catch (Exception e) {
            log.error("Error processing MPESA webhook for transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error processing webhook: " + e.getMessage()));
        }
    }
    
    private boolean isValidTimestamp(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = Instant.now().getEpochSecond();
            long maxAge = 5 * 60; // 5 minutes in seconds
            
            // Check if timestamp is within the allowed window
            return (currentTime - timestamp) <= maxAge;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private Map<String, Object> createSuccessResponse(String transactionId) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("transactionId", transactionId);
        response.put("message", "Webhook received successfully");
        response.put("timestamp", Instant.now().toString());
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}
