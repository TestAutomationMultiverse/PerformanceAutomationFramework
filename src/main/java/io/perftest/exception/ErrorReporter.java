package io.perftest.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for reporting and collecting errors
 */
@Slf4j
public class ErrorReporter {
    
    private static ErrorReporter instance;
    
    /**
     * Error entry class for storing error information
     */
    public static class ErrorEntry {
        private final ErrorCode errorCode;
        private final String message;
        private final Throwable exception;
        private final long timestamp;
        
        /**
         * Constructor
         * @param errorCode Error code
         * @param message Error message
         * @param exception Exception that caused the error
         */
        public ErrorEntry(ErrorCode errorCode, String message, Throwable exception) {
            this.errorCode = errorCode;
            this.message = message;
            this.exception = exception;
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Get the error code
         * @return Error code
         */
        public ErrorCode getErrorCode() {
            return errorCode;
        }
        
        /**
         * Get the error message
         * @return Error message
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Get the exception
         * @return Exception
         */
        public Throwable getException() {
            return exception;
        }
        
        /**
         * Get the timestamp
         * @return Timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Get a string representation of the error entry
         * @return String representation
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Error [").append(errorCode).append("]: ").append(message);
            if (exception != null) {
                sb.append(" - ").append(exception.getClass().getSimpleName());
                if (exception.getMessage() != null) {
                    sb.append(": ").append(exception.getMessage());
                }
            }
            return sb.toString();
        }
    }
    
    private final List<ErrorEntry> errors = Collections.synchronizedList(new ArrayList<>());
    private boolean collectingErrors = true;
    
    /**
     * Private constructor for singleton pattern
     */
    private ErrorReporter() {
        log.info("Initializing error reporter");
    }
    
    /**
     * Get the singleton instance
     * @return The ErrorReporter instance
     */
    public static synchronized ErrorReporter getInstance() {
        if (instance == null) {
            instance = new ErrorReporter();
        }
        return instance;
    }
    
    /**
     * Start collecting errors
     */
    public void startCollecting() {
        collectingErrors = true;
    }
    
    /**
     * Stop collecting errors
     */
    public void stopCollecting() {
        collectingErrors = false;
    }
    
    /**
     * Clear all collected errors
     */
    public void clearErrors() {
        log.debug("Clearing all errors");
        errors.clear();
    }
    
    /**
     * Report an error
     * @param errorCode Error code
     * @param message Error message
     */
    public void reportError(ErrorCode errorCode, String message) {
        log.error("Error [{}]: {}", errorCode, message);
        if (collectingErrors) {
            errors.add(new ErrorEntry(errorCode, message, null));
        }
    }
    
    /**
     * Report an error
     * @param errorCode Error code
     * @param message Error message
     * @param exception Exception that caused the error
     */
    public void reportError(ErrorCode errorCode, String message, Throwable exception) {
        log.error("Error [{}]: {}", errorCode, message, exception);
        if (collectingErrors) {
            errors.add(new ErrorEntry(errorCode, message, exception));
        }
    }
    
    /**
     * Report an error from a PerfTestException
     * @param exception The exception to report
     */
    public void reportError(PerfTestException exception) {
        log.error("Error [{}]: {}", exception.getErrorCode(), exception.getMessage(), exception);
        if (collectingErrors) {
            errors.add(new ErrorEntry(exception.getErrorCode(), exception.getMessage(), exception.getCause()));
        }
    }
    
    /**
     * Report an error from a Result
     * @param result The result to report
     * @param <T> Type of the result
     */
    public <T> void reportError(Result<T> result) {
        if (result.isFailure()) {
            log.error("Error [{}]: {}", result.getErrorCode(), result.getErrorMessage(), result.getException());
            if (collectingErrors) {
                errors.add(new ErrorEntry(result.getErrorCode(), result.getErrorMessage(), result.getException()));
            }
        }
    }
    
    /**
     * Get all collected errors
     * @return List of errors
     */
    public List<ErrorEntry> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    /**
     * Check if there are any errors
     * @return True if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Get the number of errors
     * @return Number of errors
     */
    public int getErrorCount() {
        return errors.size();
    }
    
    /**
     * Get a summary of all errors
     * @return String containing error summary
     */
    public String getErrorSummary() {
        if (errors.isEmpty()) {
            return "No errors reported";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Error summary (").append(errors.size()).append(" errors):\n");
        
        for (int i = 0; i < errors.size(); i++) {
            ErrorEntry error = errors.get(i);
            sb.append(i + 1).append(". ").append(error.toString()).append("\n");
        }
        
        return sb.toString();
    }
}
