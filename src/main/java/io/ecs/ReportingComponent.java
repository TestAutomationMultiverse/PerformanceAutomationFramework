package io.ecs;

import io.ecs.model.TestResult;
import io.ecs.model.Scenario;
import io.ecs.report.JtlReporter;
import io.ecs.report.ReportGenerator;
import io.ecs.util.JmeterJtlAdapter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ECS Component for handling reporting functionality
 * 
 * This component follows the Entity-Component-System pattern by providing
 * reporting operations that can be applied to test result entities.
 */
public class ReportingComponent {
    private static final Logger LOGGER = Logger.getLogger(ReportingComponent.class.getName());
    
    private final String reportDirectory;
    private final ReportGenerator reportGenerator;
    
    /**
     * Create a new ReportingComponent with a default report directory
     */
    public ReportingComponent() {
        this("target/reports");
    }
    
    /**
     * Create a new ReportingComponent with a specific report directory
     * 
     * @param reportDirectory Directory to store reports
     */
    public ReportingComponent(String reportDirectory) {
        this.reportDirectory = reportDirectory;
        this.reportGenerator = new ReportGenerator();
    }
    
    /**
     * Initialize JTL reporting for a scenario
     * 
     * @param scenarioName Name of the scenario
     * @return Unique test ID
     */
    public String initializeJtlReporting(String scenarioName) {
        JmeterJtlAdapter.ensureDirectoryExists(reportDirectory);
        return JmeterJtlAdapter.initializeJtlFile(scenarioName);
    }
    
    /**
     * Record test results to JTL file
     * 
     * @param scenarioName Name of the scenario
     * @param results List of test results
     */
    public void recordResults(String scenarioName, List<TestResult> results) {
        if (results == null || results.isEmpty()) {
            LOGGER.warning("No results to record for scenario: " + scenarioName);
            return;
        }
        
        JmeterJtlAdapter.recordSamples(scenarioName, results);
    }
    
    /**
     * Finalize JTL reporting for a scenario
     * 
     * @param scenarioName Name of the scenario
     */
    public void finalizeJtlReporting(String scenarioName) {
        JmeterJtlAdapter.finalizeJtlFile(scenarioName);
    }
    
    /**
     * Generate HTML report from test metrics
     * 
     * @param scenario The test scenario
     * @param metrics The test metrics
     * @return Path to the generated report
     */
    public String generateHtmlReport(Scenario scenario, Map<String, Object> metrics) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String sanitizedName = scenario.getName().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_-]", "");
        String reportPath = reportDirectory + "/performance_report_" + timestamp + "_" + sanitizedName + ".html";
        
        try {
            List<Map<String, Object>> metricsForReport = new ArrayList<>();
            metricsForReport.add(metrics);
            reportGenerator.createTestReport(reportPath, scenario, metricsForReport);
            
            LOGGER.info("Test report generated at: " + reportPath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating HTML report: " + e.getMessage(), e);
        }
        
        return reportPath;
    }
    
    /**
     * Generate HTML report from multiple test metrics
     * 
     * @param scenario The test scenario
     * @param metricsList List of metrics maps from multiple test runs
     * @return Path to the generated report
     */
    public String generateHtmlReportFromMultipleRuns(Scenario scenario, List<Map<String, Object>> metricsList) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String sanitizedName = scenario.getName().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_-]", "");
        String reportPath = reportDirectory + "/combined_report_" + timestamp + "_" + sanitizedName + ".html";
        
        try {
            reportGenerator.createTestReport(reportPath, scenario, metricsList);
            LOGGER.info("Combined test report generated at: " + reportPath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error generating combined HTML report: " + e.getMessage(), e);
        }
        
        return reportPath;
    }
    
    /**
     * Generate HTML report from test results directly
     * 
     * @param scenario The test scenario
     * @param results List of test results
     * @return Path to the generated report
     */
    public String generateHtmlReportFromResults(Scenario scenario, List<TestResult> results) {
        Map<String, Object> metrics = aggregateResultsToMetrics(results);
        return generateHtmlReport(scenario, metrics);
    }
    
    /**
     * Aggregate test results into a metrics map
     * 
     * @param results List of test results
     * @return Aggregated metrics map
     */
    public Map<String, Object> aggregateResultsToMetrics(List<TestResult> results) {
        if (results == null || results.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, Object> metrics = new HashMap<>();
        int totalRequests = results.size();
        long totalResponseTime = 0;
        long minResponseTime = Long.MAX_VALUE;
        long maxResponseTime = 0;
        int successCount = 0;
        List<Long> responseTimes = new ArrayList<>();
        
        // First pass to collect data
        for (TestResult result : results) {
            long responseTime = result.getResponseTime();
            responseTimes.add(responseTime);
            
            totalResponseTime += responseTime;
            minResponseTime = Math.min(minResponseTime, responseTime);
            maxResponseTime = Math.max(maxResponseTime, responseTime);
            
            if (result.isSuccess()) {
                successCount++;
            }
        }
        
        // Sort response times for percentiles
        responseTimes.sort(Long::compareTo);
        
        // Calculate metrics
        double avgResponseTime = totalRequests > 0 ? (double) totalResponseTime / totalRequests : 0;
        double successRate = totalRequests > 0 ? ((double) successCount / totalRequests) * 100.0 : 0;
        
        // Calculate percentiles
        int size = responseTimes.size();
        long p90 = size > 0 ? responseTimes.get((int) Math.ceil(size * 0.9) - 1) : 0;
        long p95 = size > 0 ? responseTimes.get((int) Math.ceil(size * 0.95) - 1) : 0;
        long p99 = size > 0 ? responseTimes.get((int) Math.ceil(size * 0.99) - 1) : 0;
        
        // Calculate throughput (requests per second)
        double testDuration = 1.0; // Default to 1 second if we can't calculate
        if (results.size() >= 2) {
            TestResult first = results.get(0);
            TestResult last = results.get(results.size() - 1);
            long durationMs = last.getTimestamp() - first.getTimestamp();
            testDuration = durationMs > 0 ? durationMs / 1000.0 : 1.0;
        }
        double throughput = testDuration > 0 ? totalRequests / testDuration : totalRequests;
        
        // Populate metrics map
        metrics.put("totalRequests", totalRequests);
        metrics.put("successRate", successRate);
        metrics.put("avgResponseTime", avgResponseTime);
        metrics.put("minResponseTime", minResponseTime);
        metrics.put("maxResponseTime", maxResponseTime);
        metrics.put("90thPercentile", p90);
        metrics.put("95thPercentile", p95);
        metrics.put("99thPercentile", p99);
        metrics.put("throughput", throughput);
        
        return metrics;
    }
    
    /**
     * Create a metrics map from a single test result
     * 
     * @param result The test result
     * @return Map of metrics
     */
    public Map<String, Object> createMetricsFromResult(TestResult result) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("totalRequests", 1);
        metrics.put("successRate", result.isSuccess() ? 100.0 : 0.0);
        metrics.put("avgResponseTime", (double) result.getResponseTime());
        metrics.put("minResponseTime", result.getResponseTime());
        metrics.put("maxResponseTime", result.getResponseTime());
        metrics.put("90thPercentile", result.getResponseTime());
        metrics.put("95thPercentile", result.getResponseTime());
        metrics.put("99thPercentile", result.getResponseTime());
        metrics.put("throughput", 1.0); // Just a placeholder as we have a single request
        
        return metrics;
    }
    
    /**
     * Get the report directory
     * 
     * @return Report directory path
     */
    public String getReportDirectory() {
        return reportDirectory;
    }
}