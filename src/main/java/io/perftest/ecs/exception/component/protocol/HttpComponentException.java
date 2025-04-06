package io.perftest.ecs.exception.component.protocol;

import io.perftest.ecs.exception.component.ComponentException;

/**
 * Exception class for HTTP-specific component errors.
 * Used when there are issues with components processing HTTP entities.
 */
public class HttpComponentException extends ComponentException {

    /**
     * Constructs a new HTTP component exception with null as its detail message.
     */
    public HttpComponentException() {
        super();
    }

    /**
     * Constructs a new HTTP component exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public HttpComponentException(String message) {
        super(message);
    }

    /**
     * Constructs a new HTTP component exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public HttpComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new HTTP component exception with the specified cause.
     * 
     * @param cause the cause
     */
    public HttpComponentException(Throwable cause) {
        super(cause);
    }
}
