package io.ecs.core;

import io.ecs.config.TestConfiguration;
import io.ecs.engine.Engine;
import io.ecs.engine.EngineFactory;
import io.ecs.engine.JMDSLEngine;
import io.ecs.model.ExecutionConfig;
import io.ecs.model.Scenario;
import io.ecs.engine.Protocol;
import io.ecs.report.MetricsCollector;
import io.ecs.util.CsvDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Coordinates the execution of the test based on configuration
 */
public class TestExecutor {
    
    /**
     * Execute the performance test based on the provided configuration
     * 
     * @param config the test configuration
     * @return list of test results
     * @throws Exception if test execution fails
     */
    public List<TestResult> execute(TestConfiguration config) throws Exception {
        List<TestResult> allResults = new ArrayList<>();
        
        // Create appropriate engine using EngineFactory
        Engine engine = EngineFactory.getEngine(config.getEngine(), config.getExecutionConfig());
        
        // Initialize protocol
        Protocol protocol = config.getProtocol();
        
        // Load data source if specified
        CsvDataSource dataSource = null;
        if (config.getDataSource() != null && !config.getDataSource().isEmpty()) {
            dataSource = new CsvDataSource(config.getDataSource());
        }
        
        // Execute each scenario (using model.Scenario for compatibility)
        for (io.ecs.model.Scenario scenario : config.getModelScenarios()) {
            System.out.println("Executing scenario: " + scenario.getName());
            
            TestRunner runner = new TestRunner(
                    engine,
                    protocol,
                    scenario,
                    config.getExecutionConfig(),
                    dataSource
            );
            
            List<TestResult> scenarioResults = runner.run();
            allResults.addAll(scenarioResults);
            
            // Collect and show metrics for this scenario
            MetricsCollector metrics = runner.getMetricsCollector();
            System.out.println("Scenario: " + scenario.getName() + " completed");
            System.out.println("Total requests: " + metrics.getTotalRequests());
            System.out.println("Success rate: " + metrics.getSuccessRate() + "%");
            System.out.println("Average response time: " + metrics.getAverageResponseTime() + "ms");
            System.out.println("Min/Max response time: " + metrics.getMinResponseTime() + "/" + 
                    metrics.getMaxResponseTime() + "ms");
            System.out.println("90th percentile: " + metrics.getPercentile(90) + "ms");
            System.out.println("Errors: " + metrics.getErrorCount());
            System.out.println("--------------------------------------");
        }
        
        return allResults;
    }
}
