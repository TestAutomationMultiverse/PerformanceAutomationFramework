package io.ecs.report;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JtlReporter is responsible for creating JTL files (CSV format) from test results.
 * JTL files are compatible with JMeter reporting standards.
 * 
 * This implementation follows the Entity-Component-System pattern by acting as a 
 * system that processes and records test result entities.
 */
public class JtlReporter {
    private static final Logger LOGGER = Logger.getLogger(JtlReporter.class.getName());
    
    // JMeter JTL CSV columns
    private static final String[] JTL_HEADERS = {
        "timeStamp", "elapsed", "label", "responseCode", "responseMessage", 
        "threadName", "dataType", "success", "failureMessage", "bytes", 
        "sentBytes", "grpThreads", "allThreads", "URL", "Latency", 
        "IdleTime", "Connect"
    };

    private final String outputDirectory;
    private final Map<String, FileWriter> testWriters = new ConcurrentHashMap<>();

    /**
     * Create a new JtlReporter with the default output directory
     */
    public JtlReporter() {
        this("target/jmeter-reports");
    }

    /**
     * Create a new JtlReporter with a specific output directory
     * 
     * @param outputDirectory Directory to output JTL files
     */
    public JtlReporter(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        createOutputDirectoryIfNotExists();
    }

    /**
     * Ensure the output directory exists
     */
    private void createOutputDirectoryIfNotExists() {
        Path dirPath = Paths.get(outputDirectory);
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                LOGGER.info("Created output directory: " + outputDirectory);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error creating output directory: " + outputDirectory, e);
        }
    }

    /**
     * Initialize a JTL file for a specific test
     * 
     * @param testName name of the test
     * @return unique test ID for tracking
     */
    public String initializeJtlFile(String testName) {
        String testId = UUID.randomUUID().toString();
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String filename = String.format("%s/%s_%s.jtl", outputDirectory, testName, timestamp);
            
            FileWriter writer = new FileWriter(filename);
            // Write CSV headers
            writer.write(String.join(",", JTL_HEADERS) + "\n");
            testWriters.put(testId, writer);
            
            LOGGER.info("Initialized JTL file: " + filename);
            return testId;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error initializing JTL file for test: " + testName, e);
            return null;
        }
    }

    /**
     * Records a single sample result to the JTL file
     * 
     * @param testId the test ID returned from initializeJtlFile
     * @param sampleResult the sample result data
     */
    public void recordSample(String testId, Map<String, Object> sampleResult) {
        FileWriter writer = testWriters.get(testId);
        if (writer == null) {
            LOGGER.warning("No JTL file found for test ID: " + testId);
            return;
        }
        
        try {
            // Create a row with values in the correct order
            List<String> values = createCsvRow(JTL_HEADERS, sampleResult);
            writer.write(String.join(",", values) + "\n");
            writer.flush(); // Flush to ensure data is written immediately
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing to JTL file for test ID: " + testId, e);
        }
    }

    /**
     * Finalizes the JTL file for a test
     * 
     * @param testId the test ID returned from initializeJtlFile
     */
    public void finalizeJtlFile(String testId) {
        FileWriter writer = testWriters.get(testId);
        if (writer == null) {
            LOGGER.warning("No JTL file found for test ID: " + testId);
            return;
        }
        
        try {
            writer.flush();
            writer.close();
            testWriters.remove(testId);
            LOGGER.info("Finalized JTL file for test ID: " + testId);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error finalizing JTL file for test ID: " + testId, e);
        }
    }

    /**
     * Creates a CSV row from a map of values
     * 
     * @param headers array of header names in the desired order
     * @param data map of data values
     * @return list of values in CSV format
     */
    private List<String> createCsvRow(String[] headers, Map<String, Object> data) {
        List<String> row = new ArrayList<>();
        for (String header : headers) {
            Object value = data.get(header);
            row.add(formatCsvValue(value));
        }
        return row;
    }
    
    /**
     * Format a value for CSV output
     * 
     * @param value the value to format
     * @return formatted CSV value
     */
    private String formatCsvValue(Object value) {
        if (value == null) {
            return "";
        }
        
        String strValue = value.toString();
        
        // Escape double quotes with double quotes
        if (strValue.contains("\"") || strValue.contains(",") || strValue.contains("\n")) {
            strValue = strValue.replace("\"", "\"\"");
            return "\"" + strValue + "\"";
        }
        
        return strValue;
    }

    /**
     * Creates a sample result map compatible with JMeter JTL format
     */
    public static Map<String, Object> createSampleResult(
            long timeStamp, 
            long elapsed, 
            String label, 
            String responseCode, 
            String responseMessage,
            String threadName, 
            String dataType, 
            boolean success, 
            String failureMessage, 
            long bytes,
            long sentBytes, 
            int grpThreads, 
            int allThreads, 
            String url, 
            long latency,
            long idleTime, 
            long connect) {
        
        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("timeStamp", timeStamp);
        result.put("elapsed", elapsed);
        result.put("label", label);
        result.put("responseCode", responseCode);
        result.put("responseMessage", responseMessage);
        result.put("threadName", threadName);
        result.put("dataType", dataType);
        result.put("success", success);
        result.put("failureMessage", failureMessage);
        result.put("bytes", bytes);
        result.put("sentBytes", sentBytes);
        result.put("grpThreads", grpThreads);
        result.put("allThreads", allThreads);
        result.put("URL", url);
        result.put("Latency", latency);
        result.put("IdleTime", idleTime);
        result.put("Connect", connect);
        
        return result;
    }
}