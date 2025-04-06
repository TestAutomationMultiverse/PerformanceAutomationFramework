package io.perftest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class to configure JMeter logging
 * This class sets the system property to redirect JMeter logs to the test-specific folders
 * 
 * Updated to use the unified reporting structure that stores JTL files, HTML reports,
 * and logs in a consistent directory structure:
 * target/unified-reports/protocol_timestamp_uniqueId/
 *   - jtl files
 *   - html/ (contains HTML reports)
 *   - logs/ (contains log files)
 */
public class JMeterLogConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(JMeterLogConfiguration.class);
    private static boolean initialized = false;
    private static final String DEFAULT_PROTOCOL = "default";
    
    // Store the timestamp and unique ID that get generated during log configuration
    // These can then be reused for JTL and HTML reports
    private static String currentTimestamp;
    private static String currentUniqueId;
    
    // Base directories for unified reporting structure
    private static final String UNIFIED_REPORTS_BASE = "target/unified-reports";
    
    // Unified test directory for the current test run
    private static Path unifiedTestDir;
    
    /**
     * Configure JMeter logging to use the target directory
     * This method sets the jmeter.logfile system property to point to a test-specific log file
     */
    public static void configureJMeterLogs() {
        configureJMeterLogs(DEFAULT_PROTOCOL);
    }
    
    /**
     * Configure JMeter logging for a specific protocol
     * This method sets the jmeter.logfile system property to point to a protocol-specific log file
     * 
     * @param protocolName The name of the protocol being tested
     */
    public static void configureJMeterLogs(String protocolName) {
        try {
            String logFile = configureLogging(protocolName);
            logger.info("JMeter logs configured to use {}", logFile);
            initialized = true;
        } catch (IOException e) {
            logger.error("Failed to configure JMeter logs: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Configure JMeter logging for a specific protocol and return the log file path
     * This method sets the jmeter.logfile system property to point to a protocol-specific log file
     * 
     * @param protocolName The name of the protocol being tested
     * @return The path to the configured log file as a string
     * @throws IOException If there is an error creating the log directories
     */
    public static String configureLogging(String protocolName) throws IOException {
        // Create base directories if they don't exist
        Path targetDir = Paths.get("target");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        
        // Create protocol-specific log directory with timestamp and unique ID
        // Format timestamp as yyyyMMdd_HHmmss to make it filename-friendly
        currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        currentUniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        // Create a unified report directory with protocol_timestamp_uniqueId pattern
        String dirName = String.format("%s_%s_%s", 
                protocolName, 
                currentTimestamp, 
                currentUniqueId);
        
        // Create the unified report directory
        unifiedTestDir = Paths.get(UNIFIED_REPORTS_BASE, dirName);
        if (!Files.exists(unifiedTestDir)) {
            Files.createDirectories(unifiedTestDir);
        }
        
        // Create logs subdirectory
        Path logsDir = unifiedTestDir.resolve("logs");
        if (!Files.exists(logsDir)) {
            Files.createDirectories(logsDir);
        }
        
        // Create a unique log file name for this test run
        Path logFile = logsDir.resolve("jmeter.log");
        
        // Set JMeter log file location
        System.setProperty("jmeter.logfile", logFile.toString());
        
        // Also try to set Apache JMeter log file property (alternative property name)
        System.setProperty("apache.jmeter.log", logFile.toString());
        
        // Let's try setting the log directory as well
        System.setProperty("jmeter.home", logsDir.toString());
        
        // Set output format to CSV to ensure compatibility with JMeter's HTML reporter
        // Note: HtmlReporter explicitly requires CSV format
        System.setProperty("jmeter.save.saveservice.output_format", "csv");

        // Configure required fields for CSV format
        System.setProperty("jmeter.save.saveservice.timestamp_format", "yyyy/MM/dd HH:mm:ss.SSS");
        System.setProperty("jmeter.save.saveservice.print_field_names", "true");
        System.setProperty("jmeter.save.saveservice.successful", "true");
        System.setProperty("jmeter.save.saveservice.label", "true");
        System.setProperty("jmeter.save.saveservice.time", "true");
        System.setProperty("jmeter.save.saveservice.thread_name", "true");
        System.setProperty("jmeter.save.saveservice.data_type", "true");
        System.setProperty("jmeter.save.saveservice.message", "true");
        System.setProperty("jmeter.save.saveservice.response_code", "true");
        System.setProperty("jmeter.save.saveservice.response_message", "true");
        System.setProperty("jmeter.save.saveservice.subresults", "true");
        System.setProperty("jmeter.save.saveservice.assertions", "true");
        System.setProperty("jmeter.save.saveservice.latency", "true");
        System.setProperty("jmeter.save.saveservice.connect_time", "true");
        System.setProperty("jmeter.save.saveservice.bytes", "true");
        System.setProperty("jmeter.save.saveservice.sent_bytes", "true");
        System.setProperty("jmeter.save.saveservice.sample_count", "true");
        System.setProperty("jmeter.save.saveservice.idle_time", "true");
        System.setProperty("jmeter.save.saveservice.thread_counts", "true");
        System.setProperty("jmeter.save.saveservice.assertion_results_failure_message", "true");
        
        // Configure JMeter to use our unified directory structure for HTML reports
        Path htmlDir = unifiedTestDir.resolve("html");
        if (!Files.exists(htmlDir)) {
            Files.createDirectories(htmlDir);
        }
        System.setProperty("jmeter.reportgenerator.exporter.html.property.output_dir", htmlDir.toString());
        
        logger.info("Configured unified report directory: {}", unifiedTestDir);
        logger.info("Log file: {}", logFile);
        
        return logFile.toString();
    }
    
    /**
     * Get the current timestamp for this test run
     * This can be used to create consistently named directories for logs, JTL files, and HTML reports
     * 
     * @return The current timestamp string (yyyyMMdd_HHmmss format)
     */
    public static String getCurrentTimestamp() {
        // Generate a new timestamp if none exists yet
        if (currentTimestamp == null) {
            currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        }
        return currentTimestamp;
    }
    
    /**
     * Get the current unique ID for this test run
     * 
     * @return The current unique ID string
     */
    public static String getCurrentUniqueId() {
        // Generate a new unique ID if none exists yet
        if (currentUniqueId == null) {
            currentUniqueId = UUID.randomUUID().toString().substring(0, 8);
        }
        return currentUniqueId;
    }
    
    /**
     * Get a consistent directory name string for this test run
     * This ensures logs, JTL files, and HTML reports all use the same naming pattern
     * 
     * @return A directory name string with timestamp and unique ID
     */
    public static String getRunDirectoryName() {
        return getCurrentTimestamp() + "_" + getCurrentUniqueId();
    }
    
    /**
     * Get the unified report directory for the current test run
     * 
     * @return Path to the unified report directory
     */
    public static Path getUnifiedReportDirectory() {
        return unifiedTestDir;
    }
    
    /**
     * Get the unified report directory path as a string
     * 
     * @return String path to the unified report directory
     */
    public static String getUnifiedReportDirectoryPath() {
        if (unifiedTestDir == null) {
            return null;
        }
        return unifiedTestDir.toString();
    }
    
    /**
     * Get the JTL file path for the current test run
     * This assumes the JTL file will be placed directly in the unified report directory
     * 
     * @param fileName JTL file name to use (if null, a default name will be used)
     * @return Path to the JTL file
     */
    public static Path getJtlFilePath(String fileName) {
        if (unifiedTestDir == null) {
            logger.warn("Unified report directory not initialized, cannot get JTL file path");
            return null;
        }
        
        if (fileName == null || fileName.isEmpty()) {
            fileName = "results.jtl";
        }
        
        return unifiedTestDir.resolve(fileName);
    }
    
    /**
     * Get the HTML report directory for the current test run
     * 
     * @return Path to the HTML report directory
     */
    public static Path getHtmlReportDirectory() {
        if (unifiedTestDir == null) {
            logger.warn("Unified report directory not initialized, cannot get HTML report directory");
            return null;
        }
        
        return unifiedTestDir.resolve("html");
    }
    
    /**
     * Get the logs directory for the current test run
     * 
     * @return Path to the logs directory
     */
    public static Path getLogsDirectory() {
        if (unifiedTestDir == null) {
            logger.warn("Unified report directory not initialized, cannot get logs directory");
            return null;
        }
        
        return unifiedTestDir.resolve("logs");
    }
}
