package io.ecs.component;

import io.ecs.model.TestResult;
import io.ecs.model.Scenario;
import io.ecs.report.JtlReporter;
import io.ecs.report.ReportGenerator;
import io.ecs.util.EcsLogger;
import io.ecs.util.JmeterJtlAdapter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ECS Component for handling reporting functionality
 * 
 * This component follows the Entity-Component-System pattern by providing
 * reporting operations that can be applied to test result entities.
 */
public class ReportingComponent {
    private static final EcsLogger logger = EcsLogger.getLogger(ReportingComponent.class);
    
    private final String reportDirectory;
    private final ReportGenerator reportGenerator;
    private final List<String> generatedReports;
    private final Map<String, JtlReporter> jtlReporters;
    private final JmeterJtlAdapter jtlAdapter;
    
    /**
     * Create a new ReportingComponent with a default report directory
     */
    public ReportingComponent() {
        this("target/reports");
    }
    
    /**
     * Create a new ReportingComponent with a specific report directory
     * 
     * @param reportDirectory Directory for report output
     */
    public ReportingComponent(String reportDirectory) {
        this.reportDirectory = reportDirectory;
        this.reportGenerator = new ReportGenerator();
        this.generatedReports = new ArrayList<>();
        this.jtlReporters = new HashMap<>();
        this.jtlAdapter = new JmeterJtlAdapter();
    }
    
    /**
     * Initialize reporting for a scenario
     * 
     * @param scenario Scenario to initialize reporting for
     * @return This component for chaining
     */
    public ReportingComponent initializeReporting(Scenario scenario) {
        String reportPath = getReportPath(scenario.getName());
        
        // Create JTL reporter for this scenario
        JtlReporter reporter = new JtlReporter();
        jtlReporters.put(scenario.getId(), reporter);
        
        reporter.initializeJtlFile(reportPath);
        
        // Initialize JTL adapter if it exists
        if (jtlAdapter != null) {
            try {
                jtlAdapter.initializeJtlFile(scenario.getName());
            } catch (Exception e) {
                logger.error("Error initializing JTL adapter for {}", scenario.getName(), e);
            }
        }
        
        return this;
    }
    
    /**
     * Generate a report for a completed test result
     * 
     * @param testResult Test result to generate a report for
     * @return Path to the generated report
     */
    public String generateReport(TestResult testResult) {
        try {
            // Finalize the JTL file
            JtlReporter reporter = jtlReporters.get(testResult.getScenarioId());
            if (reporter != null) {
                reporter.finalizeJtlFile(testResult.getScenarioId());
                jtlAdapter.finalizeJtlFile(testResult.getScenarioName());
            }
            
            // Generate HTML report
            String reportPath = getHtmlReportPath(testResult.getScenarioName());
            
            // Convert TestResult to a Map of metrics for the report generator
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("scenarioName", testResult.getScenarioName());
            metrics.put("success", testResult.isSuccess());
            metrics.put("statusCode", testResult.getStatusCode());
            metrics.put("responseTime", testResult.getResponseTime());
            metrics.put("startTime", testResult.getStartTime());
            metrics.put("endTime", testResult.getEndTime());
            
            String generatedPath = ReportGenerator.generateReport(testResult.getScenarioName(), metrics);
            generatedReports.add(generatedPath);
            
            return reportPath;
        } catch (Exception e) {
            logger.error("Error generating report for {}", testResult.getScenarioName(), e);
            return null;
        }
    }
    
    /**
     * Record a sample in the JTL file
     * 
     * @param testResult Test result to record
     * @return This component for chaining
     */
    public ReportingComponent recordSamples(TestResult testResult) {
        String scenarioId = testResult.getScenarioId();
        String scenarioName = testResult.getScenarioName();
        
        JtlReporter reporter = jtlReporters.get(scenarioId);
        if (reporter != null) {
            try {
                // Convert TestResult to a sample for JtlReporter
                Map<String, Object> sample = new HashMap<>();
                sample.put("timeStamp", testResult.getStartTime());
                sample.put("elapsed", testResult.getResponseTime());
                sample.put("label", testResult.getTestName());
                sample.put("responseCode", String.valueOf(testResult.getStatusCode()));
                sample.put("responseMessage", testResult.isSuccess() ? "OK" : "Error");
                sample.put("threadName", "Thread-1"); // Default thread name
                sample.put("dataType", "text");
                sample.put("success", testResult.isSuccess());
                sample.put("failureMessage", testResult.getError() != null ? testResult.getError() : "");
                sample.put("bytes", 0); // Default bytes
                sample.put("sentBytes", 0); // Default sent bytes
                sample.put("grpThreads", 1); // Default group threads
                sample.put("allThreads", 1); // Default all threads
                sample.put("URL", ""); // Default URL
                sample.put("Latency", 0); // Default latency
                sample.put("IdleTime", 0); // Default idle time
                sample.put("Connect", 0); // Default connect time
                
                reporter.recordSample(scenarioId, sample);
                
                // Convert TestResult to List for JmeterJtlAdapter
                List<TestResult> testResults = new ArrayList<>();
                testResults.add(testResult);
                jtlAdapter.recordSamples(scenarioName, testResults);
            } catch (Exception e) {
                logger.error("Error recording samples for {}", scenarioName, e);
            }
        } else {
            logger.warn("No reporter found for scenario ID: {}", scenarioId);
        }
        
        return this;
    }
    
    /**
     * Get path for a JTL report file
     * 
     * @param scenarioName Scenario name
     * @return Path to the JTL file
     */
    private String getReportPath(String scenarioName) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String timestamp = now.format(formatter);
        
        return "target/jmeter-reports/" + scenarioName + "_" + timestamp + ".jtl";
    }
    
    /**
     * Get path for an HTML report file
     * 
     * @param scenarioName Scenario name
     * @return Path to the HTML report file
     */
    private String getHtmlReportPath(String scenarioName) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = now.format(formatter);
        String safeName = scenarioName.replaceAll("[^a-zA-Z0-9]", "_");
        
        return reportDirectory + "/jmeter-dsl-" + safeName.toLowerCase() + "/performance_report_" +
               timestamp + "_" + safeName + ".html";
    }
    
    /**
     * Get the report directory
     * 
     * @return Report directory path
     */
    public String getReportDirectory() {
        return reportDirectory;
    }
    
    /**
     * Get list of generated reports
     * 
     * @return List of report file paths
     */
    public List<String> getGeneratedReports() {
        return new ArrayList<>(generatedReports);
    }
    
    /**
     * Get the JtlAdapter used for JMeter compatibility
     * 
     * @return JtlAdapter instance
     */
    public JmeterJtlAdapter getJtlAdapter() {
        return jtlAdapter;
    }
}