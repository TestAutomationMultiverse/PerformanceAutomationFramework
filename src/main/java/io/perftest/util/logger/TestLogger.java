package io.perftest.util.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Custom logger for the performance testing framework.
 * Provides methods for setting up loggers and tracking test execution.
 */
public class TestLogger {
    private static final ThreadLocal<String> TEST_EXECUTION_ID = new ThreadLocal<>();
    
    /**
     * Get a logger for a specific class
     * 
     * @param clazz The class for which to create a logger
     * @return An SLF4J Logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Start a new test execution and assign it a unique ID
     * 
     * @return The unique execution ID
     */
    public static String startTestExecution() {
        String executionId = UUID.randomUUID().toString();
        TEST_EXECUTION_ID.set(executionId);
        return executionId;
    }
    
    /**
     * Get the current test execution ID
     * 
     * @return The current execution ID, or null if none is set
     */
    public static String getTestExecutionId() {
        return TEST_EXECUTION_ID.get();
    }
    
    /**
     * End the current test execution and clear the ID
     */
    public static void endTestExecution() {
        TEST_EXECUTION_ID.remove();
    }
    
    /**
     * Log a message with the current test execution ID
     * 
     * @param logger The logger to use
     * @param level The log level
     * @param message The message to log
     * @param args The message arguments
     */
    public static void log(Logger logger, LogLevel level, String message, Object... args) {
        String executionId = getTestExecutionId();
        String formattedMessage = executionId == null ? message : String.format("[Execution: %s] %s", executionId, message);
        
        switch (level) {
            case TRACE:
                logger.trace(formattedMessage, args);
                break;
            case DEBUG:
                logger.debug(formattedMessage, args);
                break;
            case INFO:
                logger.info(formattedMessage, args);
                break;
            case WARN:
                logger.warn(formattedMessage, args);
                break;
            case ERROR:
                logger.error(formattedMessage, args);
                break;
        }
    }
    
    /**
     * Log levels for the custom logger
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
