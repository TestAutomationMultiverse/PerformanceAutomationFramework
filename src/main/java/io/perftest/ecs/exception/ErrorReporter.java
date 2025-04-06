package io.perftest.ecs.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Error reporter utility class for the ECS architecture.
 * Provides methods for logging and reporting errors.
 */
public class ErrorReporter {
    private static final Logger logger = LoggerFactory.getLogger(ErrorReporter.class);
    
    /**
     * Report an error with a specific error code
     * @param errorCode The error code
     * @param message The error message
     */
    public static void reportError(ErrorCode errorCode, String message) {
        logger.error("{}: {}", errorCode, message);
    }
    
    /**
     * Report an error with a specific error code and exception
     * @param errorCode The error code
     * @param message The error message
     * @param exception The exception that caused the error
     */
    public static void reportError(ErrorCode errorCode, String message, Throwable exception) {
        logger.error("{}: {}", errorCode, message, exception);
    }
    
    /**
     * Report a warning with a specific error code
     * @param errorCode The error code
     * @param message The warning message
     */
    public static void reportWarning(ErrorCode errorCode, String message) {
        logger.warn("{}: {}", errorCode, message);
    }
    
    /**
     * Report a warning with a specific error code and exception
     * @param errorCode The error code
     * @param message The warning message
     * @param exception The exception that caused the warning
     */
    public static void reportWarning(ErrorCode errorCode, String message, Throwable exception) {
        logger.warn("{}: {}", errorCode, message, exception);
    }
    
    /**
     * Report information with a specific error code
     * @param errorCode The error code
     * @param message The information message
     */
    public static void reportInfo(ErrorCode errorCode, String message) {
        logger.info("{}: {}", errorCode, message);
    }
    
    /**
     * Create an exception with a specific error code
     * @param errorCode The error code
     * @param message The error message
     * @return A new EcsException with the specified error code and message
     */
    public static EcsException createException(ErrorCode errorCode, String message) {
        return new EcsException(errorCode, message);
    }
    
    /**
     * Create an exception with a specific error code and cause
     * @param errorCode The error code
     * @param message The error message
     * @param cause The cause of the exception
     * @return A new EcsException with the specified error code, message, and cause
     */
    public static EcsException createException(ErrorCode errorCode, String message, Throwable cause) {
        return new EcsException(errorCode, message, cause);
    }
    
    /**
     * Throw an exception with a specific error code
     * @param errorCode The error code
     * @param message The error message
     * @throws EcsException The created exception
     */
    public static void throwException(ErrorCode errorCode, String message) {
        throw createException(errorCode, message);
    }
    
    /**
     * Throw an exception with a specific error code and cause
     * @param errorCode The error code
     * @param message The error message
     * @param cause The cause of the exception
     * @throws EcsException The created exception
     */
    public static void throwException(ErrorCode errorCode, String message, Throwable cause) {
        throw createException(errorCode, message, cause);
    }
}
