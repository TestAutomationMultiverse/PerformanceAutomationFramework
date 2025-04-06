package io.perftest.ecs.exception.component.protocol;

import io.perftest.ecs.exception.component.ComponentException;

/**
 * Exception class for SOAP-specific component errors.
 * Used when there are issues with components processing SOAP entities.
 */
public class SoapComponentException extends ComponentException {

    /**
     * Constructs a new SOAP component exception with null as its detail message.
     */
    public SoapComponentException() {
        super();
    }

    /**
     * Constructs a new SOAP component exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public SoapComponentException(String message) {
        super(message);
    }

    /**
     * Constructs a new SOAP component exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public SoapComponentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new SOAP component exception with the specified cause.
     * 
     * @param cause the cause
     */
    public SoapComponentException(Throwable cause) {
        super(cause);
    }
}
