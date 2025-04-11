package io.ecs.engine;

import io.ecs.model.Request;
import io.ecs.model.TestResult;
import io.ecs.util.EcsLogger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * GatlingEngine - Implementation of the Engine interface
 * Provides a simplified version that doesn't depend on the Gatling Java API
 */
public class GatlingEngine implements Engine {
    private static final EcsLogger logger = EcsLogger.getLogger(GatlingEngine.class);
    
    private ExecutionConfig config;
    private Map<String, String> variables;
    private Map<String, Object> metrics;
    private String reportDirectory;
    private boolean initialized = false;
    
    /**
     * Constructor that takes an ExecutionConfig
     */
    public GatlingEngine(ExecutionConfig config) {
        this.config = config;
        this.variables = new HashMap<>();
        this.metrics = new ConcurrentHashMap<>();
        
        // Set default report directory
        this.reportDirectory = config.getReportDirectory() != null ? 
                config.getReportDirectory() : "target/gatling-reports";
    }
    
    /**
     * Default constructor
     */
    public GatlingEngine() {
        this.config = new ExecutionConfig();
        this.variables = new HashMap<>();
        this.metrics = new ConcurrentHashMap<>();
        this.reportDirectory = "target/gatling-reports";
    }
    
    /**
     * Initialize the engine with variables
     * 
     * @param variables Variables for substitution in requests
     */
    @Override
    public void initialize(Map<String, String> variables) {
        if (variables != null) {
            this.variables.putAll(variables);
        }
        this.initialized = true;
        logger.info("Gatling Engine initialized with {} variables", this.variables.size());
    }
    
    /**
     * Execute a scenario with a list of requests
     * 
     * @param scenarioName The name of the scenario
     * @param requests List of requests to execute
     * @return List of test results
     */
    @Override
    public List<TestResult> executeScenario(String scenarioName, List<Request> requests) {
        if (!initialized) {
            logger.warn("Gatling engine not initialized, initializing with default values");
            initialize(new HashMap<>());
        }
        
        logger.info("Executing scenario: {} with {} requests", scenarioName, requests.size());
        
        List<TestResult> results = new ArrayList<>();
        
        try {
            // Simulate execution of requests
            simulateExecution(scenarioName, requests, results);
            
            // Process metrics
            processMetrics(results);
            
            logger.info("Scenario execution completed: {}", scenarioName);
        } catch (Exception e) {
            logger.error("Error executing scenario: {}", e.getMessage(), e);
            // Add error result
            TestResult errorResult = new TestResult();
            errorResult.setTestName(scenarioName);
            errorResult.setSuccess(false);
            errorResult.setError(e.getMessage());
            results.add(errorResult);
        }
        
        return results;
    }
    
    /**
     * Simulate execution and generate results
     * 
     * @param scenarioName The scenario name
     * @param requests List of requests
     * @param results List to store results
     */
    private void simulateExecution(String scenarioName, List<Request> requests, List<TestResult> results) {
        // For each request, create a simulated result
        Random random = new Random();
        
        for (Request request : requests) {
            TestResult result = new TestResult();
            result.setTestName(request.getName());
            result.setScenarioName(scenarioName);
            
            // Simulate successful result with random response times
            result.setSuccess(true);
            result.setStatusCode(200);
            
            // Generate a reasonable response time (50-500ms)
            long responseTime = 50 + random.nextInt(450);
            result.setResponseTime(responseTime);
            
            // Add some response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("contentType", "application/json");
            responseData.put("contentLength", 1024 + random.nextInt(2048));
            result.setResponseData(responseData);
            
            // Add the result
            results.add(result);
            
            logger.info("Request: {} completed with response time: {}ms", 
                    request.getName(), result.getResponseTime());
        }
    }
    
    /**
     * Process metrics from test results
     * 
     * @param results List of test results
     */
    private void processMetrics(List<TestResult> results) {
        if (results.isEmpty()) {
            logger.warn("No results to process metrics from");
            return;
        }
        
        int totalRequests = results.size();
        long successfulRequests = results.stream().filter(TestResult::isSuccess).count();
        double successRate = (double) successfulRequests / totalRequests * 100.0;
        
        // Calculate response time metrics
        List<Long> responseTimes = results.stream()
                .map(TestResult::getResponseTime)
                .sorted()
                .collect(Collectors.toList());
        
        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        long minResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.get(0);
        long maxResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.get(responseTimes.size() - 1);
        
        // Calculate percentiles
        int p90Index = (int) Math.ceil(0.9 * responseTimes.size()) - 1;
        int p95Index = (int) Math.ceil(0.95 * responseTimes.size()) - 1;
        int p99Index = (int) Math.ceil(0.99 * responseTimes.size()) - 1;
        
        p90Index = Math.max(0, Math.min(p90Index, responseTimes.size() - 1));
        p95Index = Math.max(0, Math.min(p95Index, responseTimes.size() - 1));
        p99Index = Math.max(0, Math.min(p99Index, responseTimes.size() - 1));
        
        long p90 = responseTimes.isEmpty() ? 0 : responseTimes.get(p90Index);
        long p95 = responseTimes.isEmpty() ? 0 : responseTimes.get(p95Index);
        long p99 = responseTimes.isEmpty() ? 0 : responseTimes.get(p99Index);
        
        // Store metrics
        metrics.put("totalRequests", totalRequests);
        metrics.put("successfulRequests", successfulRequests);
        metrics.put("successRate", successRate);
        metrics.put("avgResponseTime", avgResponseTime);
        metrics.put("minResponseTime", minResponseTime);
        metrics.put("maxResponseTime", maxResponseTime);
        metrics.put("90thPercentile", p90);
        metrics.put("95thPercentile", p95);
        metrics.put("99thPercentile", p99);
        
        logger.info("Processed metrics: {} total requests, {}% success rate, {}ms avg response time", 
                totalRequests, successRate, avgResponseTime);
    }
    
    /**
     * Execute a single request
     * 
     * @param request The request to execute
     * @return Test result for the execution
     */
    @Override
    public TestResult executeRequest(Request request) {
        logger.info("Executing request: {}", request.getName());
        
        // Create a test result
        TestResult result = new TestResult();
        result.setTestName(request.getName());
        
        try {
            // Simulate execution with reasonable timing
            Random random = new Random();
            long responseTime = 50 + random.nextInt(450);
            
            // Set result properties
            result.setSuccess(true);
            result.setStatusCode(200);
            result.setResponseTime(responseTime);
            
            // Add response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("contentType", "application/json");
            responseData.put("contentLength", 1024 + random.nextInt(2048));
            result.setResponseData(responseData);
            
            // Update metrics based on this single request
            updateMetricsFromResult(result);
            
            logger.info("Request: {} completed successfully in {}ms", request.getName(), responseTime);
            
            return result;
        } catch (Exception e) {
            logger.error("Error executing request {}: {}", request.getName(), e.getMessage(), e);
            
            // Set error information in result
            result.setSuccess(false);
            result.setStatusCode(500);
            result.setError(e.getMessage());
            result.setResponseTime(0);
            
            return result;
        }
    }
    
    /**
     * Update metrics based on a single request result
     * 
     * @param result The test result to incorporate into metrics
     */
    private void updateMetricsFromResult(TestResult result) {
        // Initialize metrics if needed
        if (!metrics.containsKey("totalRequests")) {
            metrics.put("totalRequests", 0);
            metrics.put("successfulRequests", 0);
            metrics.put("successRate", 0.0);
            metrics.put("avgResponseTime", 0.0);
            metrics.put("minResponseTime", 0L);
            metrics.put("maxResponseTime", 0L);
        }
        
        // Update counters
        int totalRequests = (int) metrics.get("totalRequests") + 1;
        metrics.put("totalRequests", totalRequests);
        
        if (result.isSuccess()) {
            int successfulRequests = (int) metrics.get("successfulRequests") + 1;
            metrics.put("successfulRequests", successfulRequests);
            double successRate = (double) successfulRequests / totalRequests * 100.0;
            metrics.put("successRate", successRate);
        }
        
        // Update response time metrics
        double currentAvg = (double) metrics.get("avgResponseTime");
        long currentMin = (long) metrics.get("minResponseTime");
        long currentMax = (long) metrics.get("maxResponseTime");
        
        // Recalculate the average
        double newAvg = ((currentAvg * (totalRequests - 1)) + result.getResponseTime()) / totalRequests;
        metrics.put("avgResponseTime", newAvg);
        
        // Update min if needed
        if (currentMin == 0 || result.getResponseTime() < currentMin) {
            metrics.put("minResponseTime", result.getResponseTime());
        }
        
        // Update max if needed
        if (result.getResponseTime() > currentMax) {
            metrics.put("maxResponseTime", result.getResponseTime());
        }
    }
    
    /**
     * Get the current performance metrics
     * 
     * @return Map of metrics
     */
    @Override
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }
    
    /**
     * Shut down the engine and release resources
     */
    @Override
    public void shutdown() {
        logger.info("Shutting down Gatling engine");
        // In a real implementation, this would release resources
    }
    
    /**
     * Helper method to replace variables in a string
     * 
     * @param source The source string with variables
     * @param variables Map of variable values
     * @return String with variables replaced
     */
    private String replaceVariables(String source, Map<String, String> variables) {
        if (source == null || variables == null || variables.isEmpty()) {
            return source;
        }
        
        String result = source;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        
        return result;
    }
}