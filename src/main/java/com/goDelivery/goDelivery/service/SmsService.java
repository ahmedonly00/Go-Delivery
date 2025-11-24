package com.goDelivery.goDelivery.service;

/**
 * Service for sending SMS notifications
 */
public interface SmsService {
    
    /**
     * Sends an SMS message to the specified phone number
     * @param toPhoneNumber The recipient's phone number in international format (e.g., +1234567890)
     * @param message The message to send
     * @return true if the message was sent successfully, false otherwise
     */
    boolean sendSms(String toPhoneNumber, String message);
    
    /**
     * Sends an SMS message to the specified phone number asynchronously
     * @param toPhoneNumber The recipient's phone number in international format (e.g., +1234567890)
     * @param message The message to send
     * @return A CompletableFuture that will be completed with true if the message was sent successfully, false otherwise
     */
    default java.util.concurrent.CompletableFuture<Boolean> sendSmsAsync(String toPhoneNumber, String message) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> sendSms(toPhoneNumber, message));
    }
}
