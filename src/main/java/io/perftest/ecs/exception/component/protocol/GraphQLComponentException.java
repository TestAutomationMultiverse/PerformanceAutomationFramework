package io.perftest.ecs.exception.component.protocol;

import io.perftest.ecs.exception.component.ComponentException;

/**
 * Exception class for GraphQL-specific component errors.
 * Used when there are issues with components processing GraphQL entities.
 */
public class GraphQLComponentException extends ComponentException {

    /**
     * Constructs a new GraphQL component exception with null as its detail message.
     */
    public GraphQLComponentException() {
        super();
    }

    /**
     * Constructs a new GraphQL component exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public GraphQLComponentException(String message) {
        super(message);
    }

    /**
     * Constructs a new GraphQL component exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public GraphQLComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new GraphQL component exception with the specified cause.
     * 
     * @param cause the cause
     */
    public GraphQLComponentException(Throwable cause) {
        super(cause);
    }
}
