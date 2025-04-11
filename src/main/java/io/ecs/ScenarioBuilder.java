package io.ecs;

import io.ecs.model.Scenario;
import io.ecs.model.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper component for building scenarios in the ECS framework
 * Provides a fluent interface for creating complex test scenarios
 */
public class ScenarioBuilder {
    
    private final Scenario scenario;
    
    /**
     * Create a new scenario builder
     * 
     * @param name Name of the scenario
     * @param engineType Engine type to use (jmdsl, jmeter, etc.)
     */
    public ScenarioBuilder(String name, String engineType) {
        scenario = new Scenario();
        scenario.setName(name);
        scenario.setEngine(engineType);
        
        // Set default values
        scenario.setThreads(1);
        scenario.setIterations(1);
        scenario.setRampUp(0);
        scenario.setHold(0);
        
        // Initialize collections
        scenario.setRequests(new java.util.ArrayList<>());
        scenario.setVariables(new HashMap<>());
    }
    
    /**
     * Static factory method to create a new scenario builder
     * 
     * @param name Name of the scenario
     * @param engineType Engine type to use
     * @return A new scenario builder
     */
    public static ScenarioBuilder create(String name, String engineType) {
        return new ScenarioBuilder(name, engineType);
    }
    
    /**
     * Set the number of threads to use
     * 
     * @param threads Number of concurrent threads
     * @return This builder instance
     */
    public ScenarioBuilder threads(int threads) {
        scenario.setThreads(threads);
        return this;
    }
    
    /**
     * Set the number of iterations per thread
     * 
     * @param iterations Number of iterations
     * @return This builder instance
     */
    public ScenarioBuilder iterations(int iterations) {
        scenario.setIterations(iterations);
        return this;
    }
    
    /**
     * Set the ramp-up period in seconds
     * 
     * @param rampUpSeconds Ramp-up time in seconds
     * @return This builder instance
     */
    public ScenarioBuilder rampUp(int rampUpSeconds) {
        scenario.setRampUp(rampUpSeconds);
        return this;
    }
    
    /**
     * Set the hold time in seconds
     * 
     * @param holdSeconds Hold time in seconds
     * @return This builder instance
     */
    public ScenarioBuilder hold(int holdSeconds) {
        scenario.setHold(holdSeconds);
        return this;
    }
    
    /**
     * Add a variable to the scenario
     * 
     * @param name Variable name
     * @param value Variable value
     * @return This builder instance
     */
    public ScenarioBuilder variable(String name, String value) {
        scenario.getVariables().put(name, value);
        return this;
    }
    
    /**
     * Add multiple variables at once
     * 
     * @param variables Map of variable name to value
     * @return This builder instance
     */
    public ScenarioBuilder variables(Map<String, String> variables) {
        if (variables != null) {
            scenario.getVariables().putAll(variables);
        }
        return this;
    }
    
    /**
     * Add a request to the scenario
     * 
     * @param request Request to add
     * @return This builder instance
     */
    public ScenarioBuilder addRequest(Request request) {
        scenario.getRequests().add(request);
        return this;
    }
    
    /**
     * Create a REST API load test scenario with common settings
     * 
     * @return This builder instance
     */
    public ScenarioBuilder asRestApiLoadTest() {
        scenario.setThreads(10);
        scenario.setIterations(10);
        scenario.setRampUp(5);
        scenario.setHold(30);
        return this;
    }
    
    /**
     * Create a spike test scenario with common settings
     * 
     * @return This builder instance
     */
    public ScenarioBuilder asSpikeTest() {
        scenario.setThreads(20);
        scenario.setIterations(5);
        scenario.setRampUp(1);
        scenario.setHold(5);
        return this;
    }
    
    /**
     * Create an endurance test scenario with common settings
     * 
     * @return This builder instance
     */
    public ScenarioBuilder asEnduranceTest() {
        scenario.setThreads(5);
        scenario.setIterations(30);
        scenario.setRampUp(10);
        scenario.setHold(300);
        return this;
    }
    
    /**
     * Build the final scenario
     * 
     * @return The configured scenario
     */
    public Scenario build() {
        return scenario;
    }
}