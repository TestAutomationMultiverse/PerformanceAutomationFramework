package io.perftest;

import io.perftest.ecs.ECSTestRunner;
import io.perftest.ecs.components.RequestBuilder;
import io.perftest.ecs.components.ScenarioBuilder;
import io.perftest.model.Request;
import io.perftest.model.Scenario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Example of using the ECS test framework directly
 * This class demonstrates how to use the performance test framework
 * without going through the App class or YAML configuration
 */
public class ECSDirectTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ECSDirectTest.class);
    
    public static void main(String[] args) {
        logger.info("Starting ECS Direct Test");
        System.out.println("\n=== Starting ECS Direct Performance Test ===\n");
        
        try {
            // Create a test runner
            ECSTestRunner runner = new ECSTestRunner("target/reports/ecs-test");
            
            // Add global variables
            Map<String, String> globalVars = new HashMap<>();
            globalVars.put("baseUrl", "https://jsonplaceholder.typicode.com");
            globalVars.put("userId", "1");
            globalVars.put("title", "ECS Test Title");
            globalVars.put("body", "This is an ECS test with timestamp ${timestamp}");
            runner.addGlobalVariables(globalVars);
            
            // Create HTTP Test Scenario using the builder
            Scenario httpScenario = ScenarioBuilder.create("HTTP API Test", "jmdsl")
                .threads(2)
                .iterations(3)
                .rampUp(1)
                .variable("endpoint", "/posts")
                .variable("method", "POST")
                .build();
            
            // Add POST request using the request builder
            Request postRequest = RequestBuilder.create("Create Post", "HTTP")
                .method("${method}")
                .endpoint("${baseUrl}${endpoint}")
                .body("{\n" +
                    "    \"userId\": ${userId},\n" +
                    "    \"title\": \"${title}\",\n" +
                    "    \"body\": \"${body}\",\n" +
                    "    \"timestamp\": ${timestamp},\n" +
                    "    \"randomId\": \"${randomInt(1000,9999)}\",\n" +
                    "    \"uniqueId\": \"${uuid}\"\n" +
                    "}")
                .jsonContent()
                .withRequestId()
                .build();
            
            // Add GET request using the request builder
            Request getRequest = RequestBuilder.create("Get Users", "HTTP")
                .method("GET")
                .endpoint("${baseUrl}/users")
                .param("_limit", "5")
                .param("_timestamp", "${timestamp}")
                .build();
            
            // Add requests to scenario
            httpScenario.getRequests().add(postRequest);
            httpScenario.getRequests().add(getRequest);
            
            // Add scenario to runner
            runner.addScenario(httpScenario);
            
            // Create a second scenario for HTTPS testing
            Scenario httpsScenario = ScenarioBuilder.create("HTTPS API Test", "jmdsl")
                .threads(1)
                .iterations(1)
                .build();
            
            // Add a GET request with request builder
            Request getUserDetails = RequestBuilder.create("Get User Details", "HTTPS")
                .method("GET")
                .endpoint("${baseUrl}/users/${userId}")
                .build();
            
            // Add request to scenario
            httpsScenario.getRequests().add(getUserDetails);
            
            // Add scenario to runner
            runner.addScenario(httpsScenario);
            
            // Run all scenarios
            System.out.println("\nExecuting test scenarios...\n");
            runner.runAllScenarios();
            
            // Shut down
            runner.shutdown();
            
            System.out.println("\n=== ECS Direct Performance Test Completed Successfully ===\n");
            logger.info("ECS Direct Test completed successfully");
            
        } catch (Exception e) {
            System.err.println("ERROR in ECS Direct Test: " + e.getMessage());
            e.printStackTrace();
            logger.error("Error in ECS Direct Test: {}", e.getMessage());
        }
    }
}