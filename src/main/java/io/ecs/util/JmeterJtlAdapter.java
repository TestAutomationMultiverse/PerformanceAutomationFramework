package io.ecs.util;

import io.ecs.model.TestResult;
import io.ecs.report.JtlReporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapter class that integrates the JMeter DSL test results with the JtlReporter
 * This allows generating standard JMeter JTL (CSV) files from JMeter DSL test execution
 */
public class JmeterJtlAdapter {
    private static final Logger LOGGER = Logger.getLogger(JmeterJtlAdapter.class.getName());
    
    // We need to maintain a single JtlReporter instance for the entire application
    private static final JtlReporter jtlReporter = new JtlReporter("target/jmeter-reports");
    // Keep track of test IDs by scenario name
    private static final Map<String, String> activeTests = new ConcurrentHashMap<>();
    
    /**
     * Initializes a JTL file for a specific test scenario
     * @param scenarioName name of the scenario
     * @return the test ID for tracking
     */
    public static String initializeJtlFile(String scenarioName) {
        // Check if we already have an active test for this scenario
        if (activeTests.containsKey(scenarioName)) {
            // Close the previous one first
            finalizeJtlFile(scenarioName);
        }
        
        // Initialize a new JTL file
        String testId = jtlReporter.initializeJtlFile(scenarioName);
        if (testId != null) {
            activeTests.put(scenarioName, testId);
            LOGGER.info("Initialized JTL file for scenario: " + scenarioName + " with ID: " + testId);
        } else {
            LOGGER.warning("Failed to initialize JTL file for scenario: " + scenarioName);
        }
        return testId;
    }
    
    /**
     * Converts a TestResult to a JMeter JTL sample result format
     * @param testResult the test result to convert
     * @return a map containing the JTL sample result data
     */
    public static Map<String, Object> convertToJtlSample(TestResult testResult) {
        long timestamp = System.currentTimeMillis();
        String threadName = "Thread-" + Thread.currentThread().getId();
        String dataType = "text";
        
        // Calculate bytes if not set
        long bytes = testResult.getReceivedBytes();
        if (bytes == 0 && testResult.getResponseBody() != null) {
            bytes = testResult.getResponseBody().getBytes().length;
        }
        
        // Get URL from endpoint or use a default
        String url = testResult.getProcessedEndpoint();
        if (url == null || url.isEmpty()) {
            url = "http://example.com/api"; // Default URL in case it's not set
        }
        
        // Get error message if any
        String failureMessage = "";
        if (!testResult.isSuccess() && testResult.getError() != null) {
            failureMessage = testResult.getError();
        }
        
        return JtlReporter.createSampleResult(
            timestamp,                                  // timeStamp
            testResult.getResponseTime(),               // elapsed
            testResult.getTestName() != null ? testResult.getTestName() : "Unknown Test", // label
            String.valueOf(testResult.getStatusCode()), // responseCode
            testResult.isSuccess() ? "OK" : "Error",    // responseMessage
            threadName,                                 // threadName
            dataType,                                   // dataType
            testResult.isSuccess(),                     // success
            failureMessage,                             // failureMessage
            bytes,                                      // bytes
            0L,                                         // sentBytes (not tracked)
            1,                                          // grpThreads
            1,                                          // allThreads
            url,                                        // URL
            testResult.getResponseTime(),               // Latency (same as response time)
            0L,                                         // IdleTime
            0L                                          // Connect
        );
    }
    
    /**
     * Records a sample result for a test
     * @param scenarioName the name of the scenario
     * @param testResult the test result to record
     */
    public static void recordSample(String scenarioName, TestResult testResult) {
        if (testResult == null) {
            LOGGER.warning("Attempted to record null test result for scenario: " + scenarioName);
            return;
        }
        
        // Make sure we have an active test ID
        String testId = activeTests.get(scenarioName);
        if (testId == null) {
            LOGGER.warning("No active test found for scenario: " + scenarioName + ". Initializing...");
            testId = initializeJtlFile(scenarioName);
            if (testId == null) {
                LOGGER.severe("Failed to initialize JTL file for: " + scenarioName);
                return;
            }
        }
        
        try {
            // Set scenario name as test name if not set
            if (testResult.getTestName() == null || testResult.getTestName().isEmpty()) {
                testResult.setTestName(scenarioName);
            }
            
            // Convert and record the sample
            Map<String, Object> sampleResult = convertToJtlSample(testResult);
            jtlReporter.recordSample(testId, sampleResult);
            LOGGER.fine("Recorded sample for scenario: " + scenarioName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording sample for scenario: " + scenarioName, e);
        }
    }
    
    /**
     * Records multiple sample results for a test
     * @param scenarioName the name of the scenario
     * @param testResults the list of test results to record
     */
    public static void recordSamples(String scenarioName, List<TestResult> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            LOGGER.warning("No test results to record for scenario: " + scenarioName);
            return;
        }
        
        // Make sure we have an active test ID
        String testId = activeTests.get(scenarioName);
        if (testId == null) {
            LOGGER.info("No active test found for scenario: " + scenarioName + ". Initializing...");
            testId = initializeJtlFile(scenarioName);
            if (testId == null) {
                LOGGER.severe("Failed to initialize JTL file for: " + scenarioName);
                return;
            }
        }
        
        try {
            // Record each sample
            for (TestResult result : testResults) {
                if (result == null) continue;
                
                // Set scenario name as test name if not set
                if (result.getTestName() == null || result.getTestName().isEmpty()) {
                    result.setTestName(scenarioName);
                }
                
                Map<String, Object> sampleResult = convertToJtlSample(result);
                jtlReporter.recordSample(testId, sampleResult);
            }
            LOGGER.info("Recorded " + testResults.size() + " samples for scenario: " + scenarioName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording samples for scenario: " + scenarioName, e);
        }
    }
    
    /**
     * Finalizes the JTL file for a test
     * @param scenarioName the name of the scenario
     */
    public static void finalizeJtlFile(String scenarioName) {
        String testId = activeTests.get(scenarioName);
        if (testId == null) {
            LOGGER.warning("No active test found with name: " + scenarioName);
            return;
        }
        
        try {
            jtlReporter.finalizeJtlFile(testId);
            activeTests.remove(scenarioName);
            LOGGER.info("Finalized JTL file for scenario: " + scenarioName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finalizing JTL file for scenario: " + scenarioName, e);
        }
    }
    
    /**
     * Makes sure the JTL output directory exists
     * @param dirPath the path to check/create
     */
    public static void ensureDirectoryExists(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                LOGGER.info("Created directory: " + dirPath);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error creating directory: " + dirPath, e);
        }
    }
    
    /**
     * Add a dummy sample to ensure the JTL file has at least one record
     * @param scenarioName the name of the scenario
     */
    public static void addDummySample(String scenarioName) {
        TestResult dummyResult = new TestResult();
        dummyResult.setTestName(scenarioName + " Summary");
        dummyResult.setSuccess(true);
        dummyResult.setStatusCode(200);
        dummyResult.setResponseTime(0);
        dummyResult.setProcessedEndpoint("http://example.com/api");
        dummyResult.setResponseBody("Test completed successfully");
        
        recordSample(scenarioName, dummyResult);
    }
}