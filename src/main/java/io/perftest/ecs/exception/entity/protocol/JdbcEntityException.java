package io.perftest.ecs.exception.entity.protocol;

import io.perftest.ecs.exception.entity.EntityException;

/**
 * Exception class for JDBC-specific entity errors.
 * Used when there are issues with JDBC query entities.
 */
public class JdbcEntityException extends EntityException {

    /**
     * Constructs a new JDBC entity exception with null as its detail message.
     */
    public JdbcEntityException() {
        super();
    }

    /**
     * Constructs a new JDBC entity exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public JdbcEntityException(String message) {
        super(message);
    }

    /**
     * Constructs a new JDBC entity exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public JdbcEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new JDBC entity exception with the specified cause.
     * 
     * @param cause the cause
     */
    public JdbcEntityException(Throwable cause) {
        super(cause);
    }
}
