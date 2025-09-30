package com.goDelivery.goDelivery.service;

import java.util.concurrent.CompletableFuture;

public interface EmailServiceInterface {
  
    CompletableFuture<Boolean> sendTestEmail(String to);
    
    void sendRestaurantWelcomeEmail(String restaurantName, String ownerEmail, String ownerName, String temporaryPassword);
    
    void sendApplicationRejectionEmail(String restaurantName, String ownerEmail, String rejectionReason);
    
    void sendPasswordResetEmail(String toEmail, String resetLink);
    
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
