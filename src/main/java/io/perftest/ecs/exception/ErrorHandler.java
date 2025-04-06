package io.perftest.ecs.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Error handler utility class for the ECS architecture.
 * Provides methods for handling exceptions and creating Result objects.
 */
public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    
    /**
     * Create a Result from an exception
     * @param exception The exception to wrap
     * @param <T> The type of the Result
     * @return A failure Result containing the exception
     */
    public static <T> Result<T> handleException(Exception exception) {
        if (exception instanceof EcsException) {
            EcsException ecsException = (EcsException) exception;
            return Result.failure(ecsException.getErrorCode(), 
                    ecsException.getMessage(), 
                    ecsException.getCause());
        } else {
            logger.error("Unexpected error: {}", exception.getMessage(), exception);
            return Result.failure(ErrorCode.UNEXPECTED_ERROR, 
                    "Unexpected error: " + exception.getMessage(), 
                    exception);
        }
    }
    
    /**
     * Create a Result from an exception with a specific error code
     * @param exception The exception to wrap
     * @param errorCode The error code to use
     * @param <T> The type of the Result
     * @return A failure Result containing the exception and error code
     */
    public static <T> Result<T> handleException(Exception exception, ErrorCode errorCode) {
        logger.error("{}: {}", errorCode, exception.getMessage(), exception);
        return Result.failure(errorCode, exception.getMessage(), exception);
    }
    
    /**
     * Create a Result from an exception with a specific error code and message
     * @param exception The exception to wrap
     * @param errorCode The error code to use
     * @param message The error message to use
     * @param <T> The type of the Result
     * @return A failure Result containing the exception, error code, and message
     */
    public static <T> Result<T> handleException(Exception exception, ErrorCode errorCode, String message) {
        logger.error("{}: {}", errorCode, message, exception);
        return Result.failure(errorCode, message, exception);
    }
    
    /**
     * Try to execute a function and wrap the result in a Result
     * @param func The function to execute
     * @param <T> The type of the Result
     * @return A Result containing either the function result or any exception that was thrown
     */
    public static <T> Result<T> tryExecute(Function<Void, T> func) {
        try {
            return Result.success(func.apply(null));
        } catch (Exception e) {
            return handleException(e);
        }
    }
    
    /**
     * Try to execute a function and wrap the result in a Result with a specific error code
     * @param func The function to execute
     * @param errorCode The error code to use if an exception is thrown
     * @param <T> The type of the Result
     * @return A Result containing either the function result or any exception that was thrown
     */
    public static <T> Result<T> tryExecute(Function<Void, T> func, ErrorCode errorCode) {
        try {
            return Result.success(func.apply(null));
        } catch (Exception e) {
            return handleException(e, errorCode);
        }
    }
    
    /**
     * Try to execute a function and wrap the result in a Result with a specific error code and message
     * @param func The function to execute
     * @param errorCode The error code to use if an exception is thrown
     * @param message The error message to use if an exception is thrown
     * @param <T> The type of the Result
     * @return A Result containing either the function result or any exception that was thrown
     */
    public static <T> Result<T> tryExecute(Function<Void, T> func, ErrorCode errorCode, String message) {
        try {
            return Result.success(func.apply(null));
        } catch (Exception e) {
            return handleException(e, errorCode, message);
        }
    }
}
