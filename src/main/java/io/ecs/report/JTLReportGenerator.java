package io.ecs.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.ecs.model.TestResult;

/**
 * JTLReportGenerator - Handles generation of JMeter JTL files and reports
 * 
 * This class provides methods to create standard JMeter JTL files in CSV format that are compatible
 * with JMeter's reporting tools. It also includes a method to generate HTML reports from JTL files
 * using the JMeter command line tool if available.
 * 
 * JTL (JMeter Test Log) files contain detailed metrics about each request that was executed during
 * a performance test. They can be used to analyze test results and to generate comprehensive
 * reports.
 */
public class JTLReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JTLReportGenerator.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    /**
     * Header row for JMeter JTL file in CSV format These columns follow JMeter's standard format
     * for CSV JTL files
     */
    private static final String JTL_CSV_HEADER =
            "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,"
                    + "bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";

    /**
     * Creates a JTL file in CSV format from test results
     * 
     * @param scenarioName The name of the test scenario
     * @param testResults A list of test results to include in the JTL file
     * @param reportDirectory The directory where the JTL file should be saved
     * @return The path to the created JTL file
     * @throws IOException If there's an error writing the file
     */
    public static String createJtlFile(String scenarioName, List<TestResult> testResults,
            String reportDirectory) throws IOException {
        // Create report directory if it doesn't exist
        File reportDir = new File(reportDirectory);
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        // Generate filename with timestamp
        String timestamp = DATE_FORMAT.format(new Date());
        String fileName =
                scenarioName.replaceAll("\\s+", "_").toLowerCase() + "_" + timestamp + ".jtl";
        String filePath = reportDirectory + "/" + fileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write(JTL_CSV_HEADER);
            writer.newLine();

            // Write each result as a row in the JTL file
            for (TestResult result : testResults) {
                // Format:
                // timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
                StringBuilder row = new StringBuilder();

                long timeStamp = System.currentTimeMillis(); // Current time or from result if
                                                             // available
                row.append(timeStamp).append(",");
                row.append(result.getResponseTime()).append(",");
                row.append(escapeForCsv(result.getTestName())).append(",");
                row.append(result.getStatusCode()).append(",");
                row.append(result.isSuccess() ? "OK" : "Error").append(",");
                row.append("Thread-1").append(","); // Thread name
                row.append("text").append(","); // Data type
                row.append(result.isSuccess() ? 1 : 0).append(","); // Convert boolean to int
                row.append(escapeForCsv(result.getError() != null ? result.getError() : ""))
                        .append(",");
                row.append(result.getReceivedBytes() > 0 ? result.getReceivedBytes()
                        : (result.getResponseBody() != null ? result.getResponseBody().length()
                                : 0))
                        .append(",");
                row.append("0").append(","); // sentBytes
                row.append("1").append(","); // grpThreads
                row.append("1").append(","); // allThreads
                row.append(escapeForCsv(
                        result.getProcessedEndpoint() != null ? result.getProcessedEndpoint() : ""))
                        .append(",");
                row.append(result.getResponseTime()).append(","); // Latency
                row.append("0").append(","); // IdleTime
                row.append("0"); // Connect

                writer.write(row.toString());
                writer.newLine();
            }
        }

        logger.info("JTL file created at: {}", filePath);
        return filePath;
    }

    /**
     * Generates a JMeter HTML report from a JTL file
     * 
     * This method attempts to use the JMeter command-line tool to generate an HTML report from a
     * JTL file. If JMeter is not available on the system, it logs a warning and returns null.
     * 
     * @param jtlFilePath The path to the JTL file
     * @param reportDirectory The directory where the report should be generated
     * @return The path to the generated report directory or null if generation failed
     */
    public static String generateJMeterReport(String jtlFilePath, String reportDirectory) {
        // Ensure report directory exists
        File reportDir = new File(reportDirectory);
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        // Generate timestamp for report directory
        String timestamp = DATE_FORMAT.format(new Date());
        String reportOutputDir = reportDirectory + "/jmeter-report-" + timestamp;

        try {
            // Check if JMeter is available
            Process process = Runtime.getRuntime().exec("jmeter -v");
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logger.warn("JMeter not found in PATH. Report generation skipped.");
                return null;
            }

            // Create JMeter command
            String jmeterCommand =
                    "jmeter -g " + jtlFilePath + " -o " + reportOutputDir + " -j jmeter-report.log";
            logger.info("Executing JMeter report command: {}", jmeterCommand);

            // Execute command
            process = Runtime.getRuntime().exec(jmeterCommand);
            process.waitFor(60, TimeUnit.SECONDS); // Wait up to 60 seconds for completion
            exitCode = process.exitValue();

            if (exitCode == 0) {
                logger.info("JMeter report generated successfully at: {}", reportOutputDir);
                return reportOutputDir;
            } else {
                logger.error("JMeter report generation failed with exit code: {}", exitCode);
                return null;
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error generating JMeter report: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to escape CSV field values
     * 
     * @param value The value to escape
     * @return The escaped value
     */
    private static String escapeForCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and special characters for CSV
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
