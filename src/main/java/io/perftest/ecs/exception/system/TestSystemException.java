package io.perftest.ecs.exception.system;

/**
 * Exception class for test system-related errors.
 * Used when there are issues with test initialization, execution, or reporting.
 */
public class TestSystemException extends SystemException {

    /**
     * Constructs a new test system exception with null as its detail message.
     */
    public TestSystemException() {
        super();
    }

    /**
     * Constructs a new test system exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public TestSystemException(String message) {
        super(message);
    }

    /**
     * Constructs a new test system exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public TestSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new test system exception with the specified cause.
     * 
     * @param cause the cause
     */
    public TestSystemException(Throwable cause) {
        super(cause);
    }
}
