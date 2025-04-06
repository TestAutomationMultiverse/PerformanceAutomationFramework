package io.perftest.ecs.exception.entity.protocol;

import io.perftest.ecs.exception.entity.EntityException;

/**
 * Exception class for SOAP-specific entity errors.
 * Used when there are issues with SOAP request entities.
 */
public class SoapEntityException extends EntityException {

    /**
     * Constructs a new SOAP entity exception with null as its detail message.
     */
    public SoapEntityException() {
        super();
    }

    /**
     * Constructs a new SOAP entity exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public SoapEntityException(String message) {
        super(message);
    }

    /**
     * Constructs a new SOAP entity exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public SoapEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new SOAP entity exception with the specified cause.
     * 
     * @param cause the cause
     */
    public SoapEntityException(Throwable cause) {
        super(cause);
    }
}
