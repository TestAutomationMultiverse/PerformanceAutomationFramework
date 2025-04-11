package io.ecs.core;

import io.ecs.engine.Engine;
import io.ecs.model.ExecutionConfig;
import io.ecs.model.Request;
import io.ecs.model.Scenario;
import io.ecs.engine.Protocol;
import io.ecs.report.MetricsCollector;
import io.ecs.util.CsvDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Responsible for running a scenario with the specified configuration
 */
public class TestRunner {
    private final Engine engine;
    private final Protocol protocol;
    private final Scenario scenario;
    private final ExecutionConfig executionConfig;
    private final CsvDataSource dataSource;
    private final MetricsCollector metricsCollector;
    
    public TestRunner(Engine engine, Protocol protocol, Scenario scenario, 
                      ExecutionConfig executionConfig, CsvDataSource dataSource) {
        this.engine = engine;
        this.protocol = protocol;
        this.scenario = scenario;
        this.executionConfig = executionConfig;
        this.dataSource = dataSource;
        this.metricsCollector = new MetricsCollector();
    }
    
    /**
     * Run the performance test scenario
     * 
     * @return list of test results
     * @throws Exception if execution fails
     */
    public List<TestResult> run() throws Exception {
        List<TestResult> results = new ArrayList<>();
        
        // Initialize thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(executionConfig.getThreads());
        
        // Calculate total tests to run
        int totalTests = executionConfig.getThreads() * executionConfig.getIterations();
        List<CompletableFuture<List<TestResult>>> futures = new ArrayList<>();
        
        // Start threads
        for (int i = 0; i < executionConfig.getThreads(); i++) {
            final int threadNum = i;
            
            // Create a delay for thread ramp-up
            long delay = 0;
            if (executionConfig.getRampUpSeconds() > 0) {
                delay = (long) ((double) threadNum / executionConfig.getThreads() * executionConfig.getRampUpSeconds() * 1000);
            }
            
            final long threadDelay = delay;
            
            CompletableFuture<List<TestResult>> future = CompletableFuture.supplyAsync(() -> {
                List<TestResult> threadResults = new ArrayList<>();
                try {
                    // Apply ramp-up delay
                    if (threadDelay > 0) {
                        Thread.sleep(threadDelay);
                    }
                    
                    // Run iterations
                    for (int j = 0; j < executionConfig.getIterations(); j++) {
                        // Get data for this iteration
                        Map<String, String> variables = new HashMap<>();
                        
                        // Add thread and iteration information
                        variables.put("threadNum", String.valueOf(threadNum));
                        variables.put("iteration", String.valueOf(j));
                        
                        // Add data from CSV if available
                        if (dataSource != null) {
                            Map<String, String> rowData = dataSource.getRow(j % dataSource.getRowCount());
                            if (rowData != null) {
                                variables.putAll(rowData);
                            }
                        }
                        
                        // Execute each request in the scenario
                        for (Request request : scenario.getRequests()) {
                            PerformanceTest test = new PerformanceTest(protocol, request, variables);
                            TestResult result = test.call();
                            threadResults.add(result);
                        }
                        
                        // Add pacing if needed (time between iterations)
                        if (j < executionConfig.getIterations() - 1) {
                            // Simple fixed pacing example - could be more sophisticated
                            Thread.sleep(100);
                        }
                    }
                    
                    // Hold time after completing iterations
                    if (executionConfig.getHoldSeconds() > 0) {
                        Thread.sleep(executionConfig.getHoldSeconds() * 1000);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error in test thread " + threadNum + ": " + e.getMessage());
                    e.printStackTrace();
                }
                return threadResults;
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        
        // Collect all results
        allFutures.thenApply(v -> 
            futures.stream()
                   .map(CompletableFuture::join)
                   .flatMap(List::stream)
                   .collect(Collectors.toList())
        ).thenAccept(allResults -> {
            results.addAll(allResults);
            
            // Process metrics
            for (TestResult result : allResults) {
                metricsCollector.recordRequest();
                if (result.isSuccess()) {
                    metricsCollector.recordSuccess();
                } else {
                    metricsCollector.recordFailure();
                }
                if (result.getResponseTime() > 0) {
                    metricsCollector.recordResponseTime(result.getResponseTime());
                }
            }
        }).join();
        
        // Shutdown executor service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        return results;
    }
    
    public MetricsCollector getMetricsCollector() {
        return metricsCollector;
    }
}
