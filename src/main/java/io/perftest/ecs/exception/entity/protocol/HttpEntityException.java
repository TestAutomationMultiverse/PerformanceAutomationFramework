package io.perftest.ecs.exception.entity.protocol;

import io.perftest.ecs.exception.entity.EntityException;

/**
 * Exception class for HTTP-specific entity errors.
 * Used when there are issues with HTTP request entities.
 */
public class HttpEntityException extends EntityException {

    /**
     * Constructs a new HTTP entity exception with null as its detail message.
     */
    public HttpEntityException() {
        super();
    }

    /**
     * Constructs a new HTTP entity exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public HttpEntityException(String message) {
        super(message);
    }

    /**
     * Constructs a new HTTP entity exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public HttpEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new HTTP entity exception with the specified cause.
     * 
     * @param cause the cause
     */
    public HttpEntityException(Throwable cause) {
        super(cause);
    }
}
