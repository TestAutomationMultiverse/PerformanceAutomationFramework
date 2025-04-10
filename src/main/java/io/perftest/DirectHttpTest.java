package io.perftest;

import io.perftest.model.TestResult;
import io.perftest.model.ExecutionConfig;
import io.perftest.engine.JMDSLEngine;
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
 * A direct HTTP test runner using the JMDSLEngine
 * This class is independent of the YAML configuration system
 */
public class DirectHttpTest {
    
    private static final Logger logger = LoggerFactory.getLogger(DirectHttpTest.class);
    
    public static void main(String[] args) {
        System.out.println("\n=== Starting Direct HTTP Performance Test ===\n");
        System.out.println("DirectHttpTest: This test runs without configuration files");
        
        // We don't need any command line arguments for this class
        // This class runs standalone with hardcoded settings
        if (args.length > 0) {
            System.out.println("Note: DirectHttpTest ignores all command line arguments");
        }
        
        try {
            // Print diagnostic information
            System.out.println("DirectHttpTest: Starting direct HTTP test execution...");
            System.out.println("DirectHttpTest: JDK version: " + System.getProperty("java.version"));
            System.out.println("DirectHttpTest: JDK vendor: " + System.getProperty("java.vendor"));
            System.out.println("DirectHttpTest: Class path: " + System.getProperty("java.class.path"));
            
            // Create execution config 
            ExecutionConfig config = new ExecutionConfig();
            config.setThreads(2);
            config.setIterations(5);
            config.setRampUpSeconds(1);
            config.setHoldSeconds(1);
            
            // Ensure reports directory exists
            String reportDir = "target/reports/direct-http-test";
            new File(reportDir).mkdirs();
            config.setReportDirectory(reportDir);
            
            // Set up variables
            Map<String, String> variables = new HashMap<>();
            variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
            variables.put("userId", "1");
            variables.put("test", "value");
            config.setVariables(variables);
            
            // Create and initialize engine
            JMDSLEngine engine = new JMDSLEngine(config);
            engine.initialize(config.getVariables());
            
            // Set up request parameters
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            
            Map<String, String> params = new HashMap<>();
            params.put("_limit", "5");
            params.put("userId", "${userId}");
            
            // Execute GET test
            System.out.println("Executing GET request test...");
            List<TestResult> results = engine.executeJMeterDslTest(
                "Posts API Test",
                "http",
                "${baseUrl}/posts",
                "GET",
                null,
                headers,
                params
            );
            
            // Execute GET single item test
            results.addAll(engine.executeJMeterDslTest(
                "Single User Test",
                "http",
                "${baseUrl}/users/${userId}",
                "GET",
                null,
                headers,
                null
            ));
            
            // Execute POST test
            System.out.println("Executing POST request test...");
            String jsonBody = "{\n" +
                "    \"title\": \"Direct HTTP Test\",\n" +
                "    \"body\": \"This is a test created by the performance test framework\",\n" +
                "    \"userId\": 1\n" +
                "}";
                
            results.addAll(engine.executeJMeterDslTest(
                "Create Post Test",
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
            System.out.println("\nTest Results Summary:");
            System.out.println("- Total requests: " + metrics.get("totalRequests"));
            System.out.println("- Success rate: " + metrics.get("successRate") + "%");
            System.out.println("- Average response time: " + metrics.get("avgResponseTime") + " ms");
            System.out.println("- Min/Max response time: " + metrics.get("minResponseTime") + "/" 
                              + metrics.get("maxResponseTime") + " ms");
            System.out.println("- 90th percentile: " + metrics.get("90thPercentile") + " ms");
            System.out.println("- Report file: " + reportPath);
            
            // Shutdown engine
            engine.shutdown();
            
            System.out.println("\n=== Direct HTTP Performance Test Completed Successfully ===\n");
            
        } catch (Exception e) {
            System.err.println("ERROR in Direct HTTP Test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}