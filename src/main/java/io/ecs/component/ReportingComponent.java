package io.ecs.component;

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
        
        try {
            reporter.initializeJtlFile(reportPath);
            jtlAdapter.initializeJtlFile(scenario);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error initializing reporting for " + scenario.getName(), e);
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
            reportGenerator.generateHtmlReport(testResult, reportPath);
            generatedReports.add(reportPath);
            
            return reportPath;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report for " + testResult.getScenarioName(), e);
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
                reporter.recordSamples(testResult);
                jtlAdapter.recordSamples(scenarioName, testResult);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error recording samples for " + scenarioName, e);
            }
        } else {
            LOGGER.warning("No reporter found for scenario ID: " + scenarioId);
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