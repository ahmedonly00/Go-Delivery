package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.config.MomoConfig;
import com.goDelivery.goDelivery.config.OrderConfig;
import com.goDelivery.goDelivery.dtos.momo.MomoPaymentRequest;
import com.goDelivery.goDelivery.dtos.momo.MomoPaymentResponse;
import com.goDelivery.goDelivery.dtos.momo.MomoTransactionStatus;
import com.goDelivery.goDelivery.dtos.momo.MomoWebhookRequest;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementRequest;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementResponse;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementStatusResponse;
import com.goDelivery.goDelivery.model.MomoTransaction;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.Payment;
import com.goDelivery.goDelivery.Enum.TransactionType;
import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.Enum.TransactionStatus;
import com.goDelivery.goDelivery.repository.MomoTransactionRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MomoService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MomoConfig momoConfig;
    
    @Autowired
    private MomoTransactionRepository momoTransactionRepository;
    
    @Autowired
    private CashierService cashierService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private MomoHealthCheckService momoHealthCheckService;
    
    @Autowired
    private OrderConfig orderConfig;
    
    @Lazy
    @Autowired
    private DisbursementService disbursementService;

    @Value("${app.payment.auto-disbursement.enabled:true}")
    private boolean autoDisbursementEnabled;

    /**
     * Request payment from a customer's mobile money account
     */
    public MomoPaymentResponse requestPayment(MomoPaymentRequest request) {
        // Check if MoMo service is available before proceeding
        if (!momoHealthCheckService.isMomoServiceAvailable()) {
            throw new RuntimeException("Payment service is temporarily unavailable. Please try again later or use an alternative payment method.");
        }
        
        log.info("Received MoMo payment request for external ID: {}", request.getExternalId());
        
        // Check if MoMo is configured
        if (momoConfig.getUsername() == null || momoConfig.getUsername().isBlank()) {
            log.warn("MoMo payment is not configured. Please provide MoMo credentials in application.properties");
            return MomoPaymentResponse.error("MoMo payment is not configured");
        }
        
        // Check for duplicate external ID
        if (momoTransactionRepository.existsByExternalId(request.getExternalId())) {
            throw new RuntimeException("Transaction with this external ID already exists");
        }

        // Create and save transaction
        MomoTransaction transaction = new MomoTransaction();
        transaction.setExternalId(request.getExternalId());
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setMsisdn(request.getMsisdn());
        transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
        transaction.setType(TransactionType.COLLECTION);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCallbackUrl(request.getCallback());
        
        try {
            // Generate JWT auth token
            String authToken = generateAuthToken();
            
            if (authToken == null) {
                throw new RuntimeException("Failed to authenticate with MoMo API: No token received");
            }
            
            // Prepare request headers for payment request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            
            // Format the phone number to remove any non-digit characters
            String formattedMsisdn = request.getMsisdn().replaceAll("[^0-9]", "");
            
            // Prepare request body according to MoMo API documentation
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("externalId", transaction.getExternalId());
            requestBody.put("msisdn", formattedMsisdn);
            requestBody.put("amount", transaction.getAmount().doubleValue());
            requestBody.put("callback", transaction.getCallbackUrl());
            requestBody.put("payerMessageTitle", request.getPayerMessageTitle());
            requestBody.put("payerMessageDescription", request.getPayerMessageDescription());
            
            // Make API call to request payment
            String apiUrl = momoConfig.getCollectionUrl();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("Sending payment request to MoMo API: {}", apiUrl);
            log.debug("Request body: {}", requestBody);
            
            // Make the payment request
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            // Save transaction with updated details
            transaction.setApiResponse(response.getBody() != null ? response.getBody().toString() : "");
            transaction.setStatusCode(response.getStatusCode().value());
            momoTransactionRepository.save(transaction);
            
            log.info("MoMo API response status: {}", response.getStatusCode());
            log.debug("MoMo API response body: {}", response.getBody());
            
            // Start polling for status updates if request was successful
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // Update transaction with response data
                if (responseBody.containsKey("referenceId")) {
                    transaction.setReferenceId((String) responseBody.get("referenceId"));
                }
                
                momoTransactionRepository.save(transaction);
                
                // Start polling
                pollTransactionStatus(transaction.getReferenceId());
                
                return MomoPaymentResponse.success(
                    transaction.getReferenceId(), 
                    transaction.getExternalId(), 
                    request.getAmount().floatValue()
                );
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorReason("Failed to initiate payment: " + response.getStatusCode());
                momoTransactionRepository.save(transaction);
                throw new RuntimeException("Failed to initiate payment: " + response.getBody());
            }
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Handle connection timeout specifically
            if (e.getCause() instanceof java.net.ConnectException || 
                e.getCause() instanceof java.net.SocketTimeoutException) {
                log.error("MoMo API is not accessible. Connection timeout to: {}", momoConfig.getBaseUrl());
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorReason("Payment service is temporarily unavailable. Please try again later.");
                momoTransactionRepository.save(transaction);
                throw new RuntimeException("Payment service is temporarily unavailable. Please try again later.");
            } else {
                log.error("Error requesting payment from MoMo API", e);
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorReason(e.getMessage());
                momoTransactionRepository.save(transaction);
                throw new RuntimeException("Error processing payment request", e);
            }
        } catch (Exception e) {
            log.error("Error requesting payment from MoMo API", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorReason(e.getMessage());
            momoTransactionRepository.save(transaction);
            throw new RuntimeException("Error processing payment request", e);
        }
    }

    /**
     * Poll for collection-disbursement status updates
     */
    @Async
    public CompletableFuture<Void> pollCollectionDisbursementStatus(String referenceId) {
        return CompletableFuture.runAsync(() -> {
            int maxAttempts = 120; // Poll for up to 60 minutes (120 * 30 seconds)
            int attempt = 0;
            long delayMs = 30000; // 30 seconds between polls (as per documentation)

            log.info("Starting collection-disbursement status polling for reference: {}", referenceId);

            while (attempt < maxAttempts) {
                try {
                    // Check if collection is successful
                    DisbursementStatusResponse statusResponse = checkDisbursementStatus(referenceId);
                    String status = statusResponse.getStatus();
                    
                    log.debug("Collection-disbursement status check for {}: {} (attempt {}/{})", 
                        referenceId, status, attempt + 1, maxAttempts);
                    
                    // If collection is completed (successful or failed), stop polling
                    if (isFinalStatus(status)) {
                        log.info("Collection-disbursement {} completed with status: {}", 
                                referenceId, status);
                        return;
                    }

                    // Wait before next poll
                    attempt++;
                    if (attempt < maxAttempts) {
                        log.debug("Will check collection-disbursement status again in {}ms (attempt {}/{})", 
                                 delayMs, attempt + 1, maxAttempts);
                        Thread.sleep(delayMs);
                    }
                    
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Collection-disbursement polling interrupted for reference: {}", referenceId);
                    throw new RuntimeException("Polling interrupted", ie);
                } catch (Exception e) {
                    log.error("Error checking collection-disbursement status for reference: {} (attempt {})", 
                        referenceId, attempt, e);
                    attempt++;
                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Polling interrupted during error handling", ie);
                        }
                    }
                }
            }
            
            log.warn("Max polling attempts reached for collection-disbursement: {}", referenceId);
        });
    }

    /**
     * Initiate a disbursement transaction
     * @param request the disbursement request
     * @return the response
     */
    public CollectionDisbursementResponse initiateCollectionDisbursement(CollectionDisbursementRequest request) {
        try {
            String authToken = generateAuthToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            
            // Format the collection MSISDN to remove any non-digit characters
            request.setCollectionMsisdn(formatMsisdn(request.getCollectionMsisdn()));
            
            // Format all recipient MSISDNs
            request.getDisbursementRecipients().forEach(recipient -> 
                recipient.setMsisdn(formatMsisdn(recipient.getMsisdn())));
            
            HttpEntity<CollectionDisbursementRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Initiating collection-disbursement request to MoMo API");
            ResponseEntity<CollectionDisbursementResponse> response = restTemplate.exchange(
                momoConfig.getBaseUrl() + "/disbursement/collection-disbursement",
                HttpMethod.POST,
                entity,
                CollectionDisbursementResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Start polling for collection status
                pollCollectionDisbursementStatus(response.getBody().getReferenceId());
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to initiate collection-disbursement: " + 
                    response.getStatusCode().value() + " - " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Error processing collection-disbursement", e);
            throw new RuntimeException("Failed to process collection-disbursement: " + e.getMessage());
        }
    }
    public DisbursementStatusResponse checkDisbursementStatus(String referenceId) {
        try {
            String authToken = generateAuthToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            
            String url = String.format("%s/disbursement/status/%s", 
                momoConfig.getBaseUrl(), referenceId);
                
            ResponseEntity<DisbursementStatusResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DisbursementStatusResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to check disbursement status: " + 
                    response.getStatusCode().value() + " - " + (response.getBody() != null ? response.getBody().toString() : "No response body"));
            }
        } catch (Exception e) {
            log.error("Error checking disbursement status", e);
            throw new RuntimeException("Failed to check disbursement status: " + e.getMessage());
        }
    }
    private String formatMsisdn(String msisdn) {
        // Remove any non-digit characters and ensure it starts with country code
        String digits = msisdn.replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) {
            return "250" + digits.substring(1); // Assuming Rwanda as default
        }
        return digits;
    }

    /**
     * Check the status of a transaction
     */
    public MomoTransactionStatus checkTransactionStatus(String referenceId) {
        return momoTransactionRepository.findByReferenceId(referenceId)
                .map(this::mapToTransactionStatus)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + referenceId));
    }

    /**
     * Handle webhook callbacks from MoMo
     */
    public void handleWebhook(MomoWebhookRequest webhookRequest) {
        momoTransactionRepository.findByReferenceId(webhookRequest.getReferenceId())
                .ifPresent(transaction -> {
                    // Update transaction status
                    transaction.setStatus(mapToTransactionStatus(webhookRequest.getStatus()));
                    transaction.setFinancialTransactionId(webhookRequest.getFinancialTransactionId());
                    
                    if (webhookRequest.getReason() != null) {
                        transaction.setErrorReason(webhookRequest.getReason().getMessage());
                    }
                    
                    momoTransactionRepository.save(transaction);
                    log.info("Updated transaction status for {} to {}", 
                            transaction.getReferenceId(), 
                            transaction.getStatus());
                    
                    // Trigger any business logic based on the transaction status
                    handleTransactionUpdate(transaction);
                });
    }

    /**
     * Poll for transaction status updates
     */
    @Async
    public CompletableFuture<Void> pollTransactionStatus(String referenceId) {
        return momoTransactionRepository.findByReferenceId(referenceId)
            .map(transaction -> CompletableFuture.runAsync(() -> {
                int maxAttempts = 120; // Poll for up to 60 minutes (120 * 30 seconds)
                int attempt = 0;
                long delayMs = 30000; // 30 seconds between polls (as per documentation)

                log.info("Starting status polling for transaction: {}", referenceId);

                while (attempt < maxAttempts) {
                    try {
                        // Generate new auth token for each attempt
                        String authToken = generateAuthToken();
                        
                        if (authToken == null) {
                            log.error("Failed to get auth token for polling attempt {}", attempt);
                            break;
                        }
                        
                        // Prepare headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Authorization", "Bearer " + authToken);
                        
                        // Build request URL
                        String statusUrl = momoConfig.getCollectionStatusUrl(transaction.getReferenceId());
                        
                        HttpEntity<Void> entity = new HttpEntity<>(headers);
                        
                        log.debug("Checking payment status for reference: {} (attempt {}/{})", 
                            referenceId, attempt + 1, maxAttempts);
                        
                        // Make API call to check status
                        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                            statusUrl,
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<Map<String, Object>>() {}
                        );

                        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                            Map<String, Object> statusResponse = response.getBody();
                            String status = (String) statusResponse.get("status");
                            
                            log.debug("Status check response for {}: {}", referenceId, status);
                            
                            // Update transaction status based on response
                            updateTransactionStatus(transaction, status, statusResponse);
                            
                            // If transaction is completed (successful or failed), stop polling
                            if (isFinalStatus(status)) {
                                log.info("Transaction {} completed with status: {}", 
                                        referenceId, status);
                                return;
                            }
                        }

                        // Wait before next poll
                        attempt++;
                        if (attempt < maxAttempts) {
                            log.debug("Will check status again in {}ms (attempt {}/{})", 
                                     delayMs, attempt + 1, maxAttempts);
                            Thread.sleep(delayMs);
                        }
                        
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Polling interrupted for transaction: {}", referenceId);
                        throw new RuntimeException("Polling interrupted", ie);
                    } catch (Exception e) {
                        log.error("Error checking payment status for reference: {} (attempt {})", 
                            referenceId, attempt, e);
                        attempt++;
                        if (attempt < maxAttempts) {
                            try {
                                Thread.sleep(delayMs);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("Polling interrupted during error handling", ie);
                            }
                        }
                    }
                }
                
                // If we've exhausted all attempts
                if (transaction.getStatus() != TransactionStatus.SUCCESS) {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setErrorReason("Max polling attempts reached without final status");
                    momoTransactionRepository.save(transaction);
                    log.warn("Max polling attempts reached for transaction: {}", referenceId);
                    
                    // Trigger update handler for failed transaction
                    handleTransactionUpdate(transaction);
                }
            }))
            .orElse(CompletableFuture.completedFuture(null));
    }

    /**
     * Generate an authentication token from MoMo API using username and password
     */
    private String generateAuthToken() {
        // Check if credentials are provided
        if (momoConfig.getUsername() == null || momoConfig.getUsername().isBlank()) {
            log.warn("No MoMo username configured. Skipping authentication.");
            return null;
        }
        
        String authUrl = momoConfig.getAuthUrl();
        
        try {
            log.info("Attempting to authenticate with MoMo API");
            log.debug("Auth URL: {}", authUrl);
            log.debug("Username: {}", momoConfig.getUsername());
            
            // Create JSON request body with username and password
            Map<String, String> authRequest = new HashMap<>();
            authRequest.put("username", momoConfig.getUsername());
            authRequest.put("password", momoConfig.getPassword());
            
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity with JSON body
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(authRequest, headers);
            
            // Make the authentication request
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                authUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            log.info("Auth response status: {}", response.getStatusCode());
            
            // Check response status
            if (response.getStatusCode() != HttpStatus.OK) {
                String errorMsg = String.format(
                    "Authentication failed with status %s. Please check your credentials.", 
                    response.getStatusCode()
                );
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            // Get response body
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                log.error("Empty response body from authentication endpoint");
                throw new RuntimeException("Empty response from authentication endpoint");
            }
            
            log.debug("Auth response body keys: {}", responseBody.keySet());
            
            // Extract token - the API returns "token" field according to documentation
            String token = null;
            
            if (responseBody.containsKey("token")) {
                token = (String) responseBody.get("token");
            } else if (responseBody.containsKey("access_token")) {
                token = (String) responseBody.get("access_token");
            }
            
            if (token == null || token.isEmpty()) {
                log.error("No token found in response. Available keys: {}", responseBody.keySet());
                throw new RuntimeException("No token found in authentication response");
            }
            
            log.info("Successfully obtained authentication token");
            log.debug("Token prefix: {}...", token.substring(0, Math.min(20, token.length())));
            
            return token;
            
        } catch (HttpClientErrorException.Forbidden e) {
            String errorMsg = String.format("""
                Authentication failed with 403 Forbidden.
                URL: %s
                Username: %s
                
                Possible issues:
                1. Invalid username or password
                2. User account doesn't have required permissions (COLLECTION_REQUEST)
                3. Account may be locked or disabled
                
                Response: %s
                """, 
                authUrl, 
                momoConfig.getUsername(),
                e.getResponseBodyAsString()
            );
            log.error(errorMsg);
            throw new RuntimeException("Authentication failed: Invalid credentials or insufficient permissions", e);
            
        } catch (HttpClientErrorException e) {
            log.error("HTTP error during authentication: {} - {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Authentication request failed: " + e.getMessage(), e);
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Handle connection timeout specifically
            if (e.getCause() instanceof java.net.ConnectException || 
                e.getCause() instanceof java.net.SocketTimeoutException) {
                String errorMsg = String.format("""
                    Connection timeout to MoMo API.
                    URL: %s
                    The payment service appears to be down or unreachable.
                    Please check:
                    1. Network connectivity
                    2. Firewall settings
                    3. MoMo API service status
                    """, 
                    authUrl
                );
                log.error(errorMsg);
                throw new RuntimeException("Authentication request failed: Payment service is temporarily unavailable", e);
            } else {
                log.error("Error during authentication request to {}", authUrl, e);
                throw new RuntimeException("Authentication request failed: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Error during authentication request to {}", authUrl, e);
            throw new RuntimeException("Authentication request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Update transaction status based on API response
     */
    private void updateTransactionStatus(MomoTransaction transaction, String status, 
                                        Map<String, Object> statusResponse) {
        TransactionStatus newStatus = mapToTransactionStatus(status);
        transaction.setStatus(newStatus);
        
        // Update additional fields from the status response
        if (statusResponse != null) {
            if (statusResponse.containsKey("financialTransactionId")) {
                transaction.setFinancialTransactionId(
                    String.valueOf(statusResponse.get("financialTransactionId"))
                );
            }
            
            if (statusResponse.containsKey("reason")) {
                Object reason = statusResponse.get("reason");
                if (reason instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> reasonMap = (Map<String, Object>) reason;
                    if (reasonMap.containsKey("message")) {
                        transaction.setErrorReason(String.valueOf(reasonMap.get("message")));
                    }
                }
            }
            
            transaction.setApiResponse(statusResponse.toString());
        }
        
        momoTransactionRepository.save(transaction);
        log.info("Updated transaction {} status to {}", transaction.getReferenceId(), newStatus);
        
        // Trigger any business logic for status update
        handleTransactionUpdate(transaction);
    }
    
    /**
     * Check if status is final (no more polling needed)
     */
    private boolean isFinalStatus(String status) {
        return "SUCCESSFUL".equalsIgnoreCase(status) || 
               "FAILED".equalsIgnoreCase(status) || 
               "CANCELLED".equalsIgnoreCase(status);
    }
    
    /**
     * Handle transaction updates and trigger related business logic
     */
    private void handleTransactionUpdate(MomoTransaction transaction) {
        log.info("Handling transaction update for {} with status {}", 
                transaction.getReferenceId(), 
                transaction.getStatus());
        
        try {
            // 1. Update related payment status if payment exists
            if (transaction.getPayment() != null) {
                Payment payment = transaction.getPayment();
                payment.setPaymentStatus(mapToPaymentStatus(transaction.getStatus()));
                payment.setGateWayResponse("Transaction status: " + transaction.getStatus());
                
                if (transaction.getStatus() == TransactionStatus.SUCCESS) {
                    payment.setPaymentDate(LocalDate.now());
                    payment.setFailureReason(null);
                } else if (transaction.getStatus() == TransactionStatus.FAILED) {
                    payment.setFailureReason(transaction.getErrorReason());
                }
                
                paymentRepository.save(payment);
            }

            // 2. Update related order status if order exists
            if (transaction.getOrder() != null) {
                Order order = transaction.getOrder();
                
                if (transaction.getStatus() == TransactionStatus.SUCCESS) {
                    order.setPaymentStatus(PaymentStatus.PAID);
                    order.setOrderStatus(OrderStatus.CONFIRMED);
                    order.setOrderConfirmedAt(LocalDate.now());
                    log.info("Order {} confirmed after successful payment", order.getOrderId());
                    
                    // Process the order after successful payment
                    try {
                        int estimatedPrepTimeMinutes = orderConfig.getDefaultPreparationTimeMinutes();
                        log.debug("Using preparation time of {} minutes for order {}", 
                                estimatedPrepTimeMinutes, order.getOrderId());
                        
                        cashierService.acceptOrder(order.getOrderId(), estimatedPrepTimeMinutes);
                        log.info("Order {} successfully processed after payment", order.getOrderId());
                        
                        // NEW: Trigger automatic disbursement for all paid orders
                        if (autoDisbursementEnabled) {
                            log.info("Auto-disbursement enabled for order {}, triggering disbursement", 
                                    order.getOrderId());
                            try {
                                disbursementService.processOrderDisbursement(order);
                                log.info("Automatic disbursement initiated for order {}", order.getOrderId());
                            } catch (Exception e) {
                                log.error("Failed to initiate automatic disbursement for order {}: {}", 
                                        order.getOrderId(), e.getMessage(), e);
                                // Don't fail the order, just log the error
                            }
                        } else {
                            log.info("Auto-disbursement is disabled for order {}", 
                                    order.getOrderId());
                        }
                        
                    } catch (Exception e) {
                        log.error("Failed to process order {} after payment: {}", 
                                order.getOrderId(), e.getMessage(), e);
                    }
                    
                } else if (transaction.getStatus() == TransactionStatus.FAILED) {
                    order.setPaymentStatus(PaymentStatus.FAILED);
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    log.warn("Payment failed for order {}", order.getOrderId());
                }
                
                orderRepository.save(order);
            }

            // 3. Send appropriate notifications
            sendTransactionNotifications(transaction);

            // 4. Log the transaction update
            log.info("Transaction {} processed with status: {}", 
                    transaction.getReferenceId(), 
                    transaction.getStatus());

        } catch (Exception e) {
            log.error("Error handling transaction update for {}", transaction.getReferenceId(), e);
        }
    }

    /**
     * Map transaction status from MoMo to internal transaction status
     */
    private MomoTransactionStatus mapToTransactionStatus(MomoTransaction transaction) {
        MomoTransactionStatus status = new MomoTransactionStatus();
        status.setReferenceId(transaction.getReferenceId());
        status.setStatus(transaction.getStatus().name());
        status.setAmount(transaction.getAmount().floatValue());
        status.setCurrency(transaction.getCurrency());
        status.setFinancialTransactionId(transaction.getFinancialTransactionId());
        status.setExternalId(transaction.getExternalId());
        status.setErrorReason(transaction.getErrorReason());
        status.setTimestamp(transaction.getUpdatedAt());
        return status;
    }

    /**
     * Map string status to TransactionStatus enum
     */
    private TransactionStatus mapToTransactionStatus(String status) {
        try {
            return TransactionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown transaction status: {}, defaulting to FAILED", status);
            return TransactionStatus.FAILED;
        }
    }

    /**
     * Map TransactionStatus to PaymentStatus
     */
    private PaymentStatus mapToPaymentStatus(TransactionStatus status) {
        return switch (status) {
            case SUCCESS -> PaymentStatus.PAID;
            case FAILED -> PaymentStatus.FAILED;
            case PENDING -> PaymentStatus.PENDING;
            case CANCELLED -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }

    /**
     * Check if an order contains items from multiple restaurants
     */
    private boolean hasMultipleRestaurants(Order order) {
        if (order == null || order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return false;
        }
        
        long distinctRestaurantCount = order.getOrderItems().stream()
            .map(item -> item.getMenuItem().getRestaurant())
            .distinct()
            .count();
            
        return distinctRestaurantCount > 1;
    }

    /**
     * Send transaction notifications to customer
     */
    private void sendTransactionNotifications(MomoTransaction transaction) {
        if (transaction.getOrder() == null || transaction.getOrder().getCustomer() == null) {
            return;
        }

        String customerEmail = transaction.getOrder().getCustomer().getEmail();
        String orderId = transaction.getOrder().getOrderId().toString();
        
        switch (transaction.getStatus()) {
            case SUCCESS:
                notificationService.sendPaymentConfirmation(
                    customerEmail,
                    Long.parseLong(orderId),
                    transaction.getAmount().floatValue(),
                    transaction.getReferenceId()
                );
                break;
                
            case FAILED:
                Map<String, Object> failureData = new HashMap<>();
                failureData.put("orderId", orderId);
                failureData.put("error", transaction.getErrorReason());
                failureData.put("message", "Your payment failed. Please try again or contact support.");
                
                notificationService.sendEmail(
                    customerEmail,
                    "Payment Failed - Order #" + orderId,
                    "payment-failed",
                    failureData
                );
                break;
                
            case PENDING:
                Map<String, Object> pendingData = new HashMap<>();
                pendingData.put("orderId", orderId);
                pendingData.put("message", "Your payment is being processed. You will receive a confirmation once it's completed.");
                
                notificationService.sendEmail(
                    customerEmail,
                    "Payment Processing - Order #" + orderId,
                    "payment-pending",
                    pendingData
                );
                break;

            case CANCELLED:
                Map<String, Object> cancelledData = new HashMap<>();
                cancelledData.put("orderId", orderId);
                cancelledData.put("message", "Your payment has been cancelled. If this was unexpected, please contact support.");
                
                notificationService.sendEmail(
                    customerEmail,
                    "Payment Cancelled - Order #" + orderId,
                    "payment-cancelled",
                    cancelledData
                );
                break;
        }
    }
}