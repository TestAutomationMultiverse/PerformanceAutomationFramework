package io.ecs;

import io.ecs.engine.Engine;
import io.ecs.engine.JMTreeBuilderEngine;
import io.ecs.model.ExecutionConfig;
import io.ecs.model.Request;
import io.ecs.model.TestResult;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example test class demonstrating the use of the experimental JMeter TreeBuilder Engine.
 * This uses JMeter's standard API to construct and execute HTTP performance tests using
 * the programmatic approach described in JMeter's documentation.
 * 
 * Reference documentation:
 * - https://jmeter.apache.org/api/index.html
 * - https://jmeter.apache.org/usermanual/build-programmatic-test-plan.html
 */
public class JMeterTreeBuilderTest {
    private static final Logger logger = LoggerFactory.getLogger(JMeterTreeBuilderTest.class);

    /**
     * JUnit test for the TreeBuilder engine functionality
     */
    @Test
    public void testTreeBuilderEngine() throws Exception {
        logger.info("=== Starting JMeter TreeBuilder Performance Test (JUnit) ===");
        
        // Create execution configuration
        ExecutionConfig config = new ExecutionConfig();
        config.setThreads(1);
        config.setIterations(1);
        config.setRampUpSeconds(1);
        config.setHoldSeconds(1);
        
        // Create the engine
        Engine engine = new JMTreeBuilderEngine(config);
        
        // Set global variables
        Map<String, String> variables = new HashMap<>();
        variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
        variables.put("userId", "1");
        engine.initialize(variables);
        
        // Setup test scenario - REST API Test
        String scenarioName = "REST API Test";
        List<Request> requests = new ArrayList<>();
        
        // GET Request
        Request getRequest = new Request();
        getRequest.setName("Get User");
        getRequest.setProtocol("http");
        getRequest.setMethod("GET");
        getRequest.setEndpoint(variables.get("baseUrl") + "/users/" + variables.get("userId"));
        Map<String, String> getHeaders = new HashMap<>();
        getHeaders.put("Accept", "application/json");
        getRequest.setHeaders(getHeaders);
        requests.add(getRequest);
        
        // Execute the scenario
        logger.info("Executing scenario: {}", scenarioName);
        List<TestResult> results = engine.executeScenario(scenarioName, requests);
        
        // Validate results
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Results should not be empty");
        
        // Verify all tests were successful
        for (TestResult result : results) {
            assertTrue(result.isSuccess(), "Test " + result.getTestName() + " should be successful");
        }
        
        // Shut down the engine - this will generate the HTML report
        engine.shutdown();
        
        logger.info("=== JMeter TreeBuilder Performance Test (JUnit) Completed Successfully ===");
    }
    
    /**
     * Main method for running the test outside of JUnit
     */
    public static void main(String[] args) {
        logger.info("=== Starting JMeter TreeBuilder Performance Test ===");
        
        try {
            // Create execution configuration
            ExecutionConfig config = new ExecutionConfig();
            config.setThreads(2);
            config.setIterations(2);
            config.setRampUpSeconds(1);
            config.setHoldSeconds(2);
            
            // Create the engine
            Engine engine = new JMTreeBuilderEngine(config);
            
            // Set global variables
            Map<String, String> variables = new HashMap<>();
            variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
            variables.put("userId", "1");
            engine.initialize(variables);
            
            // Setup test scenario - REST API Test
            String scenarioName = "REST API Test";
            List<Request> requests = new ArrayList<>();
            
            // GET Request
            Request getRequest = new Request();
            getRequest.setName("Get User");
            getRequest.setProtocol("http");
            getRequest.setMethod("GET");
            getRequest.setEndpoint(variables.get("baseUrl") + "/users/" + variables.get("userId"));
            Map<String, String> getHeaders = new HashMap<>();
            getHeaders.put("Accept", "application/json");
            getRequest.setHeaders(getHeaders);
            requests.add(getRequest);
            
            // GET Posts Request
            Request getPostsRequest = new Request();
            getPostsRequest.setName("Get Posts");
            getPostsRequest.setProtocol("http");
            getPostsRequest.setMethod("GET");
            getPostsRequest.setEndpoint(variables.get("baseUrl") + "/posts");
            getPostsRequest.setHeaders(getHeaders);
            requests.add(getPostsRequest);
            
            // POST Request
            Request postRequest = new Request();
            postRequest.setName("Create Post");
            postRequest.setProtocol("http");
            postRequest.setMethod("POST");
            postRequest.setEndpoint(variables.get("baseUrl") + "/posts");
            Map<String, String> postHeaders = new HashMap<>();
            postHeaders.put("Content-Type", "application/json");
            postHeaders.put("Accept", "application/json");
            postRequest.setHeaders(postHeaders);
            String postBody = "{\"title\": \"Test Post\", \"body\": \"This is a test post\", \"userId\": " + variables.get("userId") + "}";
            postRequest.setBody(postBody);
            requests.add(postRequest);
            
            // Execute the scenario
            logger.info("Executing scenario: {}", scenarioName);
            List<TestResult> results = engine.executeScenario(scenarioName, requests);
            
            // Retrieve and display metrics
            Map<String, Object> metrics = engine.getMetrics();
            logger.info("Test Results Summary:");
            logger.info("- Total Requests: {}", results.size());
            logger.info("- Success Rate: {}%", metrics.get("successRate"));
            logger.info("- Avg Response Time: {}ms", metrics.get("avgResponseTime"));
            logger.info("- 90th Percentile: {}ms", metrics.get("90thPercentile"));
            
            // Shut down the engine - this will generate the HTML report
            engine.shutdown();
            
            logger.info("=== JMeter TreeBuilder Performance Test Completed Successfully ===");
            
        } catch (Exception e) {
            logger.error("Error executing JMeter TreeBuilder test: {}", e.getMessage(), e);
        }
    }
}