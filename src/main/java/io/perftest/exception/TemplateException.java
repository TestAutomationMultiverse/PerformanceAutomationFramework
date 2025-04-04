package io.perftest.exception;

/**
 * Exception for template processing errors
 */
public class TemplateException extends PerfTestException {
    
    /**
     * Constructor with error code and message
     * @param errorCode Error code
     * @param message Error message
     */
    public TemplateException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructor with error code, message, and cause
     * @param errorCode Error code
     * @param message Error message
     * @param cause Cause of the exception
     */
    public TemplateException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
