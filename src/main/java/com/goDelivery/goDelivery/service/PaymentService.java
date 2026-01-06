package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.exception.ConcurrentModificationException;
import com.goDelivery.goDelivery.dtos.momo.MomoPaymentRequest;
import com.goDelivery.goDelivery.dtos.momo.MomoPaymentResponse;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.repository.PaymentRepository;

import io.jsonwebtoken.lang.Arrays;

import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.mapper.PaymentMapper;
import com.goDelivery.goDelivery.dtos.mpesa.MpesaPaymentRequest;
import com.goDelivery.goDelivery.dtos.mpesa.MpesaPaymentResponse;
import com.goDelivery.goDelivery.dtos.mpesa.MpesaWebhookRequest;
import com.goDelivery.goDelivery.dtos.payment.PaymentRequest;
import com.goDelivery.goDelivery.dtos.payment.PaymentResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;


import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final MpesaPaymentService mpesaPaymentService;
    private final MomoService momoService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final Environment env;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment for order ID: {}", paymentRequest.getOrderId());
        
        // Find the order
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + paymentRequest.getOrderId()));
        
        // Create and save payment
        Payment payment = paymentMapper.toEntity(paymentRequest, order);
        
        try {
            // Process payment based on payment method
            switch (paymentRequest.getPaymentMethod()) {
                case MPESA:
                    return processMpesaPayment(payment);
                case EMOLA:
                    return processEmolaPayment(payment);
                case CARD:
                    return processCardPayment(payment);
                case CASH:
                    return processCashPayment(payment);
                case MOMO:
                    return processMomoPayment(payment);
                default:
                    throw new IllegalArgumentException("Unsupported payment method: " + paymentRequest.getPaymentMethod());
            }
        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage(), e);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            payment = paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }


    public PaymentResponse getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return paymentMapper.toDto(payment);
    }


    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        return paymentMapper.toDto(payment);
    }


    private PaymentResponse processMpesaPayment(Payment payment) {
        log.info("Processing MPESA payment for order: {}", payment.getOrder().getOrderId());
        
        try {
            // Get order details
            Order order = payment.getOrder();
            
            // Create MPESA payment request
            MpesaPaymentRequest mpesaRequest = new MpesaPaymentRequest();
            mpesaRequest.setFromMSISDN(payment.getPhoneNumber());
            // Set amount as Double
            mpesaRequest.setAmount(payment.getAmount().floatValue());
            
            // Set callback URL for webhook
            String webhookUrl = env.getProperty("app.base-url") + "/api/v1/payments/mpesa/webhook";
            mpesaRequest.setCallback(webhookUrl);
            
            // Set third party reference (order ID)
            mpesaRequest.setThirdPartyRef("ORDER_" + order.getOrderId());
            
            // Initiate MPESA payment
            MpesaPaymentResponse mpesaResponse = mpesaPaymentService.initiatePayment(mpesaRequest).block();
            
            if (mpesaResponse != null && mpesaResponse.getTransactionId() != null) {
                // Payment initiated successfully
                payment.setTransactionId(mpesaResponse.getTransactionId());
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setGateWayResponse("MPESA payment initiated. Transaction ID: " + mpesaResponse.getTransactionId());
                
                // Save payment with transaction ID for future reference
                payment = paymentRepository.save(payment);
                
                // Return response with payment instructions
                PaymentResponse response = paymentMapper.toDto(payment);
                response.setMessage("Please complete the payment on your phone. You will receive an MPESA prompt.");
                return response;
            } else {
                // Handle MPESA API error
                String errorMsg = mpesaResponse != null ? 
                    "Failed to initiate MPESA payment. Status: " + 
                    (mpesaResponse.getTransactionStatus() != null ? mpesaResponse.getTransactionStatus() : "Unknown error") : 
                    "Failed to initiate MPESA payment: No response from payment gateway";
                
                log.error(errorMsg);
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setGateWayResponse(errorMsg);
                paymentRepository.save(payment);
                
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            log.error("Error processing MPESA payment: {}", e.getMessage(), e);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setGateWayResponse("Error processing MPESA payment: " + e.getMessage());
            paymentRepository.save(payment);
            
            throw new RuntimeException("Failed to process MPESA payment: " + e.getMessage(), e);
        }
    }

    public void handleMpesaWebhook(MpesaWebhookRequest webhookRequest) {
        log.info("Processing MPESA webhook: {}", webhookRequest);
        
        // Get the signature from headers
        String signature = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null && attributes.getRequest() != null) {
            // Try different possible header names
            signature = attributes.getRequest().getHeader("X-MPESA-Signature");
            if (signature == null) {
                signature = attributes.getRequest().getHeader("signature");
            }
            if (signature == null) {
                signature = attributes.getRequest().getHeader("x-mpesa-signature");
            }
            
            // Log all headers for debugging (sensitive info should be redacted in production)
            if (log.isDebugEnabled()) {
                java.util.Enumeration<String> headerNames = attributes.getRequest().getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    log.debug("Header: {} = {}", headerName, attributes.getRequest().getHeader(headerName));
                }
            }
        }
        
        // Log the received signature for debugging
        log.debug("Received webhook signature: {}", signature);
        
        // Skip signature validation in development if configured
        if (Arrays.asList(env.getActiveProfiles()).contains("dev") || 
            "true".equalsIgnoreCase(env.getProperty("mpesa.webhook.disable-signature-validation", "false"))) {
            log.warn("Skipping webhook signature validation in development mode");
        } 
        // Validate signature if not in development mode
        else if (signature == null || !isValidSignature(webhookRequest, signature)) {
            log.warn("Invalid or missing webhook signature. Possible tampering detected or not called in an HTTP request context.");
            throw new SecurityException("Invalid or missing webhook signature");
        }
        
        // Add retry logic
        int maxRetries = 3;
        int attempt = 0;
        boolean success = false;
        Exception lastError = null;
        
        while (attempt < maxRetries && !success) {
            try {
                attempt++;
                log.info("Processing webhook attempt {}/{}", attempt, maxRetries);
                
                // Find payment by transaction ID or third party reference
                Payment payment = paymentRepository.findByTransactionId(webhookRequest.getTransactionId())
                        .or(() -> {
                            // Try to find by order ID from third party reference
                            String ref = webhookRequest.getThirdPartyRef();
                            if (ref != null && ref.startsWith("ORDER_")) {
                                try {
                                    Long orderId = Long.parseLong(ref.substring(6));
                                    return paymentRepository.findByOrder_OrderId(orderId);
                                } catch (NumberFormatException e) {
                                    log.warn("Invalid order ID in third party reference: {}", ref);
                                    return Optional.empty();
                                }
                            }
                            return Optional.empty();
                        })
                        .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Payment not found for transaction: %s, reference: %s", 
                                webhookRequest.getTransactionId(), 
                                webhookRequest.getThirdPartyRef())));
                
                // Check if this is a duplicate webhook for a successful payment
                if (payment.getPaymentStatus() == PaymentStatus.PAID && 
                    webhookRequest.getTransactionStatus().equalsIgnoreCase("SUCCESS")) {
                    log.info("Duplicate webhook received for paid transaction: {}", 
                            webhookRequest.getTransactionId());
                    return;
                }
                
                // Update payment status based on webhook
                switch (webhookRequest.getTransactionStatus().toUpperCase()) {
                    case "SUCCESS":
                        handleSuccessfulPayment(webhookRequest, payment);
                        break;
                        
                    case "FAILED":
                    case "REJECTED":
                        payment.setPaymentStatus(PaymentStatus.FAILED);
                        payment.setGateWayResponse("Payment failed: " + webhookRequest.getDescription());
                        break;
                        
                    case "PENDING":
                        payment.setPaymentStatus(PaymentStatus.PENDING);
                        payment.setGateWayResponse("Payment pending: " + webhookRequest.getDescription());
                        break;
                        
                    case "CANCELLED":
                        handleCancelledPayment(webhookRequest, payment);
                        break;
                        
                    default:
                        log.warn("Unknown payment status: {}", webhookRequest.getTransactionStatus());
                        payment.setGateWayResponse("Payment status: " + webhookRequest.getTransactionStatus() + ". " + webhookRequest.getDescription());
                }
                
                // Save the updated payment with optimistic locking
                payment = savePaymentWithRetry(payment, webhookRequest, attempt, maxRetries);
                success = true; // Mark as successful if we reach here
                
            } catch (Exception e) {
                log.error("Error processing webhook attempt {}/{}: {}", 
                        attempt, maxRetries, e.getMessage(), e);
                lastError = e;
                if (attempt >= maxRetries) {
                    throw new RuntimeException(
                            "Failed to process webhook after " + maxRetries + " attempts", 
                            lastError);
                }
                // Exponential backoff
                try { 
                    Thread.sleep(1000 * (long) Math.pow(2, attempt - 1)); 
                } catch (InterruptedException ie) { 
                    Thread.currentThread().interrupt(); 
                }
            }
        }
        
        if (!success && lastError != null) {
            log.error("Failed to process webhook after {} attempts: {}", 
                    maxRetries, lastError.getMessage());
            throw new RuntimeException(
                    "Failed to process webhook after " + maxRetries + " attempts", 
                    lastError);
        }
    }
    

    private void handleSuccessfulPayment(MpesaWebhookRequest webhookRequest, Payment payment) {
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDate.now());
        payment.setGateWayResponse("Payment successful. " + webhookRequest.getDescription());
        
        // Update order status to PREPARING
        Order order = payment.getOrder();
        order.setOrderStatus(OrderStatus.PREPARING);
        order.setUpdatedAt(LocalDate.now());
        orderRepository.save(order);
        
        // Send notification to customer
        try {
            notificationService.sendPaymentConfirmation(
                order.getCustomer().getEmail(),
                order.getOrderId(),
                payment.getAmount(),
                payment.getTransactionId()
            );
        } catch (Exception e) {
            log.error("Failed to send payment confirmation: {}", e.getMessage(), e);
        }
    }
    
    private void handleCancelledPayment(MpesaWebhookRequest webhookRequest, Payment payment) {
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setGateWayResponse("Payment was cancelled by user: " + webhookRequest.getDescription());
        
        // Update order status to CANCELLED
        Order order = payment.getOrder();
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDate.now());
        orderRepository.save(order);
    }
    
    //Validates the MPESA webhook signature to ensure it's from a trusted source
    private boolean isValidSignature(MpesaWebhookRequest webhookRequest, String receivedSignature) {
        if (receivedSignature == null || receivedSignature.trim().isEmpty()) {
            log.warn("No signature provided for webhook validation");
            return false;
        }
        
        try {
            // Get the MPESA API key from environment variables
            String apiKey = env.getProperty("mpesa.api-key");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("MPESA API key is not configured. Please set 'mpesa.api-key' in your application properties.");
                return false;
            }
            
            // Log the input values for debugging (be careful with sensitive data in production)
            log.debug("Validating signature with transactionId: {}, status: {}", 
                webhookRequest.getTransactionId(), 
                webhookRequest.getTransactionStatus());
            
            // Create the expected signature using the request data
            // Note: The exact format should match what MPESA is using to generate the signature
            // This is just an example - adjust according to MPESA's documentation
            String payload = String.format(
                "%s%s%s", 
                webhookRequest.getTransactionId() != null ? webhookRequest.getTransactionId() : "",
                webhookRequest.getTransactionStatus() != null ? webhookRequest.getTransactionStatus() : "",
                apiKey
            );
            
            log.debug("Payload for signature: {}", payload);
            
            // Generate the expected signature
            String expectedSignature = hashWithHmacSha256(payload, apiKey);
            log.debug("Generated signature: {}", expectedSignature);
            log.debug("Received signature: {}", receivedSignature);
            
            // Compare the signatures in a time-constant manner to prevent timing attacks
            boolean isValid = constantTimeEquals(receivedSignature, expectedSignature);
            
            if (!isValid) {
                log.warn("Signature validation failed. Expected: {}, Actual: {}", 
                    expectedSignature, receivedSignature);
            } else {
                log.debug("Signature validation successful");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Error validating webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }
    
    //Compares two strings in constant time to prevent timing attacks
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    //Generates an HMAC-SHA256 hash of the input data using the provided key
    private String hashWithHmacSha256(String data, String key) {
        try {
            javax.crypto.Mac sha256Hmac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return javax.xml.bind.DatatypeConverter.printHexBinary(signedBytes).toLowerCase();
        } catch (Exception e) {
            log.error("Error generating HMAC-SHA256 hash: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate signature hash", e);
        }
    }
    
    @Retryable(value = { OptimisticLockingFailureException.class }, 
              maxAttempts = 3, 
              backoff = @Backoff(delay = 500, multiplier = 2))
    private Payment savePaymentWithRetry(Payment payment, MpesaWebhookRequest webhookRequest, 
                                       int attempt, int maxRetries) {
        try {
            payment.setVersion(payment.getVersion() + 1);
            payment = paymentRepository.saveAndFlush(payment);
            
            log.info("Updated payment status for transaction {} to {}", 
                    webhookRequest.getTransactionId(), 
                    payment.getPaymentStatus());
            
            // Log the successful webhook processing
            try {
                auditService.logPaymentWebhook(
                    webhookRequest.getTransactionId(),
                    webhookRequest.getTransactionStatus(),
                    String.format("Payment %s processed successfully", 
                        webhookRequest.getTransactionStatus().toLowerCase())
                );
            } catch (Exception e) {
                log.error("Failed to log payment webhook: {}", e.getMessage(), e);
                // Don't fail the operation if logging fails
            }
            
            return payment;
            
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock exception while updating payment (attempt {}/{}). Error: {}", 
                    attempt, maxRetries, e.getMessage());
            if (attempt >= maxRetries) {
                throw new ConcurrentModificationException(
                        "Failed to update payment after " + maxRetries + " attempts due to concurrent modification", e);
            }
            throw e;
        }
    }
    
    private PaymentResponse processEmolaPayment(Payment payment) {
        log.info("Processing Emola payment");
        // TODO: Implement actual Emola integration
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setGateWayResponse("Emola payment processed successfully");
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    private PaymentResponse processCardPayment(Payment payment) {
        log.info("Processing Card payment");
        // TODO: Implement actual Card payment integration
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setGateWayResponse("Card payment processed successfully");
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    private PaymentResponse processCashPayment(Payment payment) {
        log.info("Processing Cash payment");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setGateWayResponse("Payment will be processed on delivery");
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    private PaymentResponse processMomoPayment(Payment payment) {
        log.info("Processing MoMo payment for order: {}", payment.getOrder().getOrderId());
        
        try {
            // Get order details
            Order order = payment.getOrder();
            
            // Create MoMo payment request
            MomoPaymentRequest momoRequest = new MomoPaymentRequest();
            momoRequest.setMsisdn(payment.getPhoneNumber());
            momoRequest.setAmount(payment.getAmount().floatValue());
            
            // Set callback URL for webhook
            String webhookUrl = env.getProperty("app.base-url") + "/api/v1/payments/momo/webhook";
            momoRequest.setCallback(webhookUrl);
            
            // Set external ID (order ID)
            String externalId = "ORDER_" + order.getOrderId();
            momoRequest.setExternalId(externalId);
            
            // Set payment message
            momoRequest.setPayerMessageTitle("Moz Food Order #" + order.getOrderId());
            momoRequest.setPayerMessageDescription("Payment for order #" + order.getOrderId());
            
            // Initiate MoMo payment
            MomoPaymentResponse momoResponse = momoService.requestPayment(momoRequest);
            
            if (momoResponse != null && momoResponse.getReferenceId() != null) {
                // Payment initiated successfully
                payment.setTransactionId(momoResponse.getReferenceId());
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setGateWayResponse("MoMo payment initiated. Reference ID: " + momoResponse.getReferenceId());
                
                // Save payment with transaction ID for future reference
                payment = paymentRepository.save(payment);
                
                // Return response with payment instructions
                PaymentResponse response = paymentMapper.toDto(payment);
                response.setMessage("Please complete the payment on your phone. You will receive an MoMo prompt.");
                return response;
            } else {
                // Handle MoMo API error
                String errorMsg = momoResponse != null ? 
                    "Failed to initiate MoMo payment. " + 
                    (momoResponse.getMessage() != null ? momoResponse.getMessage() : "Unknown error") : 
                    "Failed to initiate MoMo payment: No response from payment gateway";
                
                log.error(errorMsg);
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setGateWayResponse(errorMsg);
                paymentRepository.save(payment);
                
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            log.error("Error processing MoMo payment: {}", e.getMessage(), e);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setGateWayResponse("Error processing MoMo payment: " + e.getMessage());
            paymentRepository.save(payment);
            
            throw new RuntimeException("Failed to process MoMo payment: " + e.getMessage(), e);
        }
    }
}