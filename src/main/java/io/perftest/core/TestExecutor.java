package io.perftest.core;

import io.perftest.config.TestConfiguration;
import io.perftest.engine.Engine;
import io.perftest.engine.JMAPIEngine;
import io.perftest.engine.JMDSLEngine;
import io.perftest.model.ExecutionConfig;
import io.perftest.model.Scenario;
import io.perftest.protocol.Protocol;
import io.perftest.report.MetricsCollector;
import io.perftest.util.CsvDataSource;

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
        
        // Create appropriate engine
        Engine engine;
        if ("JMDSL".equalsIgnoreCase(config.getEngine())) {
            engine = new JMDSLEngine(config.getExecutionConfig());
        } else if ("JMAPI".equalsIgnoreCase(config.getEngine())) {
            engine = new JMAPIEngine(config.getExecutionConfig());
        } else {
            throw new IllegalArgumentException("Unsupported engine: " + config.getEngine());
        }
        
        // Initialize protocol
        Protocol protocol = config.getProtocol();
        
        // Load data source if specified
        CsvDataSource dataSource = null;
        if (config.getDataSource() != null && !config.getDataSource().isEmpty()) {
            dataSource = new CsvDataSource(config.getDataSource());
        }
        
        // Execute each scenario
        for (Scenario scenario : config.getScenarios()) {
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
