package io.perftest.ecs;

import io.perftest.model.Scenario;
import io.perftest.model.Request;
import io.perftest.model.TestResult;
import io.perftest.model.ExecutionConfig;
import io.perftest.engine.Engine;
import io.perftest.engine.EngineFactory;
import io.perftest.report.ReportGenerator;
import io.perftest.util.CsvReader;
import io.perftest.util.DynamicVariableResolver;

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
 * ECS (Entity Component System) based test runner
 * This provides a direct interface to the test system without relying on the App class
 */
public class ECSTestRunner {
    private static final Logger logger = LoggerFactory.getLogger(ECSTestRunner.class);
    
    // Components
    private final Map<String, String> globalVariables;
    private final List<Scenario> scenarios;
    private final String reportDirectory;
    private final List<Map<String, List<Map<String, String>>>> dataFiles;
    
    // Systems
    private final Map<String, Engine> engines;
    private final ReportGenerator reportGenerator;
    
    /**
     * Create a new ECS Test Runner
     */
    public ECSTestRunner() {
        this("target/reports");
    }
    
    /**
     * Create a new ECS Test Runner with a specific report directory
     * 
     * @param reportDirectory Directory to store test reports
     */
    public ECSTestRunner(String reportDirectory) {
        this.globalVariables = new HashMap<>();
        this.scenarios = new ArrayList<>();
        this.reportDirectory = reportDirectory;
        this.dataFiles = new ArrayList<>();
        this.engines = new HashMap<>();
        this.reportGenerator = new ReportGenerator();
        
        // Create report directory
        new File(reportDirectory).mkdirs();
    }
    
    /**
     * Add a global variable that will be available to all scenarios
     * 
     * @param name Variable name
     * @param value Variable value
     * @return This runner instance for chaining
     */
    public ECSTestRunner addGlobalVariable(String name, String value) {
        globalVariables.put(name, value);
        return this;
    }
    
    /**
     * Add multiple global variables at once
     * 
     * @param variables Map of variable name to value
     * @return This runner instance for chaining
     */
    public ECSTestRunner addGlobalVariables(Map<String, String> variables) {
        if (variables != null) {
            globalVariables.putAll(variables);
        }
        return this;
    }
    
    /**
     * Create and add a new scenario to the runner
     * 
     * @param name Scenario name
     * @param engineType Engine type (jmdsl, jmeter, etc.)
     * @return New scenario instance
     */
    public Scenario createScenario(String name, String engineType) {
        Scenario scenario = new Scenario();
        scenario.setName(name);
        scenario.setEngine(engineType);
        scenarios.add(scenario);
        return scenario;
    }
    
    /**
     * Add an existing scenario to the runner
     * 
     * @param scenario The scenario to add
     * @return This runner instance for chaining
     */
    public ECSTestRunner addScenario(Scenario scenario) {
        scenarios.add(scenario);
        return this;
    }
    
    /**
     * Add a data file to be used in the test
     * 
     * @param name Name to reference the data file
     * @param filePath Path to the CSV file
     * @return This runner instance for chaining
     */
    public ECSTestRunner addDataFile(String name, String filePath) {
        try {
            CsvReader csvReader = new CsvReader();
            List<Map<String, String>> data = csvReader.readCsv(filePath);
            
            Map<String, List<Map<String, String>>> dataFile = new HashMap<>();
            dataFile.put(name, data);
            dataFiles.add(dataFile);
            
            logger.info("Added data file: {} with {} records", name, data.size());
        } catch (Exception e) {
            logger.error("Error loading data file: {}", e.getMessage());
        }
        return this;
    }
    
    /**
     * Run all configured scenarios
     * 
     * @return List of aggregate test results
     */
    public List<Map<String, Object>> runAllScenarios() {
        List<Map<String, Object>> allResults = new ArrayList<>();
        
        for (Scenario scenario : scenarios) {
            Map<String, Object> result = runScenario(scenario);
            if (result != null) {
                allResults.add(result);
            }
        }
        
        return allResults;
    }
    
    /**
     * Run a specific scenario
     * 
     * @param scenario The scenario to run
     * @return Aggregate test results
     */
    public Map<String, Object> runScenario(Scenario scenario) {
        try {
            logger.info("Executing scenario: {}", scenario.getName());
            
            // Create execution configuration
            ExecutionConfig executionConfig = new ExecutionConfig();
            executionConfig.setThreads(scenario.getThreads());
            executionConfig.setIterations(scenario.getIterations());
            executionConfig.setRampUpSeconds(scenario.getRampUp());
            executionConfig.setHoldSeconds(scenario.getHold());
            executionConfig.setReportDirectory(reportDirectory);
            
            // Combine global variables with scenario variables
            Map<String, String> variables = new HashMap<>(globalVariables);
            if (scenario.getVariables() != null) {
                variables.putAll(scenario.getVariables());
            }
            executionConfig.setVariables(variables);
            
            // Get or create the appropriate engine
            String engineType = scenario.getEngine() != null ? scenario.getEngine() : "jmdsl";
            Engine engine = engines.computeIfAbsent(
                engineType + "_" + scenario.getName(), 
                k -> EngineFactory.getEngine(engineType, executionConfig)
            );
            
            engine.initialize(variables);
            
            // Process requests and collect results
            List<TestResult> results = new ArrayList<>();
            Map<String, Object> scenarioStats = new HashMap<>();
            
            // Execute each request in the scenario
            for (Request request : scenario.getRequests()) {
                TestResult result = engine.executeRequest(request);
                results.add(result);
                
                // Output results
                logger.info("Request: {} completed", request.getName());
                logger.info("Success: {}", result.isSuccess());
                logger.info("Status code: {}", result.getStatusCode());
                logger.info("Response time: {}ms", result.getResponseTime());
                
                // Get current metrics
                Map<String, Object> metrics = engine.getMetrics();
                
                // Log scenario stats
                logger.info("Scenario: {} stats so far", scenario.getName());
                logger.info("Total requests: {}", metrics.get("totalRequests"));
                logger.info("Success rate: {}%", metrics.get("successRate"));
                logger.info("Average response time: {}ms", metrics.get("avgResponseTime"));
                logger.info("Min/Max response time: {}/{}ms", 
                            metrics.get("minResponseTime"), metrics.get("maxResponseTime"));
                logger.info("90th percentile: {}ms", metrics.get("90thPercentile"));
                logger.info("--------------------------------------");
                
                // Use the last metrics as the scenario stats
                scenarioStats = metrics;
            }
            
            // Generate HTML report
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String reportPath = reportDirectory + "/performance_report_" + timestamp + ".html";
            
            List<Map<String, Object>> metricsForReport = new ArrayList<>();
            metricsForReport.add(scenarioStats);
            reportGenerator.createTestReport(reportPath, scenario, metricsForReport);
            
            logger.info("Test report generated at: {}", reportPath);
            
            return scenarioStats;
            
        } catch (Exception e) {
            logger.error("Error executing scenario {}: {}", scenario.getName(), e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Shut down all engines
     */
    public void shutdown() {
        engines.values().forEach(Engine::shutdown);
    }
    
    /**
     * Process dynamic variables in a text template
     * 
     * @param template Template string with ${var} placeholders
     * @return Processed string with variables substituted
     */
    public String processVariables(String template) {
        return DynamicVariableResolver.processTemplate(template, globalVariables, createDynamicContext());
    }
    
    /**
     * Create a standard dynamic context with common runtime variables
     * 
     * @return Map of dynamic context variables
     */
    private Map<String, Object> createDynamicContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("iteration", 1);
        context.put("threadNum", Thread.currentThread().getId());
        context.put("timestamp", System.currentTimeMillis());
        return context;
    }
}