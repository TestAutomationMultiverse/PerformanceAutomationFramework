package io.perftest.ecs.exception.entity.protocol;

import io.perftest.ecs.exception.entity.EntityException;

/**
 * Exception class for GraphQL-specific entity errors.
 * Used when there are issues with GraphQL request entities.
 */
public class GraphQLEntityException extends EntityException {

    /**
     * Constructs a new GraphQL entity exception with null as its detail message.
     */
    public GraphQLEntityException() {
        super();
    }

    /**
     * Constructs a new GraphQL entity exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public GraphQLEntityException(String message) {
        super(message);
    }

    /**
     * Constructs a new GraphQL entity exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public GraphQLEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new GraphQL entity exception with the specified cause.
     * 
     * @param cause the cause
     */
    public GraphQLEntityException(Throwable cause) {
        super(cause);
    }
}
