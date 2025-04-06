package io.perftest.ecs.exception.system;

/**
 * Exception class for test execution-related errors.
 * Used when there are issues during the actual execution of a test.
 */
public class TestExecutionException extends TestSystemException {

    /**
     * Constructs a new test execution exception with null as its detail message.
     */
    public TestExecutionException() {
        super();
    }

    /**
     * Constructs a new test execution exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public TestExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new test execution exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public TestExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new test execution exception with the specified cause.
     * 
     * @param cause the cause
     */
    public TestExecutionException(Throwable cause) {
        super(cause);
    }
}
