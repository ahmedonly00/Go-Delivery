package com.goDelivery.goDelivery.service;

public interface AuditService {
    /**
     * Logs webhook processing details for auditing purposes
     * @param transactionId The transaction ID from the payment gateway
     * @param status The status of the transaction
     * @param details Additional details about the webhook processing
     */
    void logPaymentWebhook(String transactionId, String status, String details);
    
    /**
     * Logs payment processing details
     * @param paymentId The ID of the payment being processed
     * @param action The action being performed (e.g., "payment_processed", "refund_issued")
     * @param status The status of the action
     * @param details Additional details about the action
     */
    void logPaymentAction(Long paymentId, String action, String status, String details);
}
