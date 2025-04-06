package io.perftest.ecs.exception.component;

import io.perftest.ecs.exception.EcsException;

/**
 * Exception class for all Component-related errors within the ECS architecture.
 * Used when there are issues with Component processing, initialization, or interaction.
 */
public class ComponentException extends EcsException {

    /**
     * Constructs a new component exception with null as its detail message.
     */
    public ComponentException() {
        super();
    }

    /**
     * Constructs a new component exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public ComponentException(String message) {
        super(message);
    }

    /**
     * Constructs a new component exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new component exception with the specified cause.
     * 
     * @param cause the cause
     */
    public ComponentException(Throwable cause) {
        super(cause);
    }
}
