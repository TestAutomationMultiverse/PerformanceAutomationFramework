package io.perftest.exception;

import io.perftest.config.ConfigManager;
import io.perftest.config.YamlConfigUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for error handling framework
 */
@DisplayName("Error Handling Framework Tests")
public class ErrorHandlingTest {
    
    private static final Logger log = LoggerFactory.getLogger(ErrorHandlingTest.class);
    private ConfigManager configManager;
    
    /**
     * Initialize test
     */
    @BeforeEach
    void setUp() {
        configManager = ConfigManager.getInstance();
        configManager.clearConfig();
    }
    
    /**
     * Test basic exception creation and properties
     */
    @Test
    @DisplayName("Test PerfTestException creation and properties")
    void testPerfTestException() {
        PerfTestException exception = new PerfTestException(ErrorCode.GENERAL_ERROR, "Test error");
        
        assertEquals(ErrorCode.GENERAL_ERROR, exception.getErrorCode());
        assertEquals("Test error", exception.getMessage());
        assertNull(exception.getCause());
        
        Exception cause = new IOException("IO error");
        PerfTestException exceptionWithCause = new PerfTestException(ErrorCode.IO_ERROR, "Test error with cause", cause);
        
        assertEquals(ErrorCode.IO_ERROR, exceptionWithCause.getErrorCode());
        assertEquals("Test error with cause", exceptionWithCause.getMessage());
        assertEquals(cause, exceptionWithCause.getCause());
    }
    
    /**
     * Test ConfigException creation and properties
     */
    @Test
    @DisplayName("Test ConfigException creation and properties")
    void testConfigException() {
        ConfigException exception = new ConfigException(ErrorCode.CONFIG_ERROR, "Config error");
        
        assertEquals(ErrorCode.CONFIG_ERROR, exception.getErrorCode());
        assertEquals("Config error", exception.getMessage());
        
        Exception cause = new IOException("IO error");
        ConfigException exceptionWithCause = new ConfigException(ErrorCode.CONFIG_FILE_NOT_FOUND, "Config error with cause", cause);
        
        assertEquals(ErrorCode.CONFIG_FILE_NOT_FOUND, exceptionWithCause.getErrorCode());
        assertEquals("Config error with cause", exceptionWithCause.getMessage());
        assertEquals(cause, exceptionWithCause.getCause());
    }
    
    /**
     * Test Result creation and mapping
     */
    @Test
    @DisplayName("Test Result creation and mapping")
    void testResult() {
        // Test success result
        Result<String> successResult = Result.success("test");
        assertTrue(successResult.isSuccess());
        assertFalse(successResult.isFailure());
        assertEquals("test", successResult.getValue());
        assertNull(successResult.getErrorCode());
        assertNull(successResult.getErrorMessage());
        assertNull(successResult.getException());
        
        // Test failure result
        Result<String> failureResult = Result.failure(ErrorCode.CONFIG_ERROR, "Config error");
        assertFalse(failureResult.isSuccess());
        assertTrue(failureResult.isFailure());
        assertThrows(PerfTestException.class, failureResult::getValue);
        assertEquals(ErrorCode.CONFIG_ERROR, failureResult.getErrorCode());
        assertEquals("Config error", failureResult.getErrorMessage());
        assertNull(failureResult.getException());
        
        // Test failure result with exception
        Exception cause = new IOException("IO error");
        Result<String> failureResultWithException = Result.failure(ErrorCode.IO_ERROR, "IO error", cause);
        assertFalse(failureResultWithException.isSuccess());
        assertTrue(failureResultWithException.isFailure());
        assertThrows(PerfTestException.class, failureResultWithException::getValue);
        assertEquals(ErrorCode.IO_ERROR, failureResultWithException.getErrorCode());
        assertEquals("IO error", failureResultWithException.getErrorMessage());
        assertEquals(cause, failureResultWithException.getException());
        
        // Test mapping
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
        
        // Test recover
        Result<String> recoveredSuccess = successResult.recover(error -> "recovered");
        assertTrue(recoveredSuccess.isSuccess());
        assertEquals("test", recoveredSuccess.getValue());
        
        Result<String> recoveredFailure = failureResult.recover(error -> "recovered");
        assertTrue(recoveredFailure.isSuccess());
        assertEquals("recovered", recoveredFailure.getValue());
        
        // Test fold
        String foldedSuccess = successResult.fold(value -> "Success: " + value, error -> "Error: " + error.getErrorMessage());
        assertEquals("Success: test", foldedSuccess);
        
        String foldedFailure = failureResult.fold(value -> "Success: " + value, error -> "Error: " + error.getErrorMessage());
        assertEquals("Error: Config error", foldedFailure);
    }
    
    /**
     * Test ErrorHandler validation methods
     */
    @Test
    @DisplayName("Test ErrorHandler validation methods")
    void testErrorHandlerValidation() {
        // Test validateNotNull with null value
        PerfTestException nullException = assertThrows(PerfTestException.class, () -> 
                ErrorHandler.validateNotNull(null, ErrorCode.VALIDATION_ERROR, "Value cannot be null"));
        assertEquals(ErrorCode.VALIDATION_ERROR, nullException.getErrorCode());
        assertEquals("Value cannot be null", nullException.getMessage());
        
        // Test validateNotNull with non-null value
        assertDoesNotThrow(() -> ErrorHandler.validateNotNull("test", ErrorCode.VALIDATION_ERROR, "Value cannot be null"));
        
        // Test validateCondition with false condition
        PerfTestException conditionException = assertThrows(PerfTestException.class, () -> 
                ErrorHandler.validateCondition(false, ErrorCode.VALIDATION_ERROR, "Condition failed"));
        assertEquals(ErrorCode.VALIDATION_ERROR, conditionException.getErrorCode());
        assertEquals("Condition failed", conditionException.getMessage());
        
        // Test validateCondition with true condition
        assertDoesNotThrow(() -> ErrorHandler.validateCondition(true, ErrorCode.VALIDATION_ERROR, "Condition failed"));
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
     * Test ErrorHandler execution methods
     */
    @Test
    @DisplayName("Test ErrorHandler execution methods")
    void testErrorHandlerExecution() throws IOException {
        // Test successful execution
        String result = ErrorHandler.executeWithErrorHandling(() -> "test", 
                ErrorCode.GENERAL_ERROR, "Failed to execute", log);
        assertEquals("test", result);
        
        // Test failed execution with standard error handling
        PerfTestException executionException = assertThrows(PerfTestException.class, () -> 
                ErrorHandler.executeWithErrorHandling(ioExceptionSupplier(), 
                        ErrorCode.IO_ERROR, "Failed to execute", log));
        assertEquals(ErrorCode.IO_ERROR, executionException.getErrorCode());
        assertTrue(executionException.getMessage().contains("Failed to execute"));
        assertTrue(executionException.getCause() instanceof RuntimeException);
        assertTrue(executionException.getCause().getCause() instanceof IOException);
        
        // Test IOExecution that throws IOException and propagates it
        Exception ioException = assertThrows(IOException.class, () ->
                ErrorHandler.executeWithIOHandling(() -> { throw new IOException("IO error"); },
                        ErrorCode.IO_ERROR, "IO failure", log));
        assertTrue(ioException instanceof IOException);
        assertEquals("IO error", ioException.getMessage());
        
        // Test IOExecution with success
        String ioResult = ErrorHandler.executeWithIOHandling(() -> "success with io",
                ErrorCode.IO_ERROR, "IO failure", log);
        assertEquals("success with io", ioResult);
        
        // Test execution with specific exception
        PerfTestException specificException = assertThrows(PerfTestException.class, () -> 
                ErrorHandler.executeWithErrorHandling(ioExceptionSupplier(), 
                        IOException.class, ErrorCode.IO_ERROR, "IO failure", log));
        assertEquals(ErrorCode.IO_ERROR, specificException.getErrorCode());
        assertTrue(specificException.getMessage().contains("IO failure"));
        
        // Test safe execution that succeeds
        Result<String> safeResult = ErrorHandler.executeSafely(() -> "test", 
                ErrorCode.GENERAL_ERROR, "Failed to execute safely");
        assertTrue(safeResult.isSuccess());
        assertEquals("test", safeResult.getValue());
        
        // Test safe execution that fails
        Result<String> safeFailure = ErrorHandler.executeSafely(ioExceptionSupplier(), 
                ErrorCode.IO_ERROR, "Failed to execute safely");
        assertTrue(safeFailure.isFailure());
        assertEquals(ErrorCode.IO_ERROR, safeFailure.getErrorCode());
        assertTrue(safeFailure.getErrorMessage().contains("Failed to execute safely"));
        assertTrue(safeFailure.getException() instanceof RuntimeException);
        assertTrue(safeFailure.getException().getCause() instanceof IOException);
    }
    
    /**
     * Test ConfigManager error handling
     */
    @Test
    @DisplayName("Test ConfigManager error handling")
    void testConfigManagerErrorHandling() {
        // Test null key validation
        PerfTestException nullKeyException = assertThrows(PerfTestException.class, () -> 
                configManager.setConfig(null, "value"));
        assertEquals(ErrorCode.CONFIG_ERROR, nullKeyException.getErrorCode());
        
        PerfTestException nullValueException = assertThrows(PerfTestException.class, () -> 
                configManager.setConfig("key", null));
        assertEquals(ErrorCode.CONFIG_ERROR, nullValueException.getErrorCode());
        
        // Test safe set config
        Result<Void> safeSetResult = configManager.safeSetConfig("test", "value");
        assertTrue(safeSetResult.isSuccess());
        
        Result<Void> safeSetNullKeyResult = configManager.safeSetConfig(null, "value");
        assertTrue(safeSetNullKeyResult.isFailure());
        assertEquals(ErrorCode.CONFIG_ERROR, safeSetNullKeyResult.getErrorCode());
        
        // Test get config for missing key
        PerfTestException missingKeyException = assertThrows(PerfTestException.class, () -> 
                configManager.getConfig("missing"));
        assertEquals(ErrorCode.CONFIG_ERROR, missingKeyException.getErrorCode());
        
        // Test safe get config
        configManager.setConfig("safeKey", "safeValue");
        Result<String> safeGetResult = configManager.safeGetConfig("safeKey");
        assertTrue(safeGetResult.isSuccess());
        assertEquals("safeValue", safeGetResult.getValue());
        
        Result<String> safeGetMissingResult = configManager.safeGetConfig("missing");
        assertTrue(safeGetMissingResult.isFailure());
        assertEquals(ErrorCode.CONFIG_ERROR, safeGetMissingResult.getErrorCode());
        
        // Test get config or default
        String defaultValue = configManager.getConfigOrDefault("missing", "default");
        assertEquals("default", defaultValue);
        
        // Test get config as optional
        Optional<String> optionalValue = configManager.getConfigAsOptional("safeKey");
        assertTrue(optionalValue.isPresent());
        assertEquals("safeValue", optionalValue.get());
        
        Optional<String> optionalMissing = configManager.getConfigAsOptional("missing");
        assertFalse(optionalMissing.isPresent());
    }
    
    /**
     * Define a simple test config class for YAML testing
     */
    public static class TestConfig {
        public String name;
        public int value;
        
        public TestConfig() {
            // Required for Jackson
        }
        
        public TestConfig(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
