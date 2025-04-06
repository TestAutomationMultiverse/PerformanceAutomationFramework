package io.perftest.ecs.exception.system;

import io.perftest.ecs.exception.EcsException;

/**
 * Exception class for all System-related errors within the ECS architecture.
 * Used when there are issues with System initialization, coordination, or execution.
 */
public class SystemException extends EcsException {

    /**
     * Constructs a new system exception with null as its detail message.
     */
    public SystemException() {
        super();
    }

    /**
     * Constructs a new system exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public SystemException(String message) {
        super(message);
    }

    /**
     * Constructs a new system exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new system exception with the specified cause.
     * 
     * @param cause the cause
     */
    public SystemException(Throwable cause) {
        super(cause);
    }
}
