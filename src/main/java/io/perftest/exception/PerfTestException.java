package io.perftest.exception;

import lombok.Getter;

/**
 * Base exception class for performance test framework
 */
public class PerfTestException extends RuntimeException {
    
    @Getter
    private final ErrorCode errorCode;
    
    /**
     * Constructor with error code and message
     * @param errorCode Error code
     * @param message Error message
     */
    public PerfTestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructor with error code, message, and cause
     * @param errorCode Error code
     * @param message Error message
     * @param cause Cause of the exception
     */
    public PerfTestException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
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
