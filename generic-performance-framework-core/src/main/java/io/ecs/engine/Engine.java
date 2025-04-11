package io.ecs.engine;

import io.ecs.model.Request;
import io.ecs.model.Response;
import io.ecs.model.TestResult;

import java.util.List;
import java.util.Map;

/**
 * Interface for all test engine implementations
 */
public interface Engine {
    
    /**
     * Initialize the engine with global variables
     * 
     * @param variables Global variables for the test
     */
    void initialize(Map<String, String> variables);
    
    /**
     * Execute a scenario with a list of requests
     * 
     * @param scenarioName Name of the scenario
     * @param requests List of requests to execute
     * @return List of test results
     */
    List<TestResult> executeScenario(String scenarioName, List<Request> requests);
    
    /**
     * Execute a single request
     * 
     * @param request Request to execute
     * @return Test result
     */
    TestResult executeRequest(Request request);
    
    /**
     * Get the current performance metrics
     * 
     * @return Map of metrics
     */
    Map<String, Object> getMetrics();
    
    /**
     * Shut down the engine and release resources
     */
    void shutdown();
}