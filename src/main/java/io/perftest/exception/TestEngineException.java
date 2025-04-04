package io.perftest.exception;

/**
 * Exception for test engine errors
 */
public class TestEngineException extends PerfTestException {
    
    /**
     * Constructor with error code and message
     * @param errorCode Error code
     * @param message Error message
     */
    public TestEngineException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * Constructor with error code, message, and cause
     * @param errorCode Error code
     * @param message Error message
     * @param cause Cause of the exception
     */
    public TestEngineException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
