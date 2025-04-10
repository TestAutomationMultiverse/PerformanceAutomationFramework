package io.perftest;

import io.perftest.model.Scenario; 
import io.perftest.config.TestConfiguration;
import io.perftest.config.YamlConfig;
import io.perftest.engine.Engine;
import io.perftest.engine.EngineFactory;
import io.perftest.engine.JMDSLEngine;
import io.perftest.model.ExecutionConfig;
import io.perftest.model.Request;
import io.perftest.model.TestResult;
import io.perftest.report.ReportGenerator;
import io.perftest.util.CsvReader;
import io.perftest.util.FileUtils;
import io.perftest.util.TemplateProcessor;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Application Entry Point for YAML Configuration-driven Performance Testing
 * 
 * This class provides the command-line interface for executing performance tests
 * defined in YAML configuration files. It reads the YAML configuration, processes
 * test scenarios and requests, executes tests, and generates detailed HTML reports.
 * 
 * Features:
 * - YAML-based configuration for test definition
 * - Variable substitution for dynamic request content
 * - CSV data source integration for data-driven testing
 * - Multi-protocol support through the Engine and Protocol interfaces
 * - Detailed metrics collection and reporting
 * 
 * Usage:
 * java -jar performance-test.jar <config-file.yaml>
 * 
 * Example:
 * java -jar performance-test.jar src/main/resources/configs/sample_config.yaml
 * 
 * For programmatic API usage without YAML, see JMeterDSLTest class.
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar performance-test.jar <config-file.yaml>");
            System.out.println("For direct HTTP testing without YAML configuration, use DirectHttpTest class.");
            System.exit(1);
        }

        String configFile = args[0];
        try {
            // Check if file exists
            File configYamlFile = new File(configFile);
            if (!configYamlFile.exists()) {
                System.err.println("Error: Configuration file '" + configFile + "' not found.");
                System.exit(1);
            }
            
            // Load and parse YAML configuration
            String yamlContent = FileUtils.readFileAsString(configFile);
            YamlConfig yamlConfig = new YamlConfig();
            TestConfiguration testConfig = yamlConfig.parse(yamlContent);
            
            // Execute tests for each scenario
            for (Scenario scenario : testConfig.getScenarios()) {
                executeScenario(scenario, testConfig);
            }
            
            System.out.println("Test execution completed successfully.");
            
        } catch (Exception e) {
            System.err.println("Error executing performance test: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Execute a single test scenario with all its requests
     * 
     * This method handles:
     * 1. Setting up the execution configuration (threads, iterations, timing)
     * 2. Processing global and scenario-specific variables
     * 3. Loading data from CSV files for data-driven testing
     * 4. Executing each request with variable substitution
     * 5. Collecting and reporting metrics
     * 6. Generating HTML test reports
     * 
     * @param scenario The test scenario to execute
     * @param config The global test configuration containing shared variables
     * @throws Exception If an error occurs during test execution
     */
    private static void executeScenario(Scenario scenario, TestConfiguration config) throws Exception {
        logger.info("Executing scenario: {}", scenario.getName());
        
        // Create execution configuration
        ExecutionConfig executionConfig = new ExecutionConfig();
        executionConfig.setThreads(scenario.getThreads());
        executionConfig.setIterations(scenario.getIterations());
        executionConfig.setRampUpSeconds(scenario.getRampUp());
        executionConfig.setHoldSeconds(scenario.getHold());
        
        // Combine global variables with scenario variables
        Map<String, String> variables = new HashMap<>();
        if (config.getVariables() != null) {
            variables.putAll(config.getVariables());
        }
        if (scenario.getVariables() != null) {
            variables.putAll(scenario.getVariables());
        }
        executionConfig.setVariables(variables);
        
        // Get the appropriate engine (JMeter DSL by default)
        String engineType = scenario.getEngine() != null ? scenario.getEngine() : "jmdsl";
        Engine engine = EngineFactory.getEngine(engineType, executionConfig);
        engine.initialize(variables);
        
        // Load data from CSV files if specified
        Map<String, List<Map<String, String>>> dataFiles = new HashMap<>();
        if (scenario.getDataFiles() != null && !scenario.getDataFiles().isEmpty()) {
            for (Map.Entry<String, String> entry : scenario.getDataFiles().entrySet()) {
                String dataName = entry.getKey();
                String dataFile = entry.getValue();
                
                CsvReader csvReader = new CsvReader();
                List<Map<String, String>> data = csvReader.readCsv(dataFile);
                dataFiles.put(dataName, data);
            }
        }
        
        // Process requests
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Execute each request and collect metrics
        for (Request request : scenario.getRequests()) {
            // Get data for request if specified
            List<Map<String, String>> requestData = null;
            if (request.getDataSource() != null && dataFiles.containsKey(request.getDataSource())) {
                requestData = dataFiles.get(request.getDataSource());
            }
            
            // Process request with template substitution for all parameters
            TemplateProcessor processor = new TemplateProcessor();
            
            // Add baseUrl to endpoint if it doesn't already have a full URL
            String endpoint = request.getEndpoint();
            if (endpoint != null && !endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                String baseUrl = variables.getOrDefault("baseUrl", "");
                if (!baseUrl.isEmpty()) {
                    // Ensure baseUrl doesn't end with / and endpoint starts with /
                    if (baseUrl.endsWith("/") && endpoint.startsWith("/")) {
                        endpoint = baseUrl + endpoint.substring(1);
                    } else if (!baseUrl.endsWith("/") && !endpoint.startsWith("/")) {
                        endpoint = baseUrl + "/" + endpoint;
                    } else {
                        endpoint = baseUrl + endpoint;
                    }
                }
            }
            
            // Process variables in the endpoint
            String processedEndpoint = processor.processTemplate(endpoint, variables);
            String processedMethod = processor.processTemplate(request.getMethod(), variables);
            String processedBody = request.getBody() != null ? 
                processor.processTemplate(request.getBody(), variables) : null;
            
            // Process headers with variable substitution
            Map<String, String> processedHeaders = new HashMap<>();
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                    String headerValue = processor.processTemplate(header.getValue(), variables);
                    processedHeaders.put(header.getKey(), headerValue);
                }
            }
            
            // Process params with variable substitution
            Map<String, String> processedParams = new HashMap<>();
            if (request.getParams() != null) {
                for (Map.Entry<String, String> param : request.getParams().entrySet()) {
                    String paramValue = processor.processTemplate(param.getValue(), variables);
                    processedParams.put(param.getKey(), paramValue);
                }
            }
            
            // Execute using our Engine interface
            
            // Update the request with processed values
            request.setEndpoint(processedEndpoint);
            request.setMethod(processedMethod);
            request.setBody(processedBody);
            request.setHeaders(processedHeaders);
            request.setParams(processedParams);
            
            // Execute the request
            TestResult testResult = engine.executeRequest(request);
            
            // Collect metrics for reporting
            Map<String, Object> metrics = engine.getMetrics();
            results.add(metrics);
            
            // Output results
            System.out.println("Request: " + request.getName() + " completed");
            System.out.println("Success: " + testResult.isSuccess());
            System.out.println("Status code: " + testResult.getStatusCode());
            System.out.println("Response time: " + testResult.getResponseTime() + "ms");
            
            System.out.println("Scenario: " + scenario.getName() + " stats so far");
            System.out.println("Total requests: " + metrics.get("totalRequests"));
            System.out.println("Success rate: " + metrics.get("successRate") + "%");
            System.out.println("Average response time: " + metrics.get("avgResponseTime") + "ms");
            System.out.println("Min/Max response time: " + metrics.get("minResponseTime") + "/" 
                              + metrics.get("maxResponseTime") + "ms");
            System.out.println("90th percentile: " + metrics.get("90thPercentile") + "ms");
            System.out.println("--------------------------------------");
        }
        
        // Generate HTML report
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String reportPath = "target/reports/performance_report_" + timestamp + ".html";
        
        // Initialize ReportGenerator
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.createTestReport(reportPath, scenario, results);
        
        System.out.println("Test report generated at: " + reportPath);
        
        // Clean up engine
        engine.shutdown();
    }
}
