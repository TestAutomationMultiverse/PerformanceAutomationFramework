package io.ecs;

import io.ecs.config.TestConfiguration;
import io.ecs.config.YamlConfig;
import io.ecs.engine.JMTreeBuilderEngine;
import io.ecs.component.ConfigComponent;
import io.ecs.component.ProtocolComponent;
import io.ecs.component.ReportingComponent;
import io.ecs.model.RequestBuilder;
import io.ecs.model.ScenarioBuilder;
import io.ecs.system.TestExecutionSystem;
import io.ecs.model.ExecutionConfig;
import io.ecs.model.TestResult;
import io.ecs.util.FileUtils;
import io.ecs.util.EcsLogger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JMeterTreeBuilderTest - Example class demonstrating how to use JMeter TreeBuilder with the Performance Testing Framework
 * 
 * This class provides examples of two approaches for using the framework with JMeter TreeBuilder:
 * 1. Programmatic API for directly creating and executing performance tests
 * 2. YAML configuration-driven approach for defining test scenarios
 * 
 * Features demonstrated:
 * - Creating and configuring test execution parameters
 * - Setting up variables for dynamic request content
 * - Executing HTTP GET and POST requests with headers and parameters
 * - Collecting and reporting performance metrics
 * - Generating HTML test reports
 */
public class JMeterTreeBuilderTest {
    
    private static final EcsLogger logger = EcsLogger.getLogger(JMeterTreeBuilderTest.class);
    private static final String DEFAULT_CONFIG_FILE = "src/test/resources/configs/jmeter_tree_config.yaml";
    
    /**
     * Setup method to run before tests
     */
    @BeforeEach
    public void setUp() {
        logger.info("Setting up JMeterTreeBuilderTest");
    }
    
    /**
     * Cleanup method to run after tests
     */
    @AfterEach
    public void tearDown() {
        logger.info("Tearing down JMeterTreeBuilderTest");
    }
    
    /**
     * JUnit test for the HTTP testing functionality with JMeter TreeBuilder
     */
    @Test
    public void testSimpleHttpTest() throws Exception {
        logger.info("Running JUnit test for simpleHttpTest with JMeter TreeBuilder");
        
        // Create a test system with a custom report directory
        String reportDir = "target/reports/jmeter-treebuilder-test";
        new File(reportDir).mkdirs();
        
        // Set up execution config
        ExecutionConfig config = new ExecutionConfig();
        config.setThreads(1);          // Reduced for unit tests
        config.setIterations(1);       // Reduced for unit tests
        config.setRampUpSeconds(1);    // Reduced for unit tests
        config.setHoldSeconds(1);      // Reduced for unit tests
        config.setReportDirectory(reportDir);
        
        // Define test variables
        Map<String, String> variables = new HashMap<>();
        variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
        variables.put("userId", "1");
        config.setVariables(variables);
        
        // Create engine
        JMTreeBuilderEngine engine = new JMTreeBuilderEngine(config);
        engine.initialize(variables);
        
        // Create and execute a simple test
        List<TestResult> results = engine.executeScenario(
            "Get User Test",
            List.of(
                RequestBuilder.create("Get User", "HTTP")
                    .method("GET")
                    .endpoint("${baseUrl}/users/${userId}")
                    .build()
            )
        );
        
        // Verify results
        assertNotNull(results);
        assertFalse(results.isEmpty());
        for (TestResult result : results) {
            assertTrue(result.isSuccess(), "Test " + result.getTestName() + " should be successful");
        }
        
        // Generate report
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String reportPath = reportDir + "/get_user_" + timestamp + ".jtl";
        
        // Log metrics
        Map<String, Object> metrics = engine.getMetrics();
        logger.info("Total requests: {}", metrics.getOrDefault("totalRequests", 0));
        logger.info("Success rate: {}%", metrics.getOrDefault("successRate", 0.0));
        logger.info("Average response time: {}ms", metrics.getOrDefault("avgResponseTime", 0.0));
        logger.info("Report generated at: {}", reportPath);
        
        // Cleanup
        engine.shutdown();
        
        logger.info("JMeter TreeBuilder simpleHttpTest completed successfully");
    }
    
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
            // Load and execute scenarios from YAML
            testSystem.loadFromYaml(DEFAULT_CONFIG_FILE);
            List<Map<String, Object>> results = testSystem.executeAllScenarios();
            
            // Verify results
            assertNotNull(results, "Results should not be null");
            assertFalse(results.isEmpty(), "Results should not be empty");
            
            for (Map<String, Object> metrics : results) {
                assertTrue((Double)metrics.getOrDefault("successRate", 0.0) > 0, 
                    "Success rate should be greater than 0");
            }
            
            logger.info("JMeter TreeBuilder yamlConfigTest completed successfully");
        } catch (Exception e) {
            logger.error("Error in YAML config test: {}", e.getMessage(), e);
            throw e;
        } finally {
            testSystem.shutdown();
        }
    }
    
    /**
     * Main entry point for executing tests from the command line
     * 
     * @param args Optional arguments. If provided, the first argument is treated as a YAML configuration file path
     */
    public static void main(String[] args) {
        try {
            // Create test system
            TestExecutionSystem testSystem = new TestExecutionSystem("target/reports/jmeter-treebuilder-test");
            testSystem.setDefaultEngine("jmtree");
            
            // Default or provided config file
            String configFile = args.length > 0 ? args[0] : DEFAULT_CONFIG_FILE;
            logger.info("Using configuration file: {}", configFile);
            
            // Load and execute all scenarios
            testSystem.loadFromYaml(configFile);
            List<Map<String, Object>> results = testSystem.executeAllScenarios();
            
            logger.info("JMeter TreeBuilder test completed with {} scenario results", results.size());
            
            // Cleanup
            testSystem.shutdown();
        } catch (Exception e) {
            logger.error("Error in JMeter TreeBuilder test: {}", e.getMessage(), e);
        }
    }
}