package com.goDelivery.goDelivery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a concurrent modification is detected during an update operation.
 * This typically occurs when multiple threads attempt to modify the same resource simultaneously,
 * and the operation cannot be completed due to version conflicts.
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConcurrentModificationException extends RuntimeException {

    /**
     * Constructs a new concurrent modification exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ConcurrentModificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new concurrent modification exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
