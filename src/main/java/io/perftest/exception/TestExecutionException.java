package io.perftest.exception;

/**
 * Exception thrown for test execution-related errors.
 */
public class TestExecutionException extends PerfTestException {
    
    public TestExecutionException(String message) {
        super(ErrorCode.TEST_EXECUTION_ERROR, message);
    }
    
    public TestExecutionException(String message, Throwable cause) {
        super(ErrorCode.TEST_EXECUTION_ERROR, message, cause);
    }
    
    public TestExecutionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public TestExecutionException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
