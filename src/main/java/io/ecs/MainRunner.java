package io.ecs;

import io.ecs.config.TestConfiguration;
import io.ecs.config.YamlConfig;
import io.ecs.model.Scenario;
import io.ecs.util.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point for the Performance Testing Framework
 * 
 * This class provides a unified command-line interface for the framework
 * using the Entity-Component-System architectural pattern.
 * 
 * Features:
 * - YAML configuration file processing
 * - Variable substitution
 * - Multiple test protocol support
 * - Detailed metrics collection and reporting
 * 
 * Usage:
 * java -jar performance-framework.jar [config-file.yaml]
 * 
 * If no config file is provided, the runner will use the default config at
 * src/test/resources/configs/sample_config.yaml
 */
public class MainRunner {
    private static final Logger logger = LoggerFactory.getLogger(MainRunner.class);
    private static final String DEFAULT_CONFIG_FILE = "src/test/resources/configs/sample_config.yaml";
    
    public static void main(String[] args) {
        logger.info("Starting Performance Testing Framework");
        
        String configFile = DEFAULT_CONFIG_FILE;
        if (args.length > 0) {
            configFile = args[0];
            logger.info("Using custom config file: {}", configFile);
        } else {
            logger.info("Using default config file: {}", configFile);
        }
        
        try {
            // Check if config file exists
            File file = new File(configFile);
            if (!file.exists()) {
                logger.error("Configuration file not found: {}", configFile);
                System.err.println("Error: Configuration file '" + configFile + "' not found.");
                System.exit(1);
            }
            
            // Load and process YAML configuration using the ECS pattern
            executeUsingECSPattern(configFile);
            
            logger.info("Performance Testing Framework execution completed successfully");
        } catch (Exception e) {
            logger.error("Error executing performance tests: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Execute tests based on a YAML configuration file using the ECS pattern
     * 
     * @param configFile Path to the YAML configuration file
     * @throws Exception If an error occurs during execution
     */
    public static void executeUsingECSPattern(String configFile) throws Exception {
        logger.info("Executing tests from YAML config using ECS pattern: {}", configFile);
        
        // Create the test execution system (primary ECS system)
        TestExecutionSystem testSystem = new TestExecutionSystem("target/reports");
        
        // Set up global dynamic variables
        Map<String, String> globalVariables = new HashMap<>();
        globalVariables.put("timestamp", String.valueOf(System.currentTimeMillis()));
        globalVariables.put("date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        testSystem.setGlobalVariables(globalVariables);
        
        try {
            // Load scenarios from YAML config
            testSystem.loadFromYaml(configFile);
            
            // Execute all loaded scenarios
            testSystem.executeAllScenarios();
        } finally {
            // Clean up resources
            testSystem.shutdown();
        }
    }
    
    /**
     * Alternative execution method using the original approach
     * This is kept for backwards compatibility but delegates to the ECS implementation
     * 
     * @param configFile Path to the YAML configuration file
     * @throws Exception If an error occurs during execution
     */
    public static void executeFromYamlConfig(String configFile) throws Exception {
        logger.info("Executing tests from YAML config (legacy method): {}", configFile);
        executeUsingECSPattern(configFile);
    }
}