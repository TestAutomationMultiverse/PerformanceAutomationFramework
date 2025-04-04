package io.perftest.exception;

/**
 * Exception thrown for JDBC-related errors.
 */
public class JdbcException extends PerfTestException {
    
    public JdbcException(String message) {
        super(ErrorCode.JDBC_ERROR, message);
    }
    
    public JdbcException(String message, Throwable cause) {
        super(ErrorCode.JDBC_ERROR, message, cause);
    }
    
    public JdbcException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public JdbcException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
