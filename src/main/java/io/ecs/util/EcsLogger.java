package io.ecs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * ECS Logging utility that provides standardized formatting and additional
 * context for log messages throughout the application.
 * 
 * This class wraps SLF4J loggers and adds the following features:
 * - Standardized log format with level prefixes ([INFO], [WARN], etc.)
 * - Support for correlation IDs for tracking requests
 * - Supports logging with context enrichment
 * - Maintains compatibility with SLF4J
 */
public class EcsLogger {
    // Constants for MDC keys
    public static final String CORRELATION_ID = "correlationId";
    public static final String COMPONENT = "component";
    public static final String TEST_NAME = "testName";
    
    private final Logger logger;
    private final String componentName;
    
    /**
     * Private constructor to create a logger instance
     * 
     * @param logger The SLF4J logger
     * @param componentName The component name
     */
    private EcsLogger(Logger logger, String componentName) {
        this.logger = logger;
        this.componentName = componentName;
    }
    
    /**
     * Get a logger for the specified class
     * 
     * @param clazz The class to get a logger for
     * @return A new EcsLogger instance
     */
    public static EcsLogger getLogger(Class<?> clazz) {
        return new EcsLogger(LoggerFactory.getLogger(clazz), clazz.getSimpleName());
    }
    
    /**
     * Get a logger with a specified component name
     * 
     * @param clazz The class to get a logger for
     * @param componentName The component name to use
     * @return A new EcsLogger instance
     */
    public static EcsLogger getLogger(Class<?> clazz, String componentName) {
        return new EcsLogger(LoggerFactory.getLogger(clazz), componentName);
    }
    
    /**
     * Start a new correlation context with a generated ID
     * 
     * @return The generated correlation ID
     */
    public String startCorrelationContext() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID, correlationId);
        MDC.put(COMPONENT, componentName);
        return correlationId;
    }
    
    /**
     * Start a new correlation context with the specified ID
     * 
     * @param correlationId The correlation ID to use
     */
    public void startCorrelationContext(String correlationId) {
        MDC.put(CORRELATION_ID, correlationId);
        MDC.put(COMPONENT, componentName);
    }
    
    /**
     * Set a test name in the logging context
     * 
     * @param testName The name of the test being executed
     */
    public void setTestName(String testName) {
        MDC.put(TEST_NAME, testName);
    }
    
    /**
     * End the current correlation context
     */
    public void endCorrelationContext() {
        MDC.remove(CORRELATION_ID);
        MDC.remove(COMPONENT);
        MDC.remove(TEST_NAME);
    }
    
    /**
     * Log an info message
     * 
     * @param message The message to log
     */
    public void info(String message) {
        logger.info("[INFO]  | {}", message);
    }
    
    /**
     * Log an info message with a parameter
     * 
     * @param message The message to log
     * @param arg The parameter to substitute in the message
     */
    public void info(String message, Object arg) {
        logger.info("[INFO]  | " + message, arg);
    }
    
    /**
     * Log an info message with two parameters
     * 
     * @param message The message to log
     * @param arg1 The first parameter to substitute
     * @param arg2 The second parameter to substitute
     */
    public void info(String message, Object arg1, Object arg2) {
        logger.info("[INFO]  | " + message, arg1, arg2);
    }
    
    /**
     * Log an info message with multiple parameters
     * 
     * @param message The message to log
     * @param args The parameters to substitute
     */
    public void info(String message, Object... args) {
        logger.info("[INFO]  | " + message, args);
    }
    
    /**
     * Log a warning message
     * 
     * @param message The message to log
     */
    public void warn(String message) {
        logger.warn("[WARN]  | {}", message);
    }
    
    /**
     * Log a warning message with a parameter
     * 
     * @param message The message to log
     * @param arg The parameter to substitute
     */
    public void warn(String message, Object arg) {
        logger.warn("[WARN]  | " + message, arg);
    }
    
    /**
     * Log a warning message with two parameters
     * 
     * @param message The message to log
     * @param arg1 The first parameter to substitute
     * @param arg2 The second parameter to substitute
     */
    public void warn(String message, Object arg1, Object arg2) {
        logger.warn("[WARN]  | " + message, arg1, arg2);
    }
    
    /**
     * Log a warning message with multiple parameters
     * 
     * @param message The message to log
     * @param args The parameters to substitute
     */
    public void warn(String message, Object... args) {
        logger.warn("[WARN]  | " + message, args);
    }
    
    /**
     * Log an error message
     * 
     * @param message The message to log
     */
    public void error(String message) {
        logger.error("[ERROR] | {}", message);
    }
    
    /**
     * Log an error message with a parameter
     * 
     * @param message The message to log
     * @param arg The parameter to substitute
     */
    public void error(String message, Object arg) {
        logger.error("[ERROR] | " + message, arg);
    }
    
    /**
     * Log an error message with two parameters
     * 
     * @param message The message to log
     * @param arg1 The first parameter to substitute
     * @param arg2 The second parameter to substitute
     */
    public void error(String message, Object arg1, Object arg2) {
        logger.error("[ERROR] | " + message, arg1, arg2);
    }
    
    /**
     * Log an error message with an exception
     * 
     * @param message The message to log
     * @param t The throwable to log
     */
    public void error(String message, Throwable t) {
        logger.error("[ERROR] | " + message, t);
    }
    
    /**
     * Log an error message with multiple parameters
     * 
     * @param message The message to log
     * @param args The parameters to substitute
     */
    public void error(String message, Object... args) {
        logger.error("[ERROR] | " + message, args);
    }
    
    /**
     * Log a debug message
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        logger.debug("[DEBUG] | {}", message);
    }
    
    /**
     * Log a debug message with a parameter
     * 
     * @param message The message to log
     * @param arg The parameter to substitute
     */
    public void debug(String message, Object arg) {
        logger.debug("[DEBUG] | " + message, arg);
    }
    
    /**
     * Log a debug message with two parameters
     * 
     * @param message The message to log
     * @param arg1 The first parameter to substitute
     * @param arg2 The second parameter to substitute
     */
    public void debug(String message, Object arg1, Object arg2) {
        logger.debug("[DEBUG] | " + message, arg1, arg2);
    }
    
    /**
     * Log a debug message with multiple parameters
     * 
     * @param message The message to log
     * @param args The parameters to substitute
     */
    public void debug(String message, Object... args) {
        logger.debug("[DEBUG] | " + message, args);
    }
    
    /**
     * Check if debug logging is enabled
     * 
     * @return true if debug is enabled
     */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
    
    /**
     * Get the underlying SLF4J logger
     * 
     * @return The SLF4J logger
     */
    public Logger getSlf4jLogger() {
        return logger;
    }
}