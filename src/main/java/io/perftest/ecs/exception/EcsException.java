package io.perftest.ecs.exception;

import lombok.Getter;

/**
 * Base exception class for all ECS (Entity-Component-System) related exceptions.
 * This class serves as the root of the exception hierarchy within the ECS architecture.
 */
public class EcsException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    @Getter
    private final ErrorCode errorCode;
    
    /**
     * Constructs a new ECS exception with null as its detail message.
     */
    public EcsException() {
        super();
        this.errorCode = ErrorCode.UNEXPECTED_ERROR;
    }

    /**
     * Constructs a new ECS exception with the specified detail message.
     * 
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public EcsException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNEXPECTED_ERROR;
    }

    /**
     * Constructs a new ECS exception with the specified error code and detail message.
     * 
     * @param errorCode the error code for this exception
     * @param message the detail message
     */
    public EcsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new ECS exception with the specified detail message and cause.
     * 
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public EcsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.UNEXPECTED_ERROR;
    }

    /**
     * Constructs a new ECS exception with the specified error code, detail message, and cause.
     * 
     * @param errorCode the error code for this exception
     * @param message the detail message
     * @param cause the cause
     */
    public EcsException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new ECS exception with the specified cause.
     * 
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public EcsException(Throwable cause) {
        super(cause);
        this.errorCode = ErrorCode.UNEXPECTED_ERROR;
    }
    
    /**
     * Get a string representation of the exception
     * @return String representation
     */
    @Override
    public String toString() {
        return String.format("%s: %s", errorCode, getMessage());
    }
}
