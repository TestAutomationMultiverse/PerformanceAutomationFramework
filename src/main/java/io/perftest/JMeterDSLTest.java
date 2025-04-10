package io.perftest;

import io.perftest.ecs.ECSTestRunner;
import io.perftest.ecs.components.RequestBuilder;
import io.perftest.ecs.components.ScenarioBuilder;
import io.perftest.model.Scenario;
import io.perftest.model.TestResult;
import io.perftest.model.ExecutionConfig;
import io.perftest.engine.JMDSLEngine;
import io.perftest.protocol.HttpProtocol;
import io.perftest.protocol.Protocol;
import io.perftest.protocol.ProtocolFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example of using the JMeter DSL through the ECS test framework
 * This shows how to use the JMeter DSL engine without going through the App class
 */
public class JMeterDSLTest {
    
    private static final Logger logger = LoggerFactory.getLogger(JMeterDSLTest.class);
    
    public static void main(String[] args) {
        System.out.println("\n=== Starting JMeter DSL Performance Test using ECS Framework ===\n");
        
        try {
            // Check if the App class is being invoked instead (when run from workflow)
            if (args.length > 0 && args[0].endsWith(".yaml")) {
                System.out.println("Config file detected: " + args[0] + " - proceeding with JMeterDSLTest instead");
            }
            
            // First run API tests using the ECS Framework
            runECSApiTests();
            
            // Then run direct HTTP tests using JMeterDSL engine
            runDirectHttpTests();
            
            System.out.println("\n=== JMeter DSL Performance Test Completed Successfully ===\n");
            
        } catch (Exception e) {
            System.err.println("ERROR in JMeter DSL Test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run tests using the ECS Framework
     */
    private static void runECSApiTests() throws Exception {
        System.out.println("\n--- Running ECS API Tests ---\n");
    
        // Create a test runner with a custom report directory
        ECSTestRunner runner = new ECSTestRunner("target/reports/jmeter-dsl-test");
        
        // Add global variables
        Map<String, String> variables = new HashMap<>();
        variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
        variables.put("timeout", "5000");
        variables.put("username", "testuser");
        variables.put("password", "password123");
        runner.addGlobalVariables(variables);
        
        // Create a REST API load test scenario
        Scenario restApiScenario = ScenarioBuilder.create("REST API Test", "jmdsl")
            .threads(2)  // Reduced thread count for faster execution
            .iterations(2)  // Reduced iterations for faster execution
            .rampUp(1)
            .hold(2)
            .variable("userId", "1")
            .build();
        
        // Add requests to the scenario using the builder
        restApiScenario.getRequests().add(
            RequestBuilder.create("Get Posts", "HTTP")
                .method("GET")
                .endpoint("${baseUrl}/posts")
                .param("userId", "${userId}")
                .param("_limit", "5")
                .build()
        );
        
        restApiScenario.getRequests().add(
            RequestBuilder.create("Get User", "HTTP")
                .method("GET")
                .endpoint("${baseUrl}/users/${userId}")
                .build()
        );
        
        restApiScenario.getRequests().add(
            RequestBuilder.create("Create Post", "HTTP")
                .method("POST")
                .endpoint("${baseUrl}/posts")
                .header("Content-Type", "application/json")
                .jsonContent()
                .body("{\n" +
                    "    \"userId\": ${userId},\n" +
                    "    \"title\": \"Performance Test\",\n" +
                    "    \"body\": \"This is a test post for performance testing\"\n" +
                    "}")
                .build()
        );
        
        // Add the scenario to the runner
        runner.addScenario(restApiScenario);
        
        // Run all scenarios
        System.out.println("\nExecuting JMeter DSL test scenarios...\n");
        List<Map<String, Object>> results = runner.runAllScenarios();
        
        // Report results
        if (!results.isEmpty()) {
            Map<String, Object> metrics = results.get(0);
            System.out.println("\nTest Results Summary:");
            System.out.println("- Total Requests: " + metrics.get("totalRequests"));
            System.out.println("- Success Rate: " + metrics.get("successRate") + "%");
            System.out.println("- Avg Response Time: " + metrics.get("avgResponseTime") + "ms");
            System.out.println("- 90th Percentile: " + metrics.get("90thPercentile") + "ms");
        }
        
        // Shut down
        runner.shutdown();
    }
    
    /**
     * Run direct HTTP tests using JMDSLEngine
     */
    private static void runDirectHttpTests() throws Exception {
        System.out.println("\n--- Running Direct HTTP Tests ---\n");
        
        // Create execution config 
        ExecutionConfig config = new ExecutionConfig();
        config.setThreads(2);
        config.setIterations(1);
        config.setRampUpSeconds(0);
        config.setHoldSeconds(0);
        
        // Ensure reports directory exists
        String reportDir = "target/reports/direct-http-test";
        new File(reportDir).mkdirs();
        config.setReportDirectory(reportDir);
        
        // Set up variables
        Map<String, String> variables = new HashMap<>();
        variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
        variables.put("userId", "1");
        config.setVariables(variables);
        
        // Create and initialize engine
        JMDSLEngine engine = new JMDSLEngine(config);
        engine.initialize(config.getVariables());
        
        // Set up request parameters
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        
        Map<String, String> params = new HashMap<>();
        params.put("_limit", "3");
        
        // Execute GET test
        System.out.println("Executing GET request test...");
        List<TestResult> results = engine.executeJMeterDslTest(
            "Direct GET Test",
            "http",
            "${baseUrl}/posts",
            "GET",
            null,
            headers,
            params
        );
        
        // Execute POST test
        System.out.println("Executing POST request test...");
        String jsonBody = "{\"title\":\"Direct HTTP Test\",\"body\":\"This is a test\",\"userId\":1}";
        results.addAll(engine.executeJMeterDslTest(
            "Direct POST Test",
            "http", 
            "${baseUrl}/posts",
            "POST",
            jsonBody,
            headers,
            null
        ));
        
        // Generate timestamp for report
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String reportPath = reportDir + "/http_test_report_" + timestamp + ".html";
        
        // Report results
        Map<String, Object> metrics = engine.getMetrics();
        System.out.println("\nDirect Test Results Summary:");
        System.out.println("- Total requests: " + metrics.get("totalRequests"));
        System.out.println("- Success rate: " + metrics.get("successRate") + "%");
        System.out.println("- Average response time: " + metrics.get("avgResponseTime") + " ms");
        System.out.println("- Report file: " + reportPath);
        
        // Shutdown engine
        engine.shutdown();
    }
}