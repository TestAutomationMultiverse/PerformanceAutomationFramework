package io.perftest.exception;

/**
 * Exception for configuration errors
 */
public class ConfigException extends PerfTestException {
    
    /**
     * Constructor with error code and message
     * @param errorCode Error code
     * @param message Error message
     */
    public ConfigException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructor with error code, message, and cause
     * @param errorCode Error code
     * @param message Error message
     * @param cause Cause of the exception
     */
    public ConfigException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
