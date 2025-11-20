package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.config.MpesaConfig;
import com.goDelivery.goDelivery.dtos.mpesa.*;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import org.springframework.scheduling.TaskScheduler;


@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaPaymentService {

    private final MpesaConfig mpesaConfig;
    private final WebClient webClient;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final TaskScheduler taskScheduler;
    
    @Value("${app.base-url}")
    private String baseUrl;

  
    @Retryable(
        value = { WebClientResponseException.class },
        maxAttemptsExpression = "${mpesa.max-retries:3}",
        backoff = @Backoff(delayExpression = "${mpesa.retry-delay:1000}", multiplier = 2)
    )
    public Mono<MpesaPaymentResponse> initiatePayment(MpesaPaymentRequest request) {
        // Generate a unique reference if not provided
        if (request.getThirdPartyRef() == null || request.getThirdPartyRef().isEmpty()) {
            request.setThirdPartyRef("MOZ_FOOD_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Set default callback URL if not provided
        if (request.getCallback() == null || request.getCallback().isEmpty()) {
            request.setCallback(baseUrl + "/api/webhooks/mpesa-payment");
        }

        log.info("Initiating MPESA payment for MSISDN: {}", request.getFromMsisdn());
        log.debug("Request payload: {}", request);
        
        // Log the API key being used (masked for security)
        if (mpesaConfig.getApiKey() != null) {
            String maskedKey = mpesaConfig.getApiKey().length() > 8 
                ? "****" + mpesaConfig.getApiKey().substring(mpesaConfig.getApiKey().length() - 4) 
                : "****";
            log.debug("Using API key: {}", maskedKey);
        } else {
            log.error("MPESA API key is not configured!");
            return Mono.error(new RuntimeException("MPESA API key is not configured"));
        }
        
        // Create a new WebClient instance for this request to ensure fresh headers
        WebClient client = WebClient.builder()
                .baseUrl(mpesaConfig.getApiBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + mpesaConfig.getApiKey())
                .build();
        
        return client.post()
                .uri("/api/v1/transactions")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                    log.error("MPESA API error: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                        .defaultIfEmpty("No error details")
                        .flatMap(errorBody -> {
                            log.error("Error details: {}", errorBody);
                            return Mono.error(new RuntimeException("MPESA API error: " + response.statusCode() + " - " + errorBody));
                        });
                })
                .bodyToMono(new ParameterizedTypeReference<MpesaApiResponse<MpesaPaymentResponse>>() {})
                .flatMap(apiResponse -> {
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        MpesaPaymentResponse paymentResponse = apiResponse.getData();
                        log.info("MPESA payment initiated successfully. Transaction ID: {}", paymentResponse.getTransactionId());
                        return Mono.just(paymentResponse);
                    } else {
                        String errorMsg = "Failed to initiate payment: " + 
                            (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error");
                        log.error(errorMsg);
                        return Mono.error(new RuntimeException(errorMsg));
                    }
                })
                .doOnSuccess(response -> 
                    log.info("MPESA payment initiated. Transaction ID: {}", response.getTransactionId()))
                .doOnError(error -> 
                    log.error("Error initiating MPESA payment: {}", error.getMessage(), error));
    }

   
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

    
    @Retryable(
        value = { WebClientResponseException.class },
        maxAttemptsExpression = "${mpesa.max-retries:3}",
        backoff = @Backoff(delayExpression = "${mpesa.retry-delay:1000}")
    )
    public Mono<MpesaPaymentResponse> queryTransactionStatus(String transactionId) {
        log.info("Querying status for transaction: {}", transactionId);
        
        return webClient.get()
                .uri("/api/v1/transactions/status/{transactionId}" + transactionId)
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

    @Transactional
    private void handleSuccessfulPayment(MpesaWebhookRequest webhookRequest) {
        String transactionId = webhookRequest.getTransactionId();
        String reference = webhookRequest.getThirdPartyRef();
        
        log.info("Processing successful payment for transaction: {}, reference: {}", transactionId, reference);
        
        try {
            // 1. Find the order by ID (assuming reference is the order ID)
            Long orderId = Long.parseLong(reference.replaceAll("[^0-9]", ""));
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + reference));
            
            // Update order status to CONFIRMED (next status after payment)
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);
            
            log.info("Updated order {} status to CONFIRMED and payment to PAID for transaction: {}", 
                    order.getOrderId(), transactionId);
            
            // 2. Send confirmation to customer
            try {
                notificationService.sendPaymentConfirmation(
                    order.getCustomer().getEmail(),
                    order.getOrderId(),
                    order.getSubTotal() + order.getDeliveryFee() - order.getDiscountAmount(),
                    transactionId
                );
                log.info("Sent payment confirmation to customer for order: {}", order.getOrderId());
            } catch (Exception e) {
                log.error("Failed to send payment confirmation for order: {}", order.getOrderId(), e);
            }
            
            // 3. Trigger any post-payment actions
            try {
                // Notify restaurant about new order
                notificationService.sendEmail(
                    order.getRestaurant().getEmail(),
                    "New Order #" + order.getOrderId(),
                    "new_order",
                    Map.of(
                        "orderId", order.getOrderId(),
                        "customerName", order.getCustomer().getFullNames(),
                        "orderItems", order.getOrderItems(),
                        "totalAmount", order.getSubTotal() + order.getDeliveryFee() - order.getDiscountAmount()
                    )
                );
                log.info("Notified restaurant about new order: {}", order.getOrderId());
            } catch (Exception e) {
                log.error("Failed to notify restaurant about new order: {}", order.getOrderId(), e);
            }
            
        } catch (NumberFormatException e) {
            log.error("Invalid order reference format: {}", reference, e);
        } catch (ResourceNotFoundException e) {
            log.error("Failed to process successful payment - order not found for reference: {}", reference, e);
        } catch (Exception e) {
            log.error("Error processing successful payment for transaction: {}", transactionId, e);
        }
    }

    @Transactional
    private void handleFailedPayment(MpesaWebhookRequest webhookRequest) {
        String transactionId = webhookRequest.getTransactionId();
        String reference = webhookRequest.getThirdPartyRef();
        
        log.warn("Processing failed payment for transaction: {}, reference: {}", transactionId, reference);
        
        try {
            // 1. Find the order by ID (assuming reference is the order ID)
            Long orderId = Long.parseLong(reference.replaceAll("[^0-9]", ""));
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + reference));
            
            // Update order status to CANCELLED and payment status to FAILED
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            
            log.warn("Updated order {} status to CANCELLED and payment to FAILED for transaction: {}", 
                    order.getOrderId(), transactionId);
            
            // 2. Notify customer about payment failure
            try {
                notificationService.sendEmail(
                    order.getCustomer().getEmail(),
                    "Payment Failed - Order #" + order.getOrderId(),
                    "payment_failed",
                    Map.of(
                        "orderId", order.getOrderId(),
                        "customerName", order.getCustomer().getFullNames(),
                        "amount", order.getSubTotal() + order.getDeliveryFee() - order.getDiscountAmount(),
                        "transactionId", transactionId,
                        "failureReason", webhookRequest.getDescription() != null ? 
                                       webhookRequest.getDescription() : "Payment was declined or failed"
                    )
                );
                log.info("Sent payment failure notification to customer for order: {}", order.getOrderId());
            } catch (Exception e) {
                log.error("Failed to send payment failure notification for order: {}", order.getOrderId(), e);
            }
            
            // 3. Log the failure for reporting
            log.warn("Payment failed for order {} - Transaction: {}, Reason: {}", 
                    order.getOrderId(), transactionId, 
                    webhookRequest.getDescription() != null ? webhookRequest.getDescription() : "Unknown reason");
                    
        } catch (NumberFormatException e) {
            log.error("Invalid order reference format: {}", reference, e);
        } catch (ResourceNotFoundException e) {
            log.error("Failed to process failed payment - order not found for reference: {}", reference, e);
        } catch (Exception e) {
            log.error("Error processing failed payment for transaction: {}", transactionId, e);
        }
    }

    @Transactional
    private void handlePendingPayment(MpesaWebhookRequest webhookRequest) {
        String transactionId = webhookRequest.getTransactionId();
        String reference = webhookRequest.getThirdPartyRef();
        
        log.info("Processing pending payment for transaction: {}, reference: {}", transactionId, reference);
        
        try {
            // 1. Find the order by ID (assuming reference is the order ID)
            Long orderId = Long.parseLong(reference.replaceAll("[^0-9]", ""));
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + reference));
            
            // Update payment status to PENDING (keep order status as is)
            order.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(order);
            
            log.info("Updated payment status to PENDING for order: {} and transaction: {}", 
                    order.getOrderId(), transactionId);
            
            // 2. Schedule a status check if needed (e.g., after 5 minutes)
            schedulePaymentStatusCheck(orderId, transactionId, 5);
            
            // Notify customer about pending payment
            try {
                notificationService.sendEmail(
                    order.getCustomer().getEmail(),
                    "Payment Processing - Order #" + order.getOrderId(),
                    "payment_pending",
                    Map.of(
                        "orderId", order.getOrderId(),
                        "customerName", order.getCustomer().getFullNames(),
                        "amount", order.getSubTotal() + order.getDeliveryFee() - order.getDiscountAmount(),
                        "transactionId", transactionId
                    )
                );
                log.info("Sent payment pending notification to customer for order: {}", order.getOrderId());
            } catch (Exception e) {
                log.error("Failed to send payment pending notification for order: {}", order.getOrderId(), e);
            }
            
        } catch (NumberFormatException e) {
            log.error("Invalid order reference format: {}", reference, e);
        } catch (ResourceNotFoundException e) {
            log.error("Failed to process pending payment - order not found for reference: {}", reference, e);
        } catch (Exception e) {
            log.error("Error processing pending payment for transaction: {}", transactionId, e);
        }
    }
    
    @Value("${mpesa.payment.status-check.retry-count:3}")
    private int maxRetryCount;
    
    @Value("${mpesa.payment.status-check.initial-delay:5}")
    private int initialDelayMinutes;
    
    @Value("${mpesa.payment.status-check.backoff-multiplier:2}")
    private int backoffMultiplier;
    
    private final Map<Long, Integer> retryCountMap = new ConcurrentHashMap<>();
    
    @Async
    protected void schedulePaymentStatusCheck(Long orderId, String transactionId, int delayInMinutes) {
        try {
            log.info("Scheduling payment status check for order: {}, transaction: {} in {} minutes", 
                    orderId, transactionId, delayInMinutes);
            
            // Calculate the next delay with exponential backoff
            int currentRetry = retryCountMap.getOrDefault(orderId, 0);
            long delay = (long) (delayInMinutes * Math.pow(backoffMultiplier, currentRetry)) * 60 * 1000; // Convert to milliseconds
            
            // Schedule the task
            taskScheduler.schedule(
                () -> checkPaymentStatus(orderId, transactionId, currentRetry + 1),
                Instant.now().plusMillis(delay)
            );
            
            log.debug("Scheduled payment status check for order: {} in {} ms (attempt #{})", 
                    orderId, delay, currentRetry + 1);
                    
        } catch (Exception e) {
            log.error("Error scheduling payment status check for order: {}, transaction: {}", 
                    orderId, transactionId, e);
        }
    }
    
    @Async
    protected void checkPaymentStatus(Long orderId, String transactionId, int attemptNumber) {
        try {
            log.info("Checking payment status for order: {}, transaction: {} (attempt {}/{})", 
                    orderId, transactionId, attemptNumber, maxRetryCount);
            
            // 1. Query the MPESA API for transaction status
            MpesaTransactionStatus status = queryTransactionStatusInternal(transactionId);
            
            if (status != null && status.isSuccessful()) {
                // Payment is now successful
                log.info("Payment for order: {} is now SUCCESSFUL", orderId);
                
                // Process the successful payment
                Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
                
                order.setOrderStatus(OrderStatus.CONFIRMED);
                order.setPaymentStatus(PaymentStatus.PAID);
                orderRepository.save(order);
                
                // Send confirmation to customer
                sendPaymentConfirmation(order, transactionId);
                
                // Clean up retry counter
                retryCountMap.remove(orderId);
                return;
            }
            
            // If we've reached max retries, mark as failed
            if (attemptNumber >= maxRetryCount) {
                log.warn("Max retry attempts ({}) reached for order: {}. Marking payment as failed.", 
                        maxRetryCount, orderId);
                
                Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
                
                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                
                // Send failure notification
                sendPaymentFailureNotification(order, transactionId, "Payment verification timeout");
                
                // Clean up retry counter
                retryCountMap.remove(orderId);
                return;
            }
            
            // Update retry count and reschedule
            retryCountMap.put(orderId, attemptNumber);
            schedulePaymentStatusCheck(orderId, transactionId, initialDelayMinutes);
            
        } catch (Exception e) {
            log.error("Error checking payment status for order: {}, transaction: {}", 
                    orderId, transactionId, e);
            
            // If we haven't reached max retries, reschedule
            if (attemptNumber < maxRetryCount) {
                retryCountMap.put(orderId, attemptNumber);
                schedulePaymentStatusCheck(orderId, transactionId, initialDelayMinutes);
            } else {
                log.error("Max retries reached for order: {}. Giving up.", orderId);
                retryCountMap.remove(orderId);
            }
        }
    }
    
    private MpesaTransactionStatus queryTransactionStatusInternal(String transactionId) {
        try {
            // Call MPESA API to get transaction status
            return webClient.get()
                .uri("/api/v1/transactions/{transactionId}/status", transactionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mpesaConfig.getApiKey())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> {
                    log.error("Error querying transaction status: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Failed to query transaction status: " + response.statusCode()));
                })
                .bodyToMono(MpesaTransactionStatus.class)
                .block();
        } catch (Exception e) {
            log.error("Error querying MPESA transaction status for: {}", transactionId, e);
            return null;
        }
    }
    
    private void sendPaymentConfirmation(Order order, String transactionId) {
        try {
            notificationService.sendEmail(
                order.getCustomer().getEmail(),
                "Payment Confirmed - Order #" + order.getOrderId(),
                "payment_confirmed",
                Map.of(
                    "orderId", order.getOrderId(),
                    "customerName", order.getCustomer().getFullNames(),
                    "amount", order.getSubTotal() + order.getDeliveryFee() - order.getDiscountAmount(),
                    "transactionId", transactionId
                )
            );
            log.info("Sent payment confirmation for order: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send payment confirmation for order: {}", order.getOrderId(), e);
        }
    }
    
    private void sendPaymentFailureNotification(Order order, String transactionId, String reason) {
        try {
            notificationService.sendEmail(
                order.getCustomer().getEmail(),
                "Payment Failed - Order #" + order.getOrderId(),
                "payment_failed",
                Map.of(
                    "orderId", order.getOrderId(),
                    "customerName", order.getCustomer().getFullNames(),
                    "amount", order.getSubTotal() + order.getDeliveryFee() - order.getDiscountAmount(),
                    "transactionId", transactionId,
                    "failureReason", reason
                )
            );
            log.info("Sent payment failure notification for order: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send payment failure notification for order: {}", order.getOrderId(), e);
        }
    }
}
