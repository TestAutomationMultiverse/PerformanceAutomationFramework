package io.perftest.ecs.exception.entity;

import io.perftest.ecs.exception.EcsException;

/**
 * Exception class for all Entity-related errors within the ECS architecture.
 * Used when there are issues with Entity creation, validation, or processing.
 */
public class EntityException extends EcsException {

    /**
     * Constructs a new entity exception with null as its detail message.
     */
    public EntityException() {
        super();
    }

    /**
     * Constructs a new entity exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public EntityException(String message) {
        super(message);
    }

    /**
     * Constructs a new entity exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public EntityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new entity exception with the specified cause.
     * 
     * @param cause the cause
     */
    public EntityException(Throwable cause) {
        super(cause);
    }
}
