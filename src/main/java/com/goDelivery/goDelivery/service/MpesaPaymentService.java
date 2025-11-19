package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.config.MpesaConfig;
import com.goDelivery.goDelivery.dto.mpesa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling MPESA payment integration including:
 * - Payment initiation
 * - Transaction status querying
 * - Webhook handling with signature validation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaPaymentService {
    private final MpesaConfig mpesaConfig;
    private final WebClient webClient;
    
    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Initiate a payment request to MPESA
     * @param request Payment request details
     * @return Payment response with transaction details
     */
    @Retryable(
        value = { WebClientResponseException.class },
        maxAttemptsExpression = "${mpesa.max-retries:3}",
        backoff = @Backoff(delayExpression = "${mpesa.retry-delay:1000}", multiplier = 2)
    )
    public Mono<MpesaPaymentResponse> initiatePayment(MpesaPaymentRequest request) {
        // Generate a unique reference if not provided
        if (request.getThirdPartyRef() == null || request.getThirdPartyRef().isEmpty()) {
            request.setThirdPartyRef("GO_DELIVERY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Set default callback URL if not provided
        if (request.getCallback() == null || request.getCallback().isEmpty()) {
            request.setCallback(baseUrl + "/api/webhooks/mpesa-payment");
        }

        log.info("Initiating MPESA payment for MSISDN: {}", request.getFromMsisdn());
        
        return webClient.post()
                .uri("/api/v1/transactions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mpesaConfig.getApiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MpesaApiResponse.class)
                .flatMap(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return Mono.just(response.getData());
                    } else {
                        return Mono.error(new RuntimeException("Failed to initiate payment: " + 
                            (response.getMessage() != null ? response.getMessage() : "Unknown error")));
                    }
                })
                .cast(MpesaPaymentResponse.class)
                .doOnSuccess(response -> 
                    log.info("MPESA payment initiated. Transaction ID: {}", response.getTransactionId()))
                .doOnError(error -> 
                    log.error("Error initiating MPESA payment: {}", error.getMessage()));
    }

    /**
     * Handle payment status update from MPESA webhook
     * @param webhookRequest Webhook payload from MPESA
     * @param signature HMAC signature for webhook validation
     * @return CompletableFuture that completes when webhook is processed
     */
    @Async
    public CompletableFuture<Void> handlePaymentWebhook(MpesaWebhookRequest webhookRequest, String signature) {
        log.info("Processing MPESA webhook for transaction: {}", webhookRequest.getTransactionId());
        
        try {
            // Validate the webhook signature
            if (!validateWebhookSignature(webhookRequest, signature)) {
                log.warn("Invalid webhook signature for transaction: {}", webhookRequest.getTransactionId());
                return CompletableFuture.completedFuture(null);
            }

            // Process based on transaction status
            if (webhookRequest.isSuccessful()) {
                handleSuccessfulPayment(webhookRequest);
            } else if (webhookRequest.isFailed()) {
                handleFailedPayment(webhookRequest);
            } else if (webhookRequest.isPending()) {
                handlePendingPayment(webhookRequest);
            } else {
                log.warn("Unknown transaction status: {} for transaction: {}", 
                        webhookRequest.getTransactionStatus(), webhookRequest.getTransactionId());
            }
            
        } catch (Exception e) {
            log.error("Error processing MPESA webhook for transaction: {}", 
                     webhookRequest.getTransactionId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Query the status of a transaction
     * @param transactionId The transaction ID to query
     * @return Current status of the transaction
     */
    @Retryable(
        value = { WebClientResponseException.class },
        maxAttemptsExpression = "${mpesa.max-retries:3}",
        backoff = @Backoff(delayExpression = "${mpesa.retry-delay:1000}")
    )
    public Mono<MpesaPaymentResponse> queryTransactionStatus(String transactionId) {
        log.info("Querying status for transaction: {}", transactionId);
        
        return webClient.get()
                .uri("/api/v1/transactions/status/" + transactionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mpesaConfig.getApiKey())
                .retrieve()
                .bodyToMono(MpesaApiResponse.class)
                .flatMap(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return Mono.just(response.getData());
                    } else {
                        return Mono.error(new RuntimeException("Failed to query transaction status: " + 
                            (response.getMessage() != null ? response.getMessage() : "Unknown error")));
                    }
                })
                .cast(MpesaPaymentResponse.class)
                .doOnSuccess(response -> 
                    log.info("Retrieved transaction status for {}: {}", 
                            transactionId, response.getTransactionStatus()))
                .doOnError(error -> 
                    log.error("Error querying transaction status: {}", error.getMessage()));
    }

    /**
     * Validate webhook signature using HMAC-SHA256
     * @param request The webhook request
     * @param signature The signature to validate
     * @return true if signature is valid, false otherwise
     */
    public boolean validateWebhookSignature(MpesaWebhookRequest request, String signature) {
        if (signature == null || signature.isEmpty()) {
            log.warn("No signature provided for webhook validation");
            return false;
        }

        try {
            String payload = String.format("%s%s%s",
                request.getTransactionId() != null ? request.getTransactionId() : "",
                request.getTransactionStatus() != null ? request.getTransactionStatus() : "",
                mpesaConfig.getWebhookSecret()
            );
            
            // Use the non-deprecated HmacUtils constructor
            String expectedSignature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, 
                    mpesaConfig.getWebhookSecret().getBytes(StandardCharsets.UTF_8))
                .hmacHex(payload);
            
            // Use constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error validating webhook signature", e);
            return false;
        }
    }

    private void handleSuccessfulPayment(MpesaWebhookRequest webhookRequest) {
        log.info("Payment successful for transaction: {}", webhookRequest.getTransactionId());
        // TODO: Implement successful payment handling
        // 1. Update order status to PAID
        // 2. Send confirmation to customer
        // 3. Trigger any post-payment actions
    }

    private void handleFailedPayment(MpesaWebhookRequest webhookRequest) {
        log.warn("Payment failed for transaction: {}", webhookRequest.getTransactionId());
        // TODO: Implement failed payment handling
        // 1. Update order status to PAYMENT_FAILED
        // 2. Notify customer about payment failure
        // 3. Log the failure for reporting
    }

    private void handlePendingPayment(MpesaWebhookRequest webhookRequest) {
        log.info("Payment pending for transaction: {}", webhookRequest.getTransactionId());
        // TODO: Implement pending payment handling
        // 1. Update order status to PAYMENT_PENDING
        // 2. Schedule a status check if needed
    }
}
