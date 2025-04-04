package io.perftest.exception;

/**
 * Exception thrown for HTTP-related errors.
 */
public class HttpException extends PerfTestException {
    
    public HttpException(String message) {
        super(ErrorCode.HTTP_ERROR, message);
    }
    
    public HttpException(String message, Throwable cause) {
        super(ErrorCode.HTTP_ERROR, message, cause);
    }
    
    public HttpException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public HttpException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
