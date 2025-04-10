package io.perftest;

import io.perftest.engine.JMDSLEngine;
import io.perftest.model.ExecutionConfig;
import io.perftest.model.Request;
import io.perftest.model.TestResult;
import io.perftest.util.DynamicVariableResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

/**
 * Standalone runner for JMeter DSL tests with dynamic variable resolution
 * This class is separate from the main App to avoid command line conflicts
 */
public class JMeterDSLRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(JMeterDSLRunner.class);
    
    public static void main(String[] args) {
        try {
            System.out.println("\n=== JMeter DSL Runner with Dynamic Variables ===\n");
            
            // Ensure report directory exists
            File reportsDir = new File("target/reports");
            reportsDir.mkdirs();
            System.out.println("Reports directory: " + reportsDir.getAbsolutePath());
            
            // Create execution configuration
            ExecutionConfig config = new ExecutionConfig();
            config.setThreads(2);
            config.setIterations(3);
            config.setRampUpSeconds(1);
            config.setReportDirectory("target/reports");
            
            // Create test engine
            JMDSLEngine engine = new JMDSLEngine(config);
            
            // Setup global variables
            Map<String, String> globalVars = new HashMap<>();
            globalVars.put("baseUrl", "https://jsonplaceholder.typicode.com");
            globalVars.put("userId", "1");
            globalVars.put("title", "Performance Test Title");
            globalVars.put("body", "This is a dynamic test body created at ${timestamp}");
            engine.initialize(globalVars);
            
            // Run various test scenarios
            runJsonPost(engine);
            runDynamicGet(engine);
            
            System.out.println("\n=== JMeter DSL Runner Completed Successfully ===\n");
            
        } catch (Exception e) {
            System.err.println("ERROR in JMeter DSL Runner: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run a JSON POST request with dynamic variables
     */
    private static void runJsonPost(JMDSLEngine engine) throws IOException {
        System.out.println("\n--- Running JSON POST Test with Dynamic Variables ---\n");
        
        // Create dynamic test request for JSON API
        Request request = new Request();
        request.setName("Dynamic POST Test");
        request.setProtocol("HTTP");
        request.setMethod("POST");
        request.setEndpoint("${baseUrl}/posts");
        
        // Define a JSON body with variable substitution
        String jsonBody = 
            "{\n" +
            "    \"userId\": ${userId},\n" +
            "    \"title\": \"${title}\",\n" +
            "    \"body\": \"${body}\",\n" +
            "    \"timestamp\": ${timestamp},\n" +
            "    \"randomId\": \"${randomInt(1000,9999)}\",\n" +
            "    \"uniqueId\": \"${uuid}\",\n" +
            "    \"randomString\": \"${randomString(8)}\"\n" +
            "}";
        request.setBody(jsonBody);
        
        // Add headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Request-ID", "${uuid}");
        headers.put("X-Test-Timestamp", "${timestamp}");
        request.setHeaders(headers);
        
        // Execute the test
        System.out.println("Executing POST test with dynamic variables...");
        TestResult result = engine.executeRequest(request);
        
        // Save the processed request body to file for verification
        String processedBody = result.getProcessedBody();
        if (processedBody != null) {
            Files.write(Paths.get("target/reports/processed_post_body.json"), 
                     processedBody.getBytes(StandardCharsets.UTF_8));
            System.out.println("Saved processed request body to target/reports/processed_post_body.json");
        }
        
        // Print the results
        printTestResults(result, engine);
    }
    
    /**
     * Run a dynamic GET request with path parameters
     */
    private static void runDynamicGet(JMDSLEngine engine) {
        System.out.println("\n--- Running Dynamic GET Test with Path Parameters ---\n");
        
        // Create dynamic test request with path parameters
        Request request = new Request();
        request.setName("Dynamic GET Test");
        request.setProtocol("HTTP");
        request.setMethod("GET");
        request.setEndpoint("${baseUrl}/users/${userId}");
        
        // Add query parameters
        Map<String, String> params = new HashMap<>();
        params.put("_timestamp", "${timestamp}");
        params.put("_random", "${randomInt(1,100)}");
        request.setParams(params);
        
        // Add headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("X-Request-ID", "${uuid}");
        request.setHeaders(headers);
        
        // Execute the test
        System.out.println("Executing GET test with dynamic variables...");
        TestResult result = engine.executeRequest(request);
        
        // Print the results
        printTestResults(result, engine);
    }
    
    /**
     * Helper method to print test results and metrics
     */
    private static void printTestResults(TestResult result, JMDSLEngine engine) {
        System.out.println("\n=== Test Results ===");
        System.out.println("Status: " + (result.isSuccess() ? "SUCCESS" : "FAILURE"));
        System.out.println("Status code: " + result.getStatusCode());
        System.out.println("Response time: " + result.getResponseTime() + " ms");
        
        // Truncate response body if too long
        String responseBody = result.getResponseBody();
        if (responseBody != null && responseBody.length() > 300) {
            responseBody = responseBody.substring(0, 297) + "...";
        }
        System.out.println("Response body: " + responseBody);
        
        // Print headers if available
        Map<String, String> responseHeaders = result.getHeaders();
        if (responseHeaders != null && !responseHeaders.isEmpty()) {
            System.out.println("\nResponse Headers:");
            for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
                System.out.println("  " + header.getKey() + ": " + header.getValue());
            }
        }
        
        // Print metrics summary
        Map<String, Object> metrics = engine.getMetrics();
        System.out.println("\n=== Test Metrics ===");
        System.out.println("Total Requests: " + metrics.get("totalRequests"));
        System.out.println("Success Rate: " + metrics.get("successRate") + "%");
        System.out.println("Average Response Time: " + metrics.get("avgResponseTime") + " ms");
        System.out.println("Min/Max Response Time: " + metrics.get("minResponseTime") + "/" 
                          + metrics.get("maxResponseTime") + " ms");
        System.out.println("90th Percentile: " + metrics.get("90thPercentile") + " ms");
    }
}