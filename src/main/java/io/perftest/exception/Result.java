package io.perftest.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A result container that can represent either a successful result or a failure
 * @param <T> The type of the successful result
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The value of the successful result, null if failure
     */
    private final T value;
    
    /**
     * The error code of the failure, null if success
     */
    @Getter
    private final ErrorCode errorCode;
    
    /**
     * The error message of the failure, null if success
     */
    @Getter
    private final String errorMessage;
    
    /**
     * The exception that caused the failure, null if success or if there was no exception
     */
    @Getter
    private final Throwable exception;
    
    /**
     * Create a successful result
     * @param value The value of the successful result
     * @param <T> The type of the successful result
     * @return A successful result
     */
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, null, null);
    }
    
    /**
     * Create a failure result
     * @param errorCode The error code of the failure
     * @param errorMessage The error message of the failure
     * @param <T> The type of the successful result
     * @return A failure result
     */
    public static <T> Result<T> failure(ErrorCode errorCode, String errorMessage) {
        return new Result<>(null, errorCode, errorMessage, null);
    }
    
    /**
     * Create a failure result
     * @param errorCode The error code of the failure
     * @param errorMessage The error message of the failure
     * @param exception The exception that caused the failure
     * @param <T> Type of the successful result
     * @return A failure result
     */
    public static <T> Result<T> failure(ErrorCode errorCode, String errorMessage, Throwable exception) {
        return new Result<>(null, errorCode, errorMessage, exception);
    }
    
    /**
     * Create a failure result from a PerfTestException
     * @param exception The exception that caused the failure
     * @param <T> Type of the successful result
     * @return A failure result
     */
    public static <T> Result<T> failure(PerfTestException exception) {
        return new Result<>(null, exception.getErrorCode(), exception.getMessage(), exception.getCause());
    }
    
    /**
     * Convert a supplier that may throw an exception to a Result
     * @param supplier The supplier that may throw an exception
     * @param errorCode The error code to use if an exception is thrown
     * @param errorMessage The error message to use if an exception is thrown
     * @param <T> The type of the successful result
     * @return A Result
     */
    public static <T> Result<T> of(Supplier<T> supplier, ErrorCode errorCode, String errorMessage) {
        try {
            return success(supplier.get());
        } catch (Exception e) {
            return failure(errorCode, errorMessage, e);
        }
    }
    
    /**
     * Check if the result is a success
     * @return True if the result is a success, false otherwise
     */
    public boolean isSuccess() {
        return errorCode == null;
    }
    
    /**
     * Check if the result is a failure
     * @return True if the result is a failure, false otherwise
     */
    public boolean isFailure() {
        return errorCode != null;
    }
    
    /**
     * Get the value of the successful result
     * @return The value of the successful result
     * @throws PerfTestException If the result is a failure
     */
    public T getValue() {
        if (isFailure()) {
            throw new PerfTestException(errorCode, errorMessage, exception);
        }
        return value;
    }
    
    /**
     * Get the value of the successful result or a default value if the result is a failure
     * @param defaultValue The default value to return if the result is a failure
     * @return The value of the successful result or the default value
     */
    public T getValueOrDefault(T defaultValue) {
        return isSuccess() ? value : defaultValue;
    }
    
    /**
     * Get the value of the successful result or the value provided by the supplier if the result is a failure
     * @param supplier The supplier to provide a value if the result is a failure
     * @return The value of the successful result or the supplied value
     */
    public T getValueOrElse(Supplier<T> supplier) {
        return isSuccess() ? value : supplier.get();
    }
    
    /**
     * Apply a transformation function to the value of the successful result
     * @param mapper The function to apply to the value
     * @param <U> The type of the transformed value
     * @return A Result containing the transformed value, or a failure if the original result was a failure
     */
    public <U> Result<U> map(Function<T, U> mapper) {
        if (isSuccess()) {
            try {
                return Result.success(mapper.apply(value));
            } catch (Exception e) {
                return Result.failure(ErrorCode.UNEXPECTED_ERROR, 
                        "Error applying mapper function: " + e.getMessage(), e);
            }
        } else {
            return Result.failure(errorCode, errorMessage, exception);
        }
    }
    
    /**
     * Apply a transformation function that returns a Result to the value of the successful result
     * @param mapper The function to apply to the value
     * @param <U> The type of the transformed value
     * @return The Result returned by the mapper function, or a failure if the original result was a failure
     */
    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        if (isSuccess()) {
            try {
                return mapper.apply(value);
            } catch (Exception e) {
                return Result.failure(ErrorCode.UNEXPECTED_ERROR, 
                        "Error applying flatMap function: " + e.getMessage(), e);
            }
        } else {
            return Result.failure(errorCode, errorMessage, exception);
        }
    }
    
    /**
     * Handle a failure by converting it to a success
     * @param handler Function to handle the failure and provide a replacement value
     * @return A successful result with either the original value or the replacement value
     */
    public Result<T> recover(Function<Result<T>, T> handler) {
        if (isSuccess()) {
            return this;
        } else {
            try {
                return Result.success(handler.apply(this));
            } catch (Exception e) {
                return Result.failure(ErrorCode.UNEXPECTED_ERROR, 
                        "Error applying recovery function: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Handle a failure by converting it to a success using a fallback value
     * @param fallbackValue The fallback value to use if the result is a failure
     * @return A successful result with either the original value or the fallback value
     */
    public Result<T> recoverWith(T fallbackValue) {
        if (isSuccess()) {
            return this;
        } else {
            return Result.success(fallbackValue);
        }
    }
    
    /**
     * Execute a function if the result is a success
     * @param consumer The function to execute
     * @return The original result
     */
    public Result<T> onSuccess(Consumer<T> consumer) {
        if (isSuccess()) {
            try {
                consumer.accept(value);
            } catch (Exception e) {
                // We ignore exceptions in the consumer to avoid changing the result
            }
        }
        return this;
    }
    
    /**
     * Execute a function if the result is a failure
     * @param consumer The function to execute
     * @return The original result
     */
    public Result<T> onFailure(Consumer<Result<T>> consumer) {
        if (isFailure()) {
            try {
                consumer.accept(this);
            } catch (Exception e) {
                // We ignore exceptions in the consumer to avoid changing the result
            }
        }
        return this;
    }
    
    /**
     * Get a value by applying the appropriate function based on whether the result is a success or failure
     * @param successMapper Function to apply to the value if the result is a success
     * @param failureMapper Function to apply to the error if the result is a failure
     * @param <U> The type of the result of the mapper functions
     * @return The result of applying the appropriate mapper function
     */
    public <U> U fold(Function<T, U> successMapper, Function<Result<T>, U> failureMapper) {
        if (isSuccess()) {
            return successMapper.apply(value);
        } else {
            return failureMapper.apply(this);
        }
    }
    
    /**
     * Get a string representation of the result
     * @return String representation
     */
    @Override
    public String toString() {
        if (isSuccess()) {
            return "Success: " + value;
        } else {
            StringBuilder sb = new StringBuilder("Failure: [")
                    .append(errorCode)
                    .append("] ")
                    .append(errorMessage);
            
            if (exception != null) {
                sb.append(" - ").append(exception.getClass().getSimpleName())
                        .append(": ").append(exception.getMessage());
            }
            
            return sb.toString();
        }
    }
}
