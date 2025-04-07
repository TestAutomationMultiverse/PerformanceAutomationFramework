package io.perftest.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.perftest.ecs.exception.EcsException;
import io.perftest.ecs.exception.component.ComponentException;
import io.perftest.ecs.exception.config.ConfigException;
import io.perftest.ecs.exception.entity.EntityException;
import io.perftest.ecs.exception.system.SystemException;
import io.perftest.ecs.exception.validation.ValidationException;

/**
 * Test class that validates the new exception framework's functionality.
 */
public class ErrorFrameworkTest {

    @Test
    @DisplayName("Test exception hierarchy")
    public void testExceptionHierarchy() {
        // Create instances of each exception type
        EcsException ecsException = new EcsException("ECS Exception");
        EntityException entityException = new EntityException("Entity Exception");
        ComponentException componentException = new ComponentException("Component Exception");
        SystemException systemException = new SystemException("System Exception");
        ConfigException configException = new ConfigException("Config Exception");
        ValidationException validationException = new ValidationException("Validation Exception");

        // Verify inheritance relationships
        assertTrue(entityException instanceof EcsException);
        assertTrue(componentException instanceof EcsException);
        assertTrue(systemException instanceof EcsException);
        assertTrue(configException instanceof EcsException);
        assertTrue(validationException instanceof EcsException);

        // Verify exception messages
        assertEquals("ECS Exception", ecsException.getMessage());
        assertEquals("Entity Exception", entityException.getMessage());
        assertEquals("Component Exception", componentException.getMessage());
        assertEquals("System Exception", systemException.getMessage());
        assertEquals("Config Exception", configException.getMessage());
        assertEquals("Validation Exception", validationException.getMessage());
    }

    @Test
    @DisplayName("Test exception with causes")
    public void testExceptionWithCauses() {
        // Create an exception with a cause
        Exception cause = new RuntimeException("Original cause");
        EcsException ecsException = new EcsException("ECS Exception with cause", cause);

        // Verify cause is properly set
        assertNotNull(ecsException.getCause());
        assertEquals("Original cause", ecsException.getCause().getMessage());
    }
}
