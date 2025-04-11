package io.ecs;

import io.ecs.config.TestConfiguration;
import io.ecs.config.YamlConfig;
import io.ecs.engine.JMTreeBuilderEngine;
import io.ecs.model.ExecutionConfig;
import io.ecs.model.TestResult;
import io.ecs.system.TestExecutionSystem;
import io.ecs.util.FileUtils;
import io.ecs.util.EcsLogger;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JMeterTreeBuilderTest - YAML Configuration Test Class
 * 
 * This class demonstrates how to use the YAML configuration-driven approach 
 * for defining and executing performance test scenarios with JMeter TreeBuilder.
 * 
 * Only YAML configuration tests are included in this version, unit tests have been removed.
 * 
 * How to run:
 * - With Maven: mvn test -Dtest=JMeterTreeBuilderTest
 * - From IDE: Run this class as a JUnit test
 */
public class JMeterTreeBuilderTest {
    
    private static final EcsLogger logger = EcsLogger.getLogger(JMeterTreeBuilderTest.class);
    private static final String DEFAULT_CONFIG_FILE = "src/test/resources/configs/jmeter_tree_config.yaml";
    
    /**
     * JUnit test for the YAML configuration functionality with JMeter TreeBuilder
     */
    @Test
    public void testYamlConfigTest() throws Exception {
        logger.info("Running JUnit test for yamlConfigTest with JMeter TreeBuilder");
        
        // Check if the default config file exists
        File configFile = new File(DEFAULT_CONFIG_FILE);
        if (!configFile.exists()) {
            logger.warn("Default config file not found: {}", DEFAULT_CONFIG_FILE);
            return; // Skip this test if the file doesn't exist
        }
        
        // Create test system
        TestExecutionSystem testSystem = new TestExecutionSystem("target/reports/jmeter-treebuilder-test");
        testSystem.setDefaultEngine("jmtree");
        
        try {
            // Load and parse YAML configuration
            String yamlContent = FileUtils.readFileAsString(DEFAULT_CONFIG_FILE);
            YamlConfig yamlConfig = new YamlConfig();
            TestConfiguration testConfig = yamlConfig.parse(yamlContent);
            
            // For each scenario in the config, we'll create it with modified execution parameters
            if (testConfig.getExecutionConfig() != null) {
                ExecutionConfig eConfig = testConfig.getExecutionConfig();
                // Reduce test load for unit tests
                eConfig.setThreads(Math.min(1, eConfig.getThreads()));
                eConfig.setIterations(Math.min(1, eConfig.getIterations()));
                eConfig.setRampUpSeconds(Math.min(1, eConfig.getRampUpSeconds()));
                eConfig.setHoldSeconds(Math.min(1, eConfig.getHoldSeconds()));
                // We'll use this config when adding scenarios
            }
            
            // Add global variables
            if (testConfig.getVariables() != null && !testConfig.getVariables().isEmpty()) {
                testSystem.setGlobalVariables(testConfig.getVariables());
            }
            
            // Add dynamic runtime variables
            Map<String, String> runtimeVars = new HashMap<>();
            runtimeVars.put("testRunId", "JM-TREE-" + System.currentTimeMillis());
            runtimeVars.put("currentTimestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
            testSystem.addGlobalVariables(runtimeVars);
            
            // Add all scenarios from YAML config
            for (io.ecs.config.Scenario scenario : testConfig.getScenarios()) {
                testSystem.addScenario(scenario);
            }
            
            // Run all scenarios
            List<Map<String, Object>> results = testSystem.executeAllScenarios();
            
            // Verify results
            assertNotNull(results, "Results should not be null");
            assertFalse(results.isEmpty(), "Results should not be empty");
            
            // Print metrics for each scenario
            for (int i = 0; i < results.size(); i++) {
                Map<String, Object> scenarioMetrics = results.get(i);
                String scenarioName = testConfig.getScenarios().get(i).getName();
                logger.info("Metrics for scenario '{}': {}", scenarioName, scenarioMetrics);
                
                // Verify success rate
                Object successRate = scenarioMetrics.get("successRate");
                if (successRate instanceof Number) {
                    double rate = ((Number) successRate).doubleValue();
                    logger.info("Success rate for '{}': {}%", scenarioName, rate);
                }
            }
            
            logger.info("JMeter TreeBuilder YAML test completed successfully");
        } catch (Exception e) {
            logger.error("Error in JMeter TreeBuilder test: {}", e.getMessage(), e);
            throw e;
        } finally {
            testSystem.shutdown();
        }
    }
    
    /**
     * Main entry point for executing performance tests from the command line
     * 
     * @param args Optional arguments. If provided, the first argument is treated as a YAML configuration file path
     */
    public static void main(String[] args) {
        try {
            // Use config file path from args or default
            String configFile = args.length > 0 ? args[0] : DEFAULT_CONFIG_FILE;
            
            // Create test system with report directory
            TestExecutionSystem testSystem = new TestExecutionSystem("target/reports/jmeter-treebuilder-test");
            testSystem.setDefaultEngine("jmtree");
            
            // Load and parse YAML configuration
            String yamlContent = FileUtils.readFileAsString(configFile);
            YamlConfig yamlConfig = new YamlConfig();
            TestConfiguration testConfig = yamlConfig.parse(yamlContent);
            
            // Configure system from YAML
            if (testConfig.getExecutionConfig() != null) {
                // We'll use execution config params directly in scenarios
                // No need to set at system level
            }
            
            // Add global variables
            if (testConfig.getVariables() != null && !testConfig.getVariables().isEmpty()) {
                testSystem.setGlobalVariables(testConfig.getVariables());
            }
            
            // Add dynamic runtime variables
            Map<String, String> runtimeVars = new HashMap<>();
            runtimeVars.put("testRunId", "JM-TREE-" + System.currentTimeMillis());
            runtimeVars.put("currentTimestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
            testSystem.addGlobalVariables(runtimeVars);
            
            // Add all scenarios from YAML config
            for (io.ecs.config.Scenario scenario : testConfig.getScenarios()) {
                testSystem.addScenario(scenario);
            }
            
            // Execute all scenarios
            List<Map<String, Object>> results = testSystem.executeAllScenarios();
            
            // Print metrics for each scenario
            for (int i = 0; i < results.size(); i++) {
                Map<String, Object> scenarioMetrics = results.get(i);
                String scenarioName = testConfig.getScenarios().get(i).getName();
                logger.info("Metrics for scenario '{}': {}", scenarioName, scenarioMetrics);
            }
            
            logger.info("JMeter TreeBuilder tests from command line completed successfully");
            
            // Shutdown the system
            testSystem.shutdown();
            
        } catch (Exception e) {
            logger.error("Error executing JMeter TreeBuilder tests: {}", e.getMessage(), e);
        }
    }
}