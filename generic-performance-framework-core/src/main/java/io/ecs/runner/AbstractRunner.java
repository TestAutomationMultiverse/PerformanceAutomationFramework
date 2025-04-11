package io.ecs.runner;

import io.ecs.system.TestExecutionSystem;
import io.ecs.util.EcsLogger;
import io.ecs.util.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * AbstractRunner - Base class for all performance test runners
 * 
 * This abstract class provides common functionality for all test runners:
 * - Command line argument handling
 * - Configuration file loading
 * - TestExecutionSystem setup
 * - Report directory management
 * - Consistent logging
 */
public abstract class AbstractRunner {
    
    protected final EcsLogger logger;
    protected final String defaultConfigFile;
    protected final String engineType;
    protected final String reportDirectory;
    
    /**
     * Create a new runner with the specified engine and default config
     * 
     * @param loggerClass Class to use for logger name
     * @param engineType Engine type identifier (e.g., "jmdsl", "gatling", "jmtree")
     * @param defaultConfigFile Default configuration file path
     * @param reportDirectory Directory for test reports
     */
    protected AbstractRunner(Class<?> loggerClass, String engineType, 
                           String defaultConfigFile, String reportDirectory) {
        this.logger = EcsLogger.getLogger(loggerClass);
        this.engineType = engineType;
        this.defaultConfigFile = defaultConfigFile;
        this.reportDirectory = reportDirectory;
    }
    
    /**
     * Run tests with the specified configuration file
     * 
     * @param configFile Configuration file path, or null to use default
     * @return List of result metrics from test execution
     * @throws Exception If an error occurs during test execution
     */
    public List<Map<String, Object>> run(String configFile) throws Exception {
        logger.info("Starting {} runner...", engineType.toUpperCase());
        
        // Use provided config file or default
        String effectiveConfigFile = configFile != null ? configFile : defaultConfigFile;
        logger.info("Using configuration file: {}", effectiveConfigFile);
        
        // Check if the config file exists
        File file = new File(effectiveConfigFile);
        if (!file.exists()) {
            logger.error("Configuration file not found: {}", effectiveConfigFile);
            throw new IllegalArgumentException("Configuration file not found: " + effectiveConfigFile);
        }
        
        // Create test system with the reporting directory
        TestExecutionSystem testSystem = new TestExecutionSystem(reportDirectory);
        
        // Configure system to use the specified engine
        testSystem.setDefaultEngine(engineType);
        
        try {
            // Load scenarios from YAML
            testSystem.loadFromYaml(effectiveConfigFile);
            
            // Execute all scenarios
            logger.info("Executing all scenarios from config...");
            List<Map<String, Object>> results = testSystem.executeAllScenarios();
            
            // Log results summary
            logResultsSummary(results);
            
            logger.info("{} execution completed successfully", engineType.toUpperCase());
            
            return results;
        } finally {
            // Clean up
            testSystem.shutdown();
        }
    }
    
    /**
     * Log a summary of test execution results
     * 
     * @param results List of result metrics from test execution
     */
    protected void logResultsSummary(List<Map<String, Object>> results) {
        logger.info("{} execution completed with {} results", engineType.toUpperCase(), results.size());
        
        for (int i = 0; i < results.size(); i++) {
            Map<String, Object> metrics = results.get(i);
            logger.info("Scenario {} results:", i + 1);
            logger.info("  Total requests: {}", metrics.getOrDefault("totalRequests", 0));
            logger.info("  Success rate: {}%", metrics.getOrDefault("successRate", 0.0));
            logger.info("  Average response time: {}ms", metrics.getOrDefault("avgResponseTime", 0.0));
            logger.info("  Max response time: {}ms", metrics.getOrDefault("maxResponseTime", 0));
        }
    }
    
    /**
     * Main method implementation for all runners
     * 
     * @param args Command line arguments (optional configuration file path)
     * @param runner Runner instance to execute
     */
    protected static void runMain(String[] args, AbstractRunner runner) {
        try {
            // Use command line arg if provided
            String configFile = args.length > 0 ? args[0] : null;
            
            // Run the tests
            runner.run(configFile);
            
        } catch (Exception e) {
            runner.logger.error("Error executing {}: {}", 
                runner.engineType.toUpperCase(), e.getMessage(), e);
            System.exit(1);
        }
    }
}