package io.perftest.ecs.exception.component.protocol;

import io.perftest.ecs.exception.component.ComponentException;

/**
 * Exception class for JDBC-specific component errors.
 * Used when there are issues with components processing JDBC entities.
 */
public class JdbcComponentException extends ComponentException {

    /**
     * Constructs a new JDBC component exception with null as its detail message.
     */
    public JdbcComponentException() {
        super();
    }

    /**
     * Constructs a new JDBC component exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public JdbcComponentException(String message) {
        super(message);
    }

    /**
     * Constructs a new JDBC component exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public JdbcComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new JDBC component exception with the specified cause.
     * 
     * @param cause the cause
     */
    public JdbcComponentException(Throwable cause) {
        super(cause);
    }
}
