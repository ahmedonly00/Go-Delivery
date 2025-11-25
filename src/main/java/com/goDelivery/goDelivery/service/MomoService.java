package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.config.MomoConfig;
import com.goDelivery.goDelivery.dtos.momo.*;
import com.goDelivery.goDelivery.model.MomoTransaction;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.Payment;
import com.goDelivery.goDelivery.Enum.TransactionType;
import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.Enum.TransactionStatus;
import com.goDelivery.goDelivery.repository.MomoTransactionRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.config.OrderConfig;
import com.goDelivery.goDelivery.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
@RequiredArgsConstructor
public class MomoService {

    private final RestTemplate restTemplate;
    private final MomoConfig momoConfig;
    private final MomoTransactionRepository momoTransactionRepository;
    private final CashierService cashierService;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderConfig orderConfig;

    /**
     * Request payment from a customer's mobile money account
     */
    public MomoPaymentResponse requestPayment(MomoPaymentRequest request) {
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
            
            // Prepare request headers for payment request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            headers.set("X-Target-Environment", momoConfig.getEnvironment());
            headers.set("Ocp-Apim-Subscription-Key", momoConfig.getSubscriptionKey());
            headers.set("X-Reference-Id", transaction.getReferenceId());
            headers.set("X-Callback-Url", transaction.getCallbackUrl());
            
            // Format the phone number to remove any non-digit characters
            String formattedMsisdn = request.getMsisdn().replaceAll("[^0-9]", "");
            
            // Prepare request body according to MoMo API requirements
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", transaction.getAmount());
            requestBody.put("currency", "RWF");
            requestBody.put("externalId", transaction.getExternalId());
            requestBody.put("payer", Map.of(
                "partyIdType", "MSISDN",
                "partyId", formattedMsisdn
            ));
            requestBody.put("payerMessage", request.getPayerMessageTitle());
            requestBody.put("payeeNote", request.getPayerMessageDescription());
            
            // Make API call to request payment
            String apiUrl = momoConfig.getCollectionBaseUrl() + "/requesttopay";
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("Sending payment request to MoMo API: {}", apiUrl);
            log.debug("Request headers: {}", headers);
            log.debug("Request body: {}", requestBody);
            
            // Make the payment request
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // Save transaction with updated details
            transaction.setApiResponse(response.getBody());
            transaction.setStatusCode(response.getStatusCode().value());
            momoTransactionRepository.save(transaction);
            
            log.info("MoMo API response status: {}", response.getStatusCode());
            log.debug("MoMo API response body: {}", response.getBody());
            
            // Start polling for status updates
            if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                pollTransactionStatus(transaction.getReferenceId());
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorReason("Failed to initiate payment: " + response.getStatusCode());
                momoTransactionRepository.save(transaction);
                throw new RuntimeException("Failed to initiate payment: " + response.getBody());
            }
            
            return MomoPaymentResponse.success(transaction.getExternalId(), request.getAmount());
            
        } catch (Exception e) {
            log.error("Error requesting payment from MoMo API", e);
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorReason(e.getMessage());
            momoTransactionRepository.save(transaction);
            throw new RuntimeException("Error processing payment request", e);
        }
    }

    /**
     * Check the status of a transaction
     */
    public MomoTransactionStatus checkTransactionStatus(String referenceId) {
        return momoTransactionRepository.findByReferenceId(referenceId)
                .map(this::mapToTransactionStatus)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
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
                int maxAttempts = 10; // Maximum number of polling attempts
                int attempt = 0;
                long delayMs = 5000; // Start with 5 seconds delay

                while (attempt < maxAttempts) {
                    try {
                        // Generate new auth token for each attempt
                        String authToken = generateAuthToken();
                        
                        // Prepare headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Authorization", "Bearer " + authToken);
                        headers.set("Ocp-Apim-Subscription-Key", momoConfig.getSubscriptionKey());
                        headers.set("X-Target-Environment", momoConfig.getEnvironment());
                        
                        // Build request
                        String statusUrl = momoConfig.getCollectionBaseUrl() + 
                                         "/requesttopay/" + transaction.getReferenceId();
                        
                        HttpEntity<Void> entity = new HttpEntity<>(headers);
                        
                        log.debug("Checking payment status for reference: {}", referenceId);
                        
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
                            
                            // Update transaction status based on response
                            updateTransactionStatus(transaction, status, statusResponse);
                            
                            // If transaction is completed (successful or failed), stop polling
                            if (isFinalStatus(status)) {
                                log.info("Transaction {} completed with status: {}", 
                                        referenceId, status);
                                return;
                            }
                        }

                        // Exponential backoff for next poll
                        attempt++;
                        if (attempt < maxAttempts) {
                            long delay = delayMs * (long) Math.pow(2, attempt - 1);
                            log.debug("Will check status again in {}ms (attempt {}/{})", 
                                     delay, attempt + 1, maxAttempts);
                            Thread.sleep(delay);
                        }
                        
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Polling interrupted", ie);
                    } catch (Exception e) {
                        log.error("Error checking payment status for reference: " + referenceId, e);
                        attempt++;
                        try {
                            Thread.sleep(delayMs); // Wait before retry on error
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Polling interrupted during error handling", ie);
                        }
                    }
                }
                
                // If we've exhausted all attempts
                if (transaction.getStatus() != TransactionStatus.SUCCESS) {
                    transaction.setStatus(TransactionStatus.FAILED);
                    transaction.setErrorReason("Max polling attempts reached without final status");
                    momoTransactionRepository.save(transaction);
                    log.warn("Max polling attempts reached for transaction: {}", referenceId);
                }
            }))
            .orElse(CompletableFuture.completedFuture(null));
    }

    private MomoTransactionStatus mapToTransactionStatus(MomoTransaction transaction) {
        MomoTransactionStatus status = new MomoTransactionStatus();
        status.setReferenceId(transaction.getReferenceId());
        status.setStatus(transaction.getStatus().name());
        status.setAmount(transaction.getAmount().doubleValue());
        status.setCurrency(transaction.getCurrency());
        status.setFinancialTransactionId(transaction.getFinancialTransactionId());
        status.setExternalId(transaction.getExternalId());
        status.setErrorReason(transaction.getErrorReason());
        status.setTimestamp(transaction.getUpdatedAt());
        return status;
    }

    private TransactionStatus mapToTransactionStatus(String status) {
        try {
            return TransactionStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown transaction status: {}", status);
            return TransactionStatus.FAILED;
        }
    }

    /**
     * Generate an authentication token from MoMo API using username and password
     */
    private String generateAuthToken() {
        try {
            String authUrl = momoConfig.getAuthUrl();
            
            // Create login request
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", momoConfig.getUsername());
            loginRequest.put("password", momoConfig.getPassword());
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Ocp-Apim-Subscription-Key", momoConfig.getSubscriptionKey());
            
            // Create HTTP entity with login request
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);
            
            log.info("Requesting auth token from: {}", authUrl);
            
            // Make the authentication request
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                authUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("Failed to get auth token. Status: {}, Body: {}", 
                         response.getStatusCode(), 
                         response.getBody());
                throw new RuntimeException("Failed to get auth token. Status: " + response.getStatusCode());
            }
            
            // Safely get the JWT token from the response
            Map<String, Object> responseBody = response.getBody();
            Object token = responseBody.get("token");
            
            if (token != null && token instanceof String && !((String) token).isBlank()) {
                log.info("Successfully obtained JWT token");
                return (String) token;
            }
            
            log.error("Invalid or missing JWT token in response: {}", responseBody);
            throw new RuntimeException("Invalid or missing JWT token in response");
            
        } catch (Exception e) {
            log.error("Error generating JWT auth token", e);
            throw new RuntimeException("JWT Authentication failed: " + e.getMessage(), e);
        }
    }
    
    private void updateTransactionStatus(MomoTransaction transaction, String status, Map<String, Object> statusResponse) {
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
    
    private boolean isFinalStatus(String status) {
        return "SUCCESSFUL".equalsIgnoreCase(status) || 
               "FAILED".equalsIgnoreCase(status) || 
               "CANCELLED".equalsIgnoreCase(status);
    }
    
    
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
                    // Using CashierService to handle the order confirmation
                    try {
                        // Use the configured default preparation time
                        int estimatedPrepTimeMinutes = orderConfig.getDefaultPreparationTimeMinutes();
                        log.debug("Using preparation time of {} minutes for order {}", 
                                estimatedPrepTimeMinutes, order.getOrderId());
                        
                        // Update order status to CONFIRMED and handle notifications
                        cashierService.acceptOrder(order.getOrderId(), estimatedPrepTimeMinutes);
                        log.info("Order {} successfully processed after payment", order.getOrderId());
                    } catch (Exception e) {
                        log.error("Failed to process order {} after payment: {}", 
                                order.getOrderId(), e.getMessage(), e);
                        // Even if processing fails, we don't want to mark the payment as failed
                        // since the payment itself was successful
                    }
                    
                } else if (transaction.getStatus() == TransactionStatus.FAILED) {
                    order.setPaymentStatus(PaymentStatus.FAILED);
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    log.warn("Payment failed for order {}", order.getOrderId());
                    
                    // Notify customer about payment failure
                    try {
                        Map<String, Object> failureData = new HashMap<>();
                        failureData.put("orderId", order.getOrderId().toString());
                        failureData.put("error", transaction.getErrorReason());
                        failureData.put("message", "We couldn't process your payment. Please try again or use a different payment method.");
                        
                        notificationService.sendEmail(
                            order.getCustomer().getEmail(),
                            "Payment Failed - Order #" + order.getOrderId(),
                            "payment-failed",
                            failureData
                        );
                    } catch (Exception ex) {
                        log.error("Failed to send payment failure email for order {}", order.getOrderId(), ex);
                    }
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
        // Send error notification to operations team
        try {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            errorData.put("referenceId", transaction.getReferenceId());
            errorData.put("message", String.format(
                "An error occurred while processing transaction %s. Please check the logs for more details.", 
                transaction.getReferenceId()
            ));
            
            // Send to operations team email (replace with actual operations email from config)
            notificationService.sendEmail(
                "operations@mozfood.com",
                "[Action Required] Transaction Update Error: " + transaction.getReferenceId(),
                "error-alert",
                errorData
            );
        } catch (Exception ex) {
            log.error("Failed to send error notification for transaction {}", transaction.getReferenceId(), ex);
        }
    }
}

    private PaymentStatus mapToPaymentStatus(TransactionStatus status) {
        return switch (status) {
            case SUCCESS -> PaymentStatus.PAID;
            case FAILED -> PaymentStatus.FAILED;
            case PENDING -> PaymentStatus.PENDING;
            case CANCELLED -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }

    private void sendTransactionNotifications(MomoTransaction transaction) {
        if (transaction.getOrder() == null || transaction.getOrder().getCustomer() == null) {
            return;
        }

        String customerEmail = transaction.getOrder().getCustomer().getEmail();
        String orderId = transaction.getOrder().getOrderId().toString();
        
        switch (transaction.getStatus()) {
            case SUCCESS:
                // Use sendPaymentConfirmation for successful payments
                notificationService.sendPaymentConfirmation(
                    customerEmail,
                    Long.parseLong(orderId),
                    transaction.getAmount().floatValue(),
                    transaction.getReferenceId()
                );
                break;
                
            case FAILED:
                // For failed payments, use the generic sendEmail method
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
                // For pending payments
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
                // For cancelled payments
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
