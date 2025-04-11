package io.ecs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for logging with standardized format
 */
public class LogUtil {
    
    /**
     * Get a custom logger for the specified class that adds formatted prefixes to log messages
     * [INFO]  | message
     * [WARN]  | message
     * [ERROR] | message
     * 
     * @param clazz The class to create a logger for
     * @return CustomLogger instance
     */
    public static CustomLogger getLogger(Class<?> clazz) {
        return new CustomLogger(LoggerFactory.getLogger(clazz));
    }
    
    /**
     * Custom logger wrapper that adds formatted prefixes to log messages
     */
    public static class CustomLogger {
        private final Logger logger;
        
        public CustomLogger(Logger logger) {
            this.logger = logger;
        }
        
        /**
         * Log an info message with the [INFO] prefix
         * @param message Message to log
         */
        public void info(String message) {
            logger.info("[INFO]  | {}", message);
        }
        
        /**
         * Log an info message with the [INFO] prefix and one parameter
         * @param message Message to log
         * @param arg Parameter to substitute
         */
        public void info(String message, Object arg) {
            logger.info("[INFO]  | " + message, arg);
        }
        
        /**
         * Log an info message with the [INFO] prefix and two parameters
         * @param message Message to log
         * @param arg1 First parameter to substitute
         * @param arg2 Second parameter to substitute
         */
        public void info(String message, Object arg1, Object arg2) {
            logger.info("[INFO]  | " + message, arg1, arg2);
        }
        
        /**
         * Log an info message with the [INFO] prefix and multiple parameters
         * @param message Message to log
         * @param args Parameters to substitute
         */
        public void info(String message, Object... args) {
            logger.info("[INFO]  | " + message, args);
        }
        
        /**
         * Log a warning message with the [WARN] prefix
         * @param message Message to log
         */
        public void warn(String message) {
            logger.warn("[WARN]  | {}", message);
        }
        
        /**
         * Log a warning message with the [WARN] prefix and one parameter
         * @param message Message to log
         * @param arg Parameter to substitute
         */
        public void warn(String message, Object arg) {
            logger.warn("[WARN]  | " + message, arg);
        }
        
        /**
         * Log a warning message with the [WARN] prefix and two parameters
         * @param message Message to log
         * @param arg1 First parameter to substitute
         * @param arg2 Second parameter to substitute
         */
        public void warn(String message, Object arg1, Object arg2) {
            logger.warn("[WARN]  | " + message, arg1, arg2);
        }
        
        /**
         * Log a warning message with the [WARN] prefix and multiple parameters
         * @param message Message to log
         * @param args Parameters to substitute
         */
        public void warn(String message, Object... args) {
            logger.warn("[WARN]  | " + message, args);
        }
        
        /**
         * Log an error message with the [ERROR] prefix
         * @param message Message to log
         */
        public void error(String message) {
            logger.error("[ERROR] | {}", message);
        }
        
        /**
         * Log an error message with the [ERROR] prefix and one parameter
         * @param message Message to log
         * @param arg Parameter to substitute
         */
        public void error(String message, Object arg) {
            logger.error("[ERROR] | " + message, arg);
        }
        
        /**
         * Log an error message with the [ERROR] prefix and two parameters
         * @param message Message to log
         * @param arg1 First parameter to substitute
         * @param arg2 Second parameter to substitute
         */
        public void error(String message, Object arg1, Object arg2) {
            logger.error("[ERROR] | " + message, arg1, arg2);
        }
        
        /**
         * Log an error message with the [ERROR] prefix and multiple parameters
         * @param message Message to log
         * @param args Parameters to substitute
         */
        public void error(String message, Object... args) {
            logger.error("[ERROR] | " + message, args);
        }
        
        /**
         * Log an error message with the [ERROR] prefix and an exception
         * @param message Message to log
         * @param t Throwable to include in the log
         */
        public void error(String message, Throwable t) {
            logger.error("[ERROR] | " + message, t);
        }
        
        /**
         * Log a debug message with the [DEBUG] prefix
         * @param message Message to log
         */
        public void debug(String message) {
            logger.debug("[DEBUG] | {}", message);
        }
        
        /**
         * Log a debug message with the [DEBUG] prefix and one parameter
         * @param message Message to log
         * @param arg Parameter to substitute
         */
        public void debug(String message, Object arg) {
            logger.debug("[DEBUG] | " + message, arg);
        }
        
        /**
         * Log a debug message with the [DEBUG] prefix and multiple parameters
         * @param message Message to log
         * @param args Parameters to substitute
         */
        public void debug(String message, Object... args) {
            logger.debug("[DEBUG] | " + message, args);
        }
        
        /**
         * Check if debug logging is enabled
         * @return true if debug is enabled
         */
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }
        
        /**
         * Check if info logging is enabled
         * @return true if info is enabled
         */
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }
        
        /**
         * Get the underlying SLF4J logger
         * @return The SLF4J logger
         */
        public Logger getLogger() {
            return logger;
        }
    }
}