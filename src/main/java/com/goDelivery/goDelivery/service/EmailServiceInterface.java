package com.goDelivery.goDelivery.service;

import java.util.concurrent.CompletableFuture;

public interface EmailServiceInterface {
    /**
     * Sends a test email to the specified address
     * @param to The email address to send the test to
     * @return CompletableFuture that completes with true if the email was sent successfully
     */
    CompletableFuture<Boolean> sendTestEmail(String to);
    
    /**
     * Sends a welcome email to a new restaurant owner with their login credentials
     * @param restaurantName Name of the restaurant
     * @param ownerEmail Email address of the restaurant owner
     * @param ownerName Name of the restaurant owner
     * @param temporaryPassword Generated temporary password
     * @throws EmailSendingException if the email cannot be sent after all retry attempts
     */
    void sendRestaurantWelcomeEmail(String restaurantName, String ownerEmail, String ownerName, String temporaryPassword);
    
    /**
     * Sends an email notification when a restaurant application is rejected
     * @param restaurantName Name of the restaurant
     * @param ownerEmail Email address of the restaurant owner
     * @param rejectionReason Reason for rejection (optional)
     */
    void sendApplicationRejectionEmail(String restaurantName, String ownerEmail, String rejectionReason);
    
    /**
     * Sends a password reset email with a reset token
     * @param email User's email address
     * @param resetToken Password reset token
     */
    void sendPasswordResetEmail(String email, String resetToken);
    
    /**
     * Sends an email with retry mechanism
     * @param to Recipient email address
     * @param subject Email subject
     * @param content Email content (HTML)
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendEmailWithRetry(String to, String subject, String content);
}

class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
