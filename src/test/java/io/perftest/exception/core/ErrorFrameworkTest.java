package io.perftest.exception.core;

import io.perftest.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for error handling framework core functionality
 */
@DisplayName("Error Handling Framework Core Tests")
public class ErrorFrameworkTest {
    
    private static final Logger log = LoggerFactory.getLogger(ErrorFrameworkTest.class);
    
    /**
     * Test exception hierarchy
     */
    @Test
    @DisplayName("Test exception hierarchy")
    void testExceptionHierarchy() {
        // Test common base class
        PerfTestException baseException = new PerfTestException(ErrorCode.GENERAL_ERROR, "General error");
        ConfigException configException = new ConfigException(ErrorCode.CONFIG_ERROR, "Config error");
        ComponentException componentException = new ComponentException(ErrorCode.TEST_COMPONENT_ERROR, "Component error");
        
        assertTrue(baseException instanceof RuntimeException);
        assertTrue(configException instanceof PerfTestException);
        assertTrue(componentException instanceof PerfTestException);
        
        // Test error codes
        assertEquals(ErrorCode.GENERAL_ERROR, baseException.getErrorCode());
        assertEquals(ErrorCode.CONFIG_ERROR, configException.getErrorCode());
        assertEquals(ErrorCode.TEST_COMPONENT_ERROR, componentException.getErrorCode());
    }
    
    /**
     * Test error codes
     */
    @Test
    @DisplayName("Test error codes")
    void testErrorCodes() {
        // Test error code properties
        assertEquals(1000, ErrorCode.GENERAL_ERROR.getCode());
        assertEquals("General error", ErrorCode.GENERAL_ERROR.getDescription());
        
        // Test toString implementation
        assertEquals("[1000] General error", ErrorCode.GENERAL_ERROR.toString());
        assertEquals("[2000] Configuration error", ErrorCode.CONFIG_ERROR.toString());
    }
    
    /**
     * Test validation methods
     */
    @Test
    @DisplayName("Test validation methods")
    void testValidation() {
        // Test validateNotNull
        assertDoesNotThrow(() -> ErrorHandler.validateNotNull("not null", ErrorCode.VALIDATION_ERROR, "Value is null"));
        
        Exception e1 = assertThrows(PerfTestException.class, 
                () -> ErrorHandler.validateNotNull(null, ErrorCode.VALIDATION_ERROR, "Value is null"));
        assertEquals(ErrorCode.VALIDATION_ERROR, ((PerfTestException) e1).getErrorCode());
        
        // Test validateCondition
        assertDoesNotThrow(() -> ErrorHandler.validateCondition(true, ErrorCode.VALIDATION_ERROR, "Condition failed"));
        
        Exception e2 = assertThrows(PerfTestException.class, 
                () -> ErrorHandler.validateCondition(false, ErrorCode.VALIDATION_ERROR, "Condition failed"));
        assertEquals(ErrorCode.VALIDATION_ERROR, ((PerfTestException) e2).getErrorCode());
        assertEquals("Condition failed", e2.getMessage());
        
        // Test validate (alias for validateCondition)
        assertDoesNotThrow(() -> ErrorHandler.validate(true, ErrorCode.VALIDATION_ERROR, "Condition failed"));
        
        Exception e3 = assertThrows(PerfTestException.class, 
                () -> ErrorHandler.validate(false, ErrorCode.VALIDATION_ERROR, "Condition failed"));
        assertEquals(ErrorCode.VALIDATION_ERROR, ((PerfTestException) e3).getErrorCode());
        assertEquals("Condition failed", e3.getMessage());
    }
    
    /**
     * Test Result class
     */
    @Test
    @DisplayName("Test Result class")
    void testResultClass() {
        // Test success result
        Result<String> successResult = Result.success("test");
        assertTrue(successResult.isSuccess());
        assertFalse(successResult.isFailure());
        assertEquals("test", successResult.getValue());
        
        // Test failure result
        Result<String> failureResult = Result.failure(ErrorCode.CONFIG_ERROR, "Config error");
        assertFalse(failureResult.isSuccess());
        assertTrue(failureResult.isFailure());
        assertEquals(ErrorCode.CONFIG_ERROR, failureResult.getErrorCode());
        assertEquals("Config error", failureResult.getErrorMessage());
        assertNull(failureResult.getException());
        
        // Test getValue with failure
        Exception e = assertThrows(PerfTestException.class, failureResult::getValue);
        assertEquals(ErrorCode.CONFIG_ERROR, ((PerfTestException) e).getErrorCode());
        
        // Test getValueOrDefault
        assertEquals("test", successResult.getValueOrDefault("default"));
        assertEquals("default", failureResult.getValueOrDefault("default"));
        
        // Test failure with PerfTestException
        PerfTestException exception = new PerfTestException(ErrorCode.TEST_EXECUTION_ERROR, "Execution error");
        Result<String> exceptionResult = Result.failure(exception);
        
        assertFalse(exceptionResult.isSuccess());
        assertTrue(exceptionResult.isFailure());
        assertEquals(ErrorCode.TEST_EXECUTION_ERROR, exceptionResult.getErrorCode());
        assertEquals("Execution error", exceptionResult.getErrorMessage());
    }
    
    /**
     * Test Result transformation
     */
    @Test
    @DisplayName("Test Result transformation")
    void testResultTransformation() {
        Result<String> successResult = Result.success("test");
        Result<String> failureResult = Result.failure(ErrorCode.CONFIG_ERROR, "Config error");
        
        // Test map
        Result<Integer> mappedSuccess = successResult.map(String::length);
        assertTrue(mappedSuccess.isSuccess());
        assertEquals(4, mappedSuccess.getValue());
        
        Result<Integer> mappedFailure = failureResult.map(String::length);
        assertTrue(mappedFailure.isFailure());
        assertEquals(ErrorCode.CONFIG_ERROR, mappedFailure.getErrorCode());
        
        // Test flatMap
        Result<Integer> flatMappedSuccess = successResult.flatMap(s -> Result.success(s.length()));
        assertTrue(flatMappedSuccess.isSuccess());
        assertEquals(4, flatMappedSuccess.getValue());
        
        Result<Integer> flatMappedFailure = failureResult.flatMap(s -> Result.success(s.length()));
        assertTrue(flatMappedFailure.isFailure());
        assertEquals(ErrorCode.CONFIG_ERROR, flatMappedFailure.getErrorCode());
    }
    
    // Helper method to safely create a Supplier that throws an IOException for testing
    private <T> Supplier<T> ioExceptionSupplier() {
        return () -> {
            try {
                throw new IOException("IO error");
            } catch (IOException e) {
                // Convert checked exception to unchecked
                throw new RuntimeException(e);
            }
        };
    }
    
    /**
     * Test error handling methods
     */
    @Test
    @DisplayName("Test error handling methods")
    void testErrorHandling() throws IOException {
        // Test executeWithErrorHandling
        String result = ErrorHandler.executeWithErrorHandling(() -> "test", 
                ErrorCode.GENERAL_ERROR, "Execution failed", log);
        assertEquals("test", result);
        
        // Test with IOExceptions - we now need to handle the IOException
        // Test executeWithIOHandling with a supplier that throws IOException
        Exception e = assertThrows(IOException.class, () -> 
                ErrorHandler.executeWithIOHandling(() -> { throw new IOException("IO error"); }, 
                        ErrorCode.IO_ERROR, "Execution failed", log));
        
        assertTrue(e instanceof IOException);
        assertEquals("IO error", e.getMessage());
        
        // Test executeWithIOHandling with a supplier that returns a value
        String ioResult = ErrorHandler.executeWithIOHandling(() -> "test with io", 
                ErrorCode.IO_ERROR, "Execution failed", log);
        assertEquals("test with io", ioResult);
        
        // Test executeSafely
        Result<String> safeResult = ErrorHandler.executeSafely(() -> "test", 
                ErrorCode.GENERAL_ERROR, "Execution failed");
        assertTrue(safeResult.isSuccess());
        assertEquals("test", safeResult.getValue());
        
        // Use the helper method that safely wraps the IOException
        Result<String> safeFailure = ErrorHandler.executeSafely(ioExceptionSupplier(), 
                ErrorCode.IO_ERROR, "Execution failed");
        assertTrue(safeFailure.isFailure());
        assertEquals(ErrorCode.IO_ERROR, safeFailure.getErrorCode());
        assertTrue(safeFailure.getErrorMessage().contains("Execution failed"));
        assertTrue(safeFailure.getException().getCause() instanceof IOException);
    }
    
    /**
     * Test ErrorReporter
     */
    @Test
    @DisplayName("Test ErrorReporter")
    void testErrorReporter() {
        ErrorReporter reporter = ErrorReporter.getInstance();
        reporter.clearErrors();
        
        // Test reporting errors
        reporter.reportError(ErrorCode.GENERAL_ERROR, "General error");
        reporter.reportError(ErrorCode.CONFIG_ERROR, "Config error", new IOException("IO error"));
        
        PerfTestException exception = new PerfTestException(ErrorCode.TEST_EXECUTION_ERROR, "Execution error");
        reporter.reportError(exception);
        
        Result<String> failureResult = Result.failure(ErrorCode.VALIDATION_ERROR, "Validation error");
        reporter.reportError(failureResult);
        
        // Test error collection
        assertEquals(4, reporter.getErrorCount());
        assertTrue(reporter.hasErrors());
        
        // Test error summary
        String summary = reporter.getErrorSummary();
        assertTrue(summary.contains("Error summary (4 errors)"));
        assertTrue(summary.contains("General error"));
        assertTrue(summary.contains("Config error"));
        assertTrue(summary.contains("Execution error"));
        assertTrue(summary.contains("Validation error"));
    }
}
