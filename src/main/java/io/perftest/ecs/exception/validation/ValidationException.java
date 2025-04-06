package io.perftest.ecs.exception.validation;

import io.perftest.ecs.exception.EcsException;

/**
 * Exception class for validation-related errors.
 * Used when there are issues with validating input parameters, entities, or configurations.
 */
public class ValidationException extends EcsException {

    /**
     * Constructs a new validation exception with null as its detail message.
     */
    public ValidationException() {
        super();
    }

    /**
     * Constructs a new validation exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new validation exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new validation exception with the specified cause.
     * 
     * @param cause the cause
     */
    public ValidationException(Throwable cause) {
        super(cause);
    }
}
