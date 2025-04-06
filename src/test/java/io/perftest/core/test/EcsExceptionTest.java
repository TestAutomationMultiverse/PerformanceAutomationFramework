package io.perftest.core.test;

import io.perftest.ecs.exception.*;
import io.perftest.ecs.exception.entity.EntityException;
import io.perftest.ecs.exception.component.ComponentException;
import io.perftest.ecs.exception.system.SystemException;
import io.perftest.core.test.BaseTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the ECS-based exception framework.
 * Validates the behavior of the new exception hierarchy, error handling, and result patterns.
 */
public class EcsExceptionTest {
    
    @Test
    @DisplayName("Test ECS exception hierarchy")
    public void testEcsExceptionHierarchy() {
        // Create instances of each exception type
        EcsException ecsException = new EcsException("ECS Exception");
        EntityException entityException = new EntityException("Entity Exception");
        ComponentException componentException = new ComponentException("Component Exception");
        SystemException systemException = new SystemException("System Exception");
        
        // Verify inheritance relationships
        assertTrue(entityException instanceof EcsException);
        assertTrue(componentException instanceof EcsException);
        assertTrue(systemException instanceof EcsException);
        
        // Verify exception messages
        assertEquals("ECS Exception", ecsException.getMessage());
        assertEquals("Entity Exception", entityException.getMessage());
        assertEquals("Component Exception", componentException.getMessage());
        assertEquals("System Exception", systemException.getMessage());
        
        // Verify default error codes
        assertEquals(ErrorCode.UNEXPECTED_ERROR, ecsException.getErrorCode());
        assertEquals(ErrorCode.UNEXPECTED_ERROR, entityException.getErrorCode());
        assertEquals(ErrorCode.UNEXPECTED_ERROR, componentException.getErrorCode());
        assertEquals(ErrorCode.UNEXPECTED_ERROR, systemException.getErrorCode());
    }
    
    @Test
    @DisplayName("Test ECS exception with error codes")
    public void testEcsExceptionWithErrorCodes() {
        // Create exceptions with specific error codes
        EcsException ecsException = new EcsException(ErrorCode.ENTITY_GENERAL_ERROR, "Entity error");
        
        // Verify error codes are set correctly
        assertEquals(ErrorCode.ENTITY_GENERAL_ERROR, ecsException.getErrorCode());
        assertEquals("Entity error", ecsException.getMessage());
    }
    
    @Test
    @DisplayName("Test Result success and failure")
    public void testResultSuccessAndFailure() {
        // Create a successful result
        Result<String> successResult = Result.success("Success value");
        
        // Create a failure result
        Result<String> failureResult = Result.failure(ErrorCode.COMPONENT_GENERAL_ERROR, "Component error");
        
        // Verify result properties
        assertTrue(successResult.isSuccess());
        assertFalse(successResult.isFailure());
        assertEquals("Success value", successResult.getValue());
        
        assertTrue(failureResult.isFailure());
        assertFalse(failureResult.isSuccess());
        assertEquals(ErrorCode.COMPONENT_GENERAL_ERROR, failureResult.getErrorCode());
        assertEquals("Component error", failureResult.getErrorMessage());
        
        // Verify exception thrown when accessing value of failure result
        assertThrows(EcsException.class, () -> failureResult.getValue());
    }
    
    @Test
    @DisplayName("Test Result mapping and recovery")
    public void testResultMappingAndRecovery() {
        // Create results
        Result<Integer> successResult = Result.success(5);
        Result<Integer> failureResult = Result.failure(ErrorCode.IO_ERROR, "IO error");
        
        // Test map operation
        Result<String> mappedSuccess = successResult.map(i -> "Value is " + i);
        Result<String> mappedFailure = failureResult.map(i -> "Value is " + i);
        
        assertTrue(mappedSuccess.isSuccess());
        assertEquals("Value is 5", mappedSuccess.getValue());
        
        assertTrue(mappedFailure.isFailure());
        assertEquals(ErrorCode.IO_ERROR, mappedFailure.getErrorCode());
        
        // Test recovery
        Result<Integer> recoveredResult = failureResult.recoverWith(10);
        
        assertTrue(recoveredResult.isSuccess());
        assertEquals(10, recoveredResult.getValue());
    }
    
    @Test
    @DisplayName("Test ErrorHandler and ErrorReporter")
    public void testErrorHandlerAndReporter() {
        // Test error handler
        Exception testException = new RuntimeException("Test exception");
        Result<String> handledResult = ErrorHandler.handleException(testException);
        
        assertTrue(handledResult.isFailure());
        assertEquals(ErrorCode.UNEXPECTED_ERROR, handledResult.getErrorCode());
        assertEquals(testException, handledResult.getException());
        
        // Test try-execute
        Result<String> tryResult = ErrorHandler.tryExecute(
                v -> "Success", 
                ErrorCode.VALIDATION_ERROR, 
                "Error executing function"
        );
        
        assertTrue(tryResult.isSuccess());
        assertEquals("Success", tryResult.getValue());
        
        // Test error reporter - this just verifies it doesn't throw exceptions
        ErrorReporter.reportError(ErrorCode.NETWORK_ERROR, "Network error");
        ErrorReporter.reportWarning(ErrorCode.CONFIG_GENERAL_ERROR, "Config warning");
        
        // Test exception creation
        EcsException createdEx = ErrorReporter.createException(
                ErrorCode.TEMPLATE_ERROR, 
                "Template error", 
                testException
        );
        
        assertEquals(ErrorCode.TEMPLATE_ERROR, createdEx.getErrorCode());
        assertEquals("Template error", createdEx.getMessage());
        assertEquals(testException, createdEx.getCause());
    }
}
