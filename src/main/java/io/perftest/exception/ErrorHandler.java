package io.perftest.exception;

import java.io.IOException;

import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * Utility class for error handling
 */
public class ErrorHandler {
    
    private ErrorHandler() {
        // Utility class, no instantiation
    }
    
    /**
     * Validate that a value is not null
     * @param value Value to validate
     * @param errorCode Error code to use if validation fails
     * @param errorMessage Error message to use if validation fails
     * @param <T> Type of the value
     * @throws PerfTestException If validation fails
     */
    public static <T> void validateNotNull(T value, ErrorCode errorCode, String errorMessage) {
        if (value == null) {
            throw new PerfTestException(errorCode, errorMessage);
        }
    }
    
    /**
     * Validate that a condition is true
     * @param condition Condition to validate
     * @param errorCode Error code to use if validation fails
     * @param errorMessage Error message to use if validation fails
     * @throws PerfTestException If validation fails
     */
    public static void validateCondition(boolean condition, ErrorCode errorCode, String errorMessage) {
        if (!condition) {
            throw new PerfTestException(errorCode, errorMessage);
        }
    }
    
    /**
     * Validate that a condition is true (alias for validateCondition)
     * @param condition Condition to validate
     * @param errorCode Error code to use if validation fails
     * @param errorMessage Error message to use if validation fails
     * @throws PerfTestException If validation fails
     */
    public static void validate(boolean condition, ErrorCode errorCode, String errorMessage) {
        validateCondition(condition, errorCode, errorMessage);
    }
    
    /**
     * Validate that a string is not null or empty
     * @param value String to validate
     * @param errorCode Error code to use if validation fails
     * @param errorMessage Error message to use if validation fails
     * @throws PerfTestException If validation fails
     */
    public static void validateNotEmpty(String value, ErrorCode errorCode, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new PerfTestException(errorCode, errorMessage);
        }
    }
    
    /**
     * Execute a supplier with error handling
     * @param supplier The supplier to execute
     * @param errorCode Error code to use if an exception occurs
     * @param errorMessage Error message to use if an exception occurs
     * @param logger Logger to use for logging
     * @param <T> Type of the result
     * @return The result of the supplier
     * @throws PerfTestException If an exception occurs
     */
    public static <T> T executeWithErrorHandling(Supplier<T> supplier, ErrorCode errorCode, 
                                                String errorMessage, Logger logger) {
        try {
            return supplier.get();
        } catch (Exception e) {
            logger.error("{}: {}", errorMessage, e.getMessage(), e);
            throw new PerfTestException(errorCode, errorMessage + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute a supplier with error handling, propagating IOException
     * @param supplier The supplier to execute that may throw IOException
     * @param errorCode Error code to use if a non-IOException exception occurs
     * @param errorMessage Error message to use if an exception occurs
     * @param logger Logger to use for logging
     * @param <T> Type of the result
     * @return The result of the supplier
     * @throws PerfTestException If a non-IOException exception occurs
     * @throws IOException If an IOException occurs
     */
    @FunctionalInterface
    public interface IOSupplier<T> {
        T get() throws IOException;
    }
    
    public static <T> T executeWithIOHandling(IOSupplier<T> supplier, ErrorCode errorCode, 
                                             String errorMessage, Logger logger) throws IOException {
        try {
            return supplier.get();
        } catch (IOException e) {
            // Let IOExceptions propagate
            throw e;
        } catch (Exception e) {
            logger.error("{}: {}", errorMessage, e.getMessage(), e);
            throw new PerfTestException(errorCode, errorMessage + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute a supplier with specific exception handling
     * @param supplier The supplier to execute
     * @param exceptionClass The type of exception to handle
     * @param errorCode Error code to use if an exception occurs
     * @param errorMessage Error message to use if an exception occurs
     * @param logger Logger to use for logging
     * @param <T> Type of the result
     * @param <E> Type of the exception
     * @return The result of the supplier
     * @throws PerfTestException If an exception occurs
     */
    public static <T, E extends Exception> T executeWithErrorHandling(Supplier<T> supplier, 
                                                                    Class<E> exceptionClass,
                                                                    ErrorCode errorCode, 
                                                                    String errorMessage, 
                                                                    Logger logger) {
        try {
            return supplier.get();
        } catch (Exception e) {
            // Check if this is the expected exception class or if the cause is that exception
            if (exceptionClass.isInstance(e) || 
                (e.getCause() != null && exceptionClass.isInstance(e.getCause())) || 
                (e instanceof PerfTestException && ((PerfTestException)e).getErrorCode() == ErrorCode.IO_ERROR)) {
                logger.error("{}: {}", errorMessage, e.getMessage(), e);
                throw new PerfTestException(errorCode, errorMessage + ": " + e.getMessage(), e);
            } else {
                throw e;
            }
        }
    }
    
    /**
     * Execute a runnable with error handling
     * @param runnable The runnable to execute
     * @param errorCode Error code to use if an exception occurs
     * @param errorMessage Error message to use if an exception occurs
     * @param logger Logger to use for logging
     * @throws PerfTestException If an exception occurs
     */
    public static void executeWithErrorHandling(Runnable runnable, ErrorCode errorCode, 
                                              String errorMessage, Logger logger) {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.error("{}: {}", errorMessage, e.getMessage(), e);
            throw new PerfTestException(errorCode, errorMessage + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute a supplier and return a Result
     * @param supplier The supplier to execute
     * @param errorCode Error code to use if an exception occurs
     * @param errorMessage Error message to use if an exception occurs
     * @param <T> Type of the result
     * @return A Result containing the result or an error
     */
    public static <T> Result<T> executeSafely(Supplier<T> supplier, ErrorCode errorCode, String errorMessage) {
        try {
            return Result.success(supplier.get());
        } catch (Exception e) {
            return Result.failure(errorCode, errorMessage + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute a runnable and return a Result
     * @param runnable The runnable to execute
     * @param errorCode Error code to use if an exception occurs
     * @param errorMessage Error message to use if an exception occurs
     * @return A Result indicating success or failure
     */
    public static Result<Void> executeSafely(Runnable runnable, ErrorCode errorCode, String errorMessage) {
        try {
            runnable.run();
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(errorCode, errorMessage + ": " + e.getMessage(), e);
        }
    }
}
