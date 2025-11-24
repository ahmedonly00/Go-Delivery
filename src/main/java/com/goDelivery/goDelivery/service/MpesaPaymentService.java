package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.config.MpesaConfig;
import com.goDelivery.goDelivery.dtos.mpesa.*;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.MpesaTransaction;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.repository.MpesaTransactionRepository;
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
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaPaymentService {

    private final MpesaConfig mpesaConfig;
    private final WebClient webClient;
    private final OrderRepository orderRepository;
    private final MpesaTransactionRepository mpesaTransactionRepository;
    private final NotificationService notificationService;
    private final TaskScheduler taskScheduler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${mpesa.payment.status-check.retry-count:3}")
    private int maxRetryCount;
    
    @Value("${mpesa.payment.status-check.initial-delay:5}")
    private int initialDelayMinutes;
    
    @Value("${mpesa.payment.status-check.backoff-multiplier:2}")
    private int backoffMultiplier;
    
    private final Map<Long, Integer> retryCountMap = new ConcurrentHashMap<>();

    /**
     * Creates a new MpesaTransaction from the payment request
     */
    private MpesaTransaction createTransactionFromRequest(MpesaPaymentRequest request, String apiResponse) {
        MpesaTransaction transaction = new MpesaTransaction();
        transaction.setMsisdn(request.getFromMSISDN());
        transaction.setAmount(request.getAmount());
        transaction.setApiResponse(apiResponse);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        
        // If this is linked to an order, set the order
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        if (order.getPaymentStatus() == PaymentStatus.PENDING) {
            order.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(order);
        }

        transaction.setOrder(order);
        }
        
        return mpesaTransactionRepository.save(transaction);
    }
    
    /**
     * Updates an existing transaction with data from the payment response
     */
    private void updateTransactionFromResponse(MpesaTransaction transaction, MpesaPaymentResponse response) {
        if (response.getTransactionId() != null) {
            transaction.setTransactionId(response.getTransactionId());
        }
        
        if (response.getTransactionStatus() != null) {
            transaction.setStatus(mapStatus(response.getTransactionStatus()));
        }
        
        if (response.getMsisdn() != null) {
            transaction.setMsisdn(response.getMsisdn());
        }
        
        if (response.getAmount() != null) {
            transaction.setAmount(response.getAmount());
        }
        
        mpesaTransactionRepository.save(transaction);
    }
    
    /**
     * Maps MPESA status string to PaymentStatus enum
     */
    private PaymentStatus mapStatus(String status) {
        if (status == null) {
            return PaymentStatus.PENDING;
        }
        
        switch (status.toUpperCase()) {
            case "SUCCESS":
            case "COMPLETED":
                return PaymentStatus.PAID;
            case "FAILED":
            case "REJECTED":
                return PaymentStatus.FAILED;
            case "PENDING":
            default:
                return PaymentStatus.PENDING;
        }
    }

    @Retryable(
        value = { WebClientResponseException.class },
        maxAttemptsExpression = "${mpesa.max-retries:3}",
        backoff = @Backoff(delayExpression = "${mpesa.retry-delay:1000}", multiplier = 2)
    )
    @Transactional
    public Mono<MpesaPaymentResponse> initiatePayment(MpesaPaymentRequest request) {
        // Set default callback URL if not provided
        if (request.getCallback() == null || request.getCallback().isEmpty()) {
            request.setCallback(baseUrl + "/api/webhooks/mpesa-payment");
        }

        log.info("Initiating MPESA payment for MSISDN: {}", request.getFromMSISDN());
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
                .defaultHeader("x-api-key", mpesaConfig.getApiKey())
                .build();
        
        String url = String.format("%s?fromMSISDN=%s&amount=%s&callback=%s",
                mpesaConfig.getPaymentEndpoint(),
                request.getFromMSISDN(),
                request.getAmount(),
                request.getCallback() != null ? request.getCallback() : "");
                
        log.info("Sending payment request to: {}{}", mpesaConfig.getApiBaseUrl(), url);
        
        // Create and save the transaction record first
        MpesaTransaction transaction = createTransactionFromRequest(request, null);
        
        return client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.debug("Raw MPESA API response: {}", response))
                .flatMap(response -> {
                    try {
                        // Parse the response as a JsonNode to handle different response formats
                        JsonNode rootNode = objectMapper.readTree(response);
                        
                        // Check if the response has a data array with elements
                        if (rootNode.has("data") && rootNode.get("data").isArray() && rootNode.get("data").size() > 0) {
                            // If data array has elements, parse the first one
                            MpesaPaymentResponse paymentResponse = objectMapper.treeToValue(
                                rootNode.get("data").get(0), 
                                MpesaPaymentResponse.class
                            );
                            log.info("MPESA payment initiated successfully. Transaction ID: {}", 
                                paymentResponse.getTransactionId());
                            
                            // Generate a unique reference for this transaction if not already set
                            if (paymentResponse.getThirdPartyRef() == null || paymentResponse.getThirdPartyRef().isEmpty()) {
                                String thirdPartyRef = "REF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                                paymentResponse.setThirdPartyRef(thirdPartyRef);
                                transaction.setThirdPartyRef(thirdPartyRef);
                            }
                            
                            // Update transaction with response data
                            updateTransactionFromResponse(transaction, paymentResponse);
                            return Mono.just(paymentResponse);
                        } 
                        // If data array is empty but we have a success message, create a response
                        else if (rootNode.has("success") && rootNode.get("success").asBoolean()) {
                            log.info("MPESA payment initiated successfully but no transaction data returned");
                            
                            // Update transaction as pending
                            transaction.setStatus(PaymentStatus.PENDING);
                            transaction.setTransactionId("PENDING_" + UUID.randomUUID().toString());
                            mpesaTransactionRepository.save(transaction);
                            
                            // Generate a unique reference for this transaction
                            String ref = "REF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                            transaction.setThirdPartyRef(ref);
                            mpesaTransactionRepository.save(transaction);
                            
                            MpesaPaymentResponse emptyResponse = new MpesaPaymentResponse();
                            emptyResponse.setTransactionId(transaction.getTransactionId());
                            emptyResponse.setTransactionStatus("PENDING");
                            emptyResponse.setThirdPartyRef(ref);
                            return Mono.just(emptyResponse);
                        } 
                        // If we have an error message
                        else if (rootNode.has("error")) {
                            String errorMsg = rootNode.has("message") ? 
                                rootNode.get("message").asText() : "Unknown error from MPESA API";
                            log.error("MPESA API returned an error: {}", errorMsg);
                            return Mono.error(new RuntimeException(errorMsg));
                        } 
                        // If we can't determine the response format
                        else {
                            log.error("Unexpected response format from MPESA API: {}", response);
                            return Mono.error(new RuntimeException("Unexpected response format from MPESA API"));
                        }
                    } catch (Exception e) {
                        log.error("Error parsing MPESA API response: {}", e.getMessage(), e);
                        
                        // Update transaction with error
                        transaction.setStatus(PaymentStatus.FAILED);
                        transaction.setDescription("Error parsing MPESA API response: " + e.getMessage());
                        mpesaTransactionRepository.save(transaction);
                        
                        return Mono.error(new RuntimeException("Failed to parse MPESA API response: " + e.getMessage()));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error processing MPESA payment: {}", e.getMessage(), e);
                    
                    // Update transaction with error
                    transaction.setStatus(PaymentStatus.FAILED);
                    transaction.setDescription("Error processing MPESA payment: " + e.getMessage());
                    mpesaTransactionRepository.save(transaction);
                    
                    return Mono.error(new RuntimeException("Failed to process MPESA payment: " + e.getMessage()));
                })
                .doOnSuccess(response -> 
                    log.info("MPESA payment initiated. Transaction ID: {}", 
                        response.getTransactionId() != null ? response.getTransactionId() : "N/A"))
                .doOnError(error -> 
                    log.error("Error initiating MPESA payment: {}", error.getMessage(), error));
    }


    @Async
    public void processWebhook(String payload, String signature) {
        try {
            // Parse the payload
            JsonNode rootNode = objectMapper.readTree(payload);
            String transactionId = rootNode.path("transactionId").asText();
            
            if (!StringUtils.hasText(transactionId)) {
                log.error("No transaction ID in webhook payload");
                return;
            }
            
            log.info("Processing MPESA webhook for transaction: {}", transactionId);
            
            // Find the transaction
            MpesaTransaction transaction = mpesaTransactionRepository.findByTransactionId(transactionId)
                    .orElseGet(() -> {
                        log.warn("Transaction not found for ID: {}. Creating new transaction record.", transactionId);
                        return createTransactionFromWebhook(rootNode);
                    });
            
            // Save the raw payload
            transaction.setCallbackPayload(payload);
            
            // Validate signature if required
            if (mpesaConfig.isWebhookSignatureRequired() && !validateWebhookSignature(payload, signature)) {
                log.warn("Invalid webhook signature for transaction: {}", transactionId);
                transaction.setStatus(PaymentStatus.FAILED);
                transaction.setDescription("Invalid webhook signature");
                mpesaTransactionRepository.save(transaction);
                return;
            }
            
            // Update transaction status based on webhook payload
            updateTransactionFromWebhook(transaction, rootNode);
            
            // Save the updated transaction
            mpesaTransactionRepository.save(transaction);
            log.info("Successfully processed webhook for transaction: {}", transactionId);
            
            // If the transaction is completed and has an associated order, we can clean up retry tracking
            if ((transaction.getStatus() == PaymentStatus.PAID || 
                 transaction.getStatus() == PaymentStatus.FAILED) &&
                 transaction.getOrder() != null) {
                retryCountMap.remove(transaction.getOrder().getOrderId());
            }
            
        } catch (Exception e) {
            log.error("Error processing webhook: " + e.getMessage(), e);
        }
    }
    
    private MpesaTransaction createTransactionFromWebhook(JsonNode webhookData) {
        MpesaTransaction transaction = new MpesaTransaction();
        String transactionId = webhookData.path("transactionId").asText();
        String thirdPartyRef = webhookData.path("thirdPartyRef").asText();
        
        transaction.setTransactionId(transactionId);
        transaction.setAmount(webhookData.path("amount").floatValue());
        transaction.setMsisdn(webhookData.path("msisdn").asText());
        transaction.setThirdPartyRef(thirdPartyRef);
        
        // Set status based on webhook data
        String status = webhookData.path("transactionStatus").asText("PENDING");
        transaction.setStatus(mapStatus(status));
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        
        // Set API response (the webhook payload)
        try {
            transaction.setApiResponse(webhookData.toString());
        } catch (Exception e) {
            log.warn("Failed to serialize webhook data to JSON", e);
        }
        
        // Try to find and link the order if thirdPartyRef is an order ID
        if (StringUtils.hasText(thirdPartyRef)) {
            try {
                // Assuming thirdPartyRef contains the order ID or can be used to find the order
                Order order = orderRepository.findByOrderNumber(thirdPartyRef)
                    .orElseGet(() -> {
                        try {
                            // Try parsing as order ID if not found by order number
                            Long orderId = Long.parseLong(thirdPartyRef);
                            return orderRepository.findByOrderId(orderId).orElse(null);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    });
                
                if (order != null) {
                    transaction.setOrder(order);
                    log.info("Linked transaction {} to order {}", transactionId, order.getOrderId());
                }
            } catch (Exception e) {
                log.error("Error linking order to transaction: " + transactionId, e);
            }
        }
        
        // Set completedAt if this is a final status
        if (transaction.getStatus() == PaymentStatus.PAID || 
            transaction.getStatus() == PaymentStatus.FAILED) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        
        return mpesaTransactionRepository.save(transaction);
    }
    
    private void updateTransactionFromWebhook(MpesaTransaction transaction, JsonNode webhookData) {
        boolean statusChanged = false;
        
        // Update status if provided in webhook
        if (webhookData.has("transactionStatus")) {
            String status = webhookData.get("transactionStatus").asText();
            PaymentStatus newStatus = mapStatus(status);
            statusChanged = transaction.getStatus() != newStatus;
            transaction.setStatus(newStatus);
        }
        
        // Update amount if provided
        if (webhookData.has("amount")) {
            transaction.setAmount(webhookData.get("amount").floatValue());
        }
        
        // Update description if provided
        if (webhookData.has("description")) {
            transaction.setDescription(webhookData.get("description").asText());
        }
        
        // Update MSISDN if provided
        if (webhookData.has("msisdn")) {
            transaction.setMsisdn(webhookData.get("msisdn").asText());
        }
        
        // Update thirdPartyRef if provided
        if (webhookData.has("thirdPartyRef")) {
            String thirdPartyRef = webhookData.get("thirdPartyRef").asText();
            transaction.setThirdPartyRef(thirdPartyRef);
            
            // Try to link order if not already linked and we have a thirdPartyRef
            if (transaction.getOrder() == null && StringUtils.hasText(thirdPartyRef)) {
                try {
                    // Try to find order by order number or ID
                    Order order = orderRepository.findByOrderNumber(thirdPartyRef)
                        .orElseGet(() -> {
                            try {
                                Long orderId = Long.parseLong(thirdPartyRef);
                                return orderRepository.findByOrderId(orderId).orElse(null);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        });
                    
                    if (order != null) {
                        transaction.setOrder(order);
                        log.info("Linked transaction {} to order {}", transaction.getTransactionId(), order.getOrderId());
                    }
                } catch (Exception e) {
                    log.error("Error linking order to transaction: " + transaction.getTransactionId(), e);
                }
            }
        }
        
        // Update API response with latest webhook data
        try {
            transaction.setApiResponse(webhookData.toString());
        } catch (Exception e) {
            log.warn("Failed to serialize webhook data to JSON", e);
        }
        
        // Update timestamps
        transaction.setUpdatedAt(LocalDateTime.now());
        
        // Set completedAt if this is a final status and it's a new status
        if (statusChanged && (transaction.getStatus() == PaymentStatus.PAID || 
                             transaction.getStatus() == PaymentStatus.FAILED)) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        
        // Update other fields if provided
        if (webhookData.has("amount")) {
            transaction.setAmount(webhookData.get("amount").floatValue());
        }
        
        if (webhookData.has("description")) {
            transaction.setDescription(webhookData.get("description").asText());
        }
        
        // Update the last updated timestamp
        transaction.setUpdatedAt(LocalDateTime.now());
    }
    
    private boolean validateWebhookSignature(String payload, String signature) {
        // Skip validation if not required
        if (!mpesaConfig.isWebhookSignatureRequired()) {
            return true;
        }
        
        // If signature is required but not provided
        if (!StringUtils.hasText(signature)) {
            log.warn("No signature provided for webhook validation");
            return false;
        }
        
        try {
            String secret = mpesaConfig.getWebhookSecret();
            if (!StringUtils.hasText(secret)) {
                log.warn("Webhook secret is not configured");
                return false;
            }
            
            // Generate HMAC-SHA256 signature
            String algorithm = mpesaConfig.getWebhookSignatureAlgorithm();
            Mac hmac = Mac.getInstance(algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
            hmac.init(secretKey);
            
            byte[] signatureBytes = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getEncoder().encodeToString(signatureBytes);
            
            // Compare signatures in constant time to prevent timing attacks
            return MessageDigest.isEqual(computedSignature.getBytes(StandardCharsets.UTF_8), 
                                       signature.getBytes(StandardCharsets.UTF_8));
            
        } catch (Exception e) {
            log.error("Error validating webhook signature", e);
            return false;
        }
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
                .header(HttpHeaders.AUTHORIZATION, "x-api-key " + mpesaConfig.getApiKey())
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
            Order order = orderRepository.findByOrderId(orderId)
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
            Order order = orderRepository.findByOrderId(orderId)
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
            Order order = orderRepository.findByOrderId(orderId)
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
                Order order = orderRepository.findByOrderId(orderId)
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
                
                Order order = orderRepository.findByOrderId(orderId)
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
                .header(HttpHeaders.AUTHORIZATION, "x-api-key " + mpesaConfig.getApiKey())
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