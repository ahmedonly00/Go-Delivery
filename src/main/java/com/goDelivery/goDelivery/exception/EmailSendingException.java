package com.goDelivery.goDelivery.exception;

/**
 * Exception thrown when there is an error sending an email.
 */
public class EmailSendingException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
