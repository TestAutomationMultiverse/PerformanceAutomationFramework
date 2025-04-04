package io.perftest.exception;

/**
 * Exception thrown for SOAP-related errors.
 */
public class SoapException extends PerfTestException {
    
    public SoapException(String message) {
        super(ErrorCode.SOAP_ERROR, message);
    }
    
    public SoapException(String message, Throwable cause) {
        super(ErrorCode.SOAP_ERROR, message, cause);
    }
    
    public SoapException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public SoapException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
