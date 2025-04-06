package io.perftest.ecs.exception.config;

import io.perftest.ecs.exception.EcsException;

/**
 * Exception class for configuration-related errors.
 * Used when there are issues with loading, parsing, or validating configuration.
 */
public class ConfigException extends EcsException {

    /**
     * Constructs a new configuration exception with null as its detail message.
     */
    public ConfigException() {
        super();
    }

    /**
     * Constructs a new configuration exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * Constructs a new configuration exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new configuration exception with the specified cause.
     * 
     * @param cause the cause
     */
    public ConfigException(Throwable cause) {
        super(cause);
    }
}
