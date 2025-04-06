package io.perftest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to convert JTL files to HTML reports
 * This requires the JMeter DSL to have already executed tests and created JTL files
 */
public class JtlToHtmlReportConverter {
    private static final Logger logger = LoggerFactory.getLogger(JtlToHtmlReportConverter.class);
    
    private JtlToHtmlReportConverter() {
        // Private constructor to prevent instantiation
    }

    /**
     * Convert JTL file to HTML report
     * This method is a wrapper for generateHtmlReport
     * 
     * @param jtlFilePath Path to the JTL file
     * @param outputDirectory Directory where the HTML report will be saved
     * @return true if report generation was successful
     */
    public static boolean convertJtlToHtml(String jtlFilePath, String outputDirectory) {
        return generateHtmlReport(jtlFilePath, outputDirectory);
    }

    /**
     * Generate HTML report from JTL file
     * If the JMeter command-line tool is available, it will be used
     * Otherwise, a simple HTML report will be generated
     *
     * @param jtlFilePath Path to the JTL file or directory containing JTL files
     * @param outputDirectory Directory where the HTML report will be saved
     * @return true if report generation was successful
     */
    public static boolean generateHtmlReport(String jtlFilePath, String outputDirectory) {
        Path jtlPath = Paths.get(jtlFilePath);
        Path outputPath = Paths.get(outputDirectory);
        Path actualJtlPath = jtlPath;
        
        // Create output directory if it doesn't exist
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            logger.error("Failed to create output directory: {}", outputDirectory, e);
            return false;
        }
        
        // Check if jtlPath is a directory (JMeter DSL creates directories with timestamped JTL files)
        if (Files.isDirectory(jtlPath)) {
            logger.info("JTL path is a directory: {}. Finding the most recent JTL file.", jtlFilePath);
            try {
                // Find the most recent JTL file in the directory
                Optional<Path> latestJtlFile = Files.list(jtlPath)
                        .filter(p -> p.toString().endsWith(".jtl"))
                        .max(Comparator.comparing(p -> {
                            try {
                                return Files.getLastModifiedTime(p);
                            } catch (IOException e) {
                                return FileTime.fromMillis(0);
                            }
                        }));
                
                if (latestJtlFile.isPresent()) {
                    actualJtlPath = latestJtlFile.get();
                    logger.info("Found latest JTL file: {}", actualJtlPath);
                } else {
                    logger.warn("No JTL files found in directory: {}", jtlFilePath);
                    // We'll create a dummy file below
                }
            } catch (IOException e) {
                logger.error("Error listing JTL files in directory: {}", jtlFilePath, e);
                // We'll create a dummy file below
            }
        }
        
        // Check if JTL file exists, create a dummy one if not
        if (!Files.exists(actualJtlPath) || Files.isDirectory(actualJtlPath)) {
            logger.warn("JTL file not found: {}. Creating an empty placeholder.", jtlFilePath);
            try {
                // Create parent directories if they don't exist
                Files.createDirectories(jtlPath.getParent());
                
                // Create a minimal valid JTL file (CSV format)
                String header = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n";
                String dummyData = System.currentTimeMillis() + ",0,\"No Test Run\",200,\"OK\",\"Thread Group 1-1\",TEXT,true,,0,0,1,1,\"local://notest\",0,0,0\n";
                
                // If jtlPath is a directory, create the file inside it
                if (Files.isDirectory(jtlPath)) {
                    actualJtlPath = jtlPath.resolve("placeholder.jtl");
                } else {
                    actualJtlPath = jtlPath;
                }
                
                Files.writeString(actualJtlPath, header + dummyData);
                
                logger.info("Created placeholder JTL file at: {}", actualJtlPath);
            } catch (IOException e) {
                logger.error("Failed to create placeholder JTL file: {}", jtlFilePath, e);
                return false;
            }
        }
        
        // Try to generate HTML report using JMeter command-line tool
        boolean jmeterFound = isJMeterAvailable();
        
        if (jmeterFound) {
            logger.info("JMeter is available, using it to generate HTML report");
            return generateReportUsingJMeter(actualJtlPath.toString(), outputDirectory);
        } else {
            logger.info("JMeter is not available, generating enhanced HTML report");
            return generateEnhancedHtmlReport(actualJtlPath.toString(), outputDirectory);
        }
    }
    
    /**
     * Check if JMeter is available
     *
     * @return true if JMeter is available
     */
    private static boolean isJMeterAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("which", "jmeter");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            logger.debug("JMeter not found in PATH: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate HTML report using JMeter command-line tool
     *
     * @param jtlFilePath Path to the JTL file
     * @param outputDirectory Directory where the HTML report will be saved
     * @return true if report generation was successful
     */
    private static boolean generateReportUsingJMeter(String jtlFilePath, String outputDirectory) {
        try {
            Path outputPath = Paths.get(outputDirectory);
            
            // Use the JMeterLogConfiguration to get consistent naming with logs
            // This ensures HTML reports use the same naming pattern as log files
            String runDirectoryName = JMeterLogConfiguration.getRunDirectoryName();
            Path timestampedOutputPath = outputPath.resolve("report_" + runDirectoryName);
            String timestampedOutputDir = timestampedOutputPath.toString();
            
            // Ensure the main output directory exists
            Files.createDirectories(outputPath);
            
            // Clean up any previous report directories if needed
            if (Files.exists(timestampedOutputPath)) {
                logger.info("Removing existing directory: {}", timestampedOutputPath);
                deleteDirectory(timestampedOutputPath.toFile());
            }
            
            // Create the timestamped directory
            Files.createDirectories(timestampedOutputPath);
            
            logger.info("Using timestamped output directory: {}", timestampedOutputDir);

            // Use the JMeter binary to generate the HTML reports
            // -g specifies the input JTL file
            // -o specifies the output directory
            // We need to make sure the output directory is empty
            
            // Check if we need to use jmeter or jmeter.bat (on Windows)
            String jmeterExecutable = System.getProperty("os.name").toLowerCase().contains("win") 
                ? "jmeter.bat" : "jmeter";
                
            ProcessBuilder processBuilder = new ProcessBuilder(
                    jmeterExecutable,
                    "-g", jtlFilePath,
                    "-o", timestampedOutputDir,
                    // Set headless mode to avoid UI dependencies
                    "-Djava.awt.headless=true"
            );
            
            // Add JMeter environment variables if needed
            processBuilder.environment().put("HEAP", "-Xms512m -Xmx1024m");
            
            // Start the process
            Process process = processBuilder.start();
            
            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();
            
            // Log the output from JMeter
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[JMeter Report Generator] {}", line);
                    outputBuilder.append(line).append("\n");
                }
            }
            
            // Log errors from JMeter
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.error("[JMeter Report Generator] {}", line);
                    errorBuilder.append(line).append("\n");
                }
            }
            
            // Wait for the process to complete with a generous timeout
            boolean completed = process.waitFor(90, TimeUnit.SECONDS);
            int exitCode = process.exitValue();
            
            // Handle successful report generation
            if (completed && exitCode == 0) {
                // Copy all the report files to maintain the full JMeter dashboard structure
                copyJMeterReportFiles(timestampedOutputPath, outputPath);
                
                // Also create a timestamped index.html in the parent directory for easier access
                try {
                    Path indexFile = timestampedOutputPath.resolve("index.html");
                    if (Files.exists(indexFile)) {
                        // Use the same run directory name for consistency
                        Path timestampedIndexFile = outputPath.resolve("index_" + runDirectoryName + ".html");
                        
                        // Copy the index file with a timestamp
                        Files.copy(indexFile, timestampedIndexFile, StandardCopyOption.REPLACE_EXISTING);
                        
                        // Also update the main index.html
                        Path parentIndexFile = outputPath.resolve("index.html");
                        if (Files.exists(parentIndexFile)) {
                            Files.delete(parentIndexFile);
                        }
                        Files.copy(indexFile, parentIndexFile);
                        
                        logger.info("Copied JMeter dashboard to parent directory for easy access");
                    } else {
                        logger.warn("JMeter did not generate an index.html file in the report directory");
                    }
                } catch (IOException e) {
                    logger.warn("Could not copy JMeter dashboard to parent directory: {}", e.getMessage());
                }
                
                logger.info("Successfully generated JMeter HTML dashboard at: {}", timestampedOutputDir);
                return true;
            } else {
                // If JMeter fails, let's generate our own report with the error details
                String errorOutput = errorBuilder.toString();
                
                // If JMeter failed because the folder is not empty, create our own report
                if (errorOutput.contains("folder is not empty") || errorOutput.contains("Cannot write to folder")) {
                    logger.warn("JMeter couldn't generate the report due to non-empty folder. Generating fallback report.");
                    return generateFallbackHtmlReport(jtlFilePath, outputDirectory, 
                            "JMeter couldn't generate the report: " + errorOutput, 
                            outputBuilder.toString());
                }
                
                logger.error("Failed to generate HTML report. JMeter exited with code: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error generating HTML report using JMeter", e);
            // Generate a fallback report with the error details
            return generateFallbackHtmlReport(jtlFilePath, outputDirectory, 
                    "Error generating JMeter report: " + e.getMessage(), 
                    "Exception stack trace: " + stackTraceToString(e));
        }
    }
    
    /**
     * Helper method to convert an exception's stack trace to a string
     */
    private static String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Helper method to delete a directory recursively
     */
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
    
    /**
     * Copy necessary JMeter report files from the timestamped directory to the main output directory 
     * so they can be accessed directly
     */
    private static void copyJMeterReportFiles(Path source, Path destination) throws IOException {
        // Only copy index.html and essential folders to the parent directory
        Path indexFile = source.resolve("index.html");
        if (Files.exists(indexFile)) {
            Path destIndexFile = destination.resolve("index.html");
            Files.copy(indexFile, destIndexFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Copy the content folder which has CSS, JS, etc.
            Path contentFolder = source.resolve("content");
            if (Files.exists(contentFolder)) {
                Path destContentFolder = destination.resolve("content");
                copyDirectory(contentFolder, destContentFolder);
            }
            
            // Copy the sbadmin2 folder which has the dashboard templates
            Path sbadminFolder = source.resolve("sbadmin2");
            if (Files.exists(sbadminFolder)) {
                Path destSbadminFolder = destination.resolve("sbadmin2");
                copyDirectory(sbadminFolder, destSbadminFolder);
            }
        }
    }
    
    /**
     * Helper method to copy a directory recursively
     */
    private static void copyDirectory(Path source, Path destination) throws IOException {
        if (!Files.exists(destination)) {
            Files.createDirectories(destination);
        }
        
        try (java.util.stream.Stream<Path> stream = Files.list(source)) {
            stream.forEach(sourceFile -> {
                Path destFile = destination.resolve(source.relativize(sourceFile));
                try {
                    if (Files.isDirectory(sourceFile)) {
                        copyDirectory(sourceFile, destFile);
                    } else {
                        Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    // Log and continue, don't stop for a single file error
                    logger.warn("Error copying file {}: {}", sourceFile, e.getMessage());
                }
            });
        }
    }
    
    /**
     * Generate a fallback HTML report when JMeter is not available
     * This creates a simple HTML report with basic test information
     *
     * @param jtlFilePath Path to the JTL file
     * @param outputDirectory Directory where the HTML report will be saved
     * @return true if report generation was successful
     */
    private static boolean generateEnhancedHtmlReport(String jtlFilePath, String outputDirectory) {
        try {
            Path outputPath = Paths.get(outputDirectory);
            Path jtlPath = Paths.get(jtlFilePath);
            
            // Use the JMeterLogConfiguration to get consistent naming with logs
            String runDirectoryName = JMeterLogConfiguration.getRunDirectoryName();
            
            // Ensure the output directory exists
            Files.createDirectories(outputPath);
            
            // Read the JTL file contents
            String jtlContents = Files.exists(jtlPath) && !Files.isDirectory(jtlPath) ? 
                    Files.readString(jtlPath) : "No JTL file found.";
            
            // Create a comprehensive HTML report similar to JMeter's standard dashboard
            StringBuilder htmlReport = new StringBuilder();
            htmlReport.append("<!DOCTYPE html>\n");
            htmlReport.append("<html lang=\"en\">\n");
            htmlReport.append("<head>\n");
            htmlReport.append("    <meta charset=\"UTF-8\">\n");
            htmlReport.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            htmlReport.append("    <title>Performance Test Dashboard</title>\n");
            htmlReport.append("    <!-- Include Chart.js for data visualization -->\n");
            htmlReport.append("    <script src=\"https://cdn.jsdelivr.net/npm/chart.js@3.7.1/dist/chart.min.js\"></script>\n");
            htmlReport.append("    <!-- Include Bootstrap for styling -->\n");
            htmlReport.append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n");
            htmlReport.append("    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js\"></script>\n");
            htmlReport.append("    <!-- Font Awesome for icons -->\n");
            htmlReport.append("    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.1.1/css/all.min.css\">\n");
            htmlReport.append("    <style>\n");
            htmlReport.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; color: #333; background-color: #f7f9fc; }\n");
            htmlReport.append("        .dashboard-header { background-color: #2c3e50; color: white; padding: 20px 0; margin-bottom: 20px; }\n");
            htmlReport.append("        .dashboard-title { font-weight: 300; }\n");
            htmlReport.append("        .card { border: none; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); margin-bottom: 20px; transition: all 0.3s ease; }\n");
            htmlReport.append("        .card:hover { transform: translateY(-5px); box-shadow: 0 6px 12px rgba(0,0,0,0.15); }\n");
            htmlReport.append("        .card-header { background-color: #f8f9fa; border-bottom: 1px solid #eee; font-weight: 600; }\n");
            htmlReport.append("        .metric-card { text-align: center; padding: 20px; }\n");
            htmlReport.append("        .metric-value { font-size: 2rem; font-weight: 700; margin: 10px 0; }\n");
            htmlReport.append("        .metric-label { font-size: 1rem; color: #6c757d; }\n");
            htmlReport.append("        .chart-container { position: relative; height: 300px; margin-bottom: 20px; }\n");
            htmlReport.append("        .success { color: #28a745; }\n");
            htmlReport.append("        .error { color: #dc3545; }\n");
            htmlReport.append("        .warning { color: #ffc107; }\n");
            htmlReport.append("        .info { color: #17a2b8; }\n");
            htmlReport.append("        .table-responsive { border-radius: 10px; overflow: hidden; }\n");
            htmlReport.append("        .table-striped tbody tr:nth-of-type(odd) { background-color: rgba(0,0,0,.02); }\n");
            htmlReport.append("        .table th { font-weight: 600; }\n");
            htmlReport.append("        .summary-item { display: inline-block; margin-right: 20px; margin-bottom: 10px; }\n");
            htmlReport.append("        .summary-label { font-weight: 600; margin-right: 5px; }\n");
            htmlReport.append("        .tab-content { padding: 20px 0; }\n");
            htmlReport.append("        .badge { font-weight: 500; }\n");
            htmlReport.append("    </style>\n");
            htmlReport.append("</head>\n");
            htmlReport.append("<body>\n");
            htmlReport.append("    <div class=\"dashboard-header\">\n");
            htmlReport.append("        <div class=\"container\">\n");
            htmlReport.append("            <h1 class=\"dashboard-title\"><i class=\"fas fa-tachometer-alt\"></i> Performance Test Dashboard</h1>\n");
            htmlReport.append("            <p class=\"text-white-50\">Generated on " + java.time.LocalDateTime.now() + "</p>\n");
            htmlReport.append("        </div>\n");
            htmlReport.append("    </div>\n");
            htmlReport.append("    <div class=\"container\">\n");
            
            // Parse and analyze JTL data if available
            if (jtlContents.startsWith("timeStamp,elapsed")) {
                String[] lines = jtlContents.split("\n");
                String[] headers = lines[0].split(",");
                
                // Variables to hold statistics
                int totalSamples = 0;
                int successCount = 0;
                int errorCount = 0;
                long totalElapsed = 0;
                long minElapsed = Long.MAX_VALUE;
                long maxElapsed = 0;
                
                // Arrays to hold data for charts
                java.util.List<String> labels = new java.util.ArrayList<>();
                java.util.List<Long> responseTimeData = new java.util.ArrayList<>();
                java.util.List<String> successData = new java.util.ArrayList<>();
                
                // Find indices of relevant columns
                int timeStampIdx = indexOfHeader(headers, "timeStamp");
                int elapsedIdx = indexOfHeader(headers, "elapsed");
                int labelIdx = indexOfHeader(headers, "label");
                int successIdx = indexOfHeader(headers, "success");
                int responseCodeIdx = indexOfHeader(headers, "responseCode");
                int responseMessageIdx = indexOfHeader(headers, "responseMessage");
                int urlIdx = indexOfHeader(headers, "URL");
                
                // Process each line
                for (int i = 1; i < lines.length; i++) {
                    if (!lines[i].trim().isEmpty()) {
                        totalSamples++;
                        String[] values = parseCSVLine(lines[i]);
                        
                        // Skip if not enough values (should never happen with properly formed JTL)
                        // Include URL index in maximum calculation if it exists
                        int maxIndex = Math.max(
                            Math.max(Math.max(elapsedIdx, successIdx), responseCodeIdx),
                            urlIdx >= 0 ? urlIdx : -1
                        );
                        if (values.length <= maxIndex) {
                            continue;
                        }
                        
                        // Add data for charts
                        if (labelIdx >= 0 && labelIdx < values.length) {
                        }
                        
                        // Add data for charts
                        if (labelIdx >= 0 && labelIdx < values.length) {
                            labels.add(values[labelIdx]);
                        } else {
                            labels.add("Sample " + i);
                        }
                        
                        // Calculate response time statistics
                        if (elapsedIdx >= 0 && elapsedIdx < values.length) {
                            long elapsed = tryParseLong(values[elapsedIdx], 0);
                            totalElapsed += elapsed;
                            minElapsed = Math.min(minElapsed, elapsed);
                            maxElapsed = Math.max(maxElapsed, elapsed);
                            responseTimeData.add(elapsed);
                        } else {
                            responseTimeData.add(0L);
                        }
                        
                        // Count successes and errors
                        if (successIdx >= 0 && successIdx < values.length) {
                            if ("true".equals(values[successIdx])) {
                                successCount++;
                                successData.add("Success");
                            } else {
                                errorCount++;
                                successData.add("Error");
                            }
                        }
                    }
                }
                
                // Calculate averages
                double avgResponseTime = totalSamples > 0 ? (double) totalElapsed / totalSamples : 0;
                double errorRate = totalSamples > 0 ? (double) errorCount / totalSamples * 100 : 0;
                
                // Output summary metrics
                htmlReport.append("        <!-- Summary Metrics -->\n");
                htmlReport.append("        <div class=\"row mb-4\">\n");
                htmlReport.append("            <div class=\"col-md-3\">\n");
                htmlReport.append("                <div class=\"card metric-card\">\n");
                htmlReport.append("                    <div class=\"metric-value\">" + totalSamples + "</div>\n");
                htmlReport.append("                    <div class=\"metric-label\">Total Samples</div>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("            <div class=\"col-md-3\">\n");
                htmlReport.append("                <div class=\"card metric-card\">\n");
                htmlReport.append("                    <div class=\"metric-value " + (errorRate > 0 ? "error" : "success") + "\">" 
                                  + String.format("%.1f%%", errorRate) + "</div>\n");
                htmlReport.append("                    <div class=\"metric-label\">Error Rate</div>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("            <div class=\"col-md-3\">\n");
                htmlReport.append("                <div class=\"card metric-card\">\n");
                htmlReport.append("                    <div class=\"metric-value\">" + String.format("%.2f", avgResponseTime) + " ms</div>\n");
                htmlReport.append("                    <div class=\"metric-label\">Average Response Time</div>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("            <div class=\"col-md-3\">\n");
                htmlReport.append("                <div class=\"card metric-card\">\n");
                htmlReport.append("                    <div class=\"metric-value\">" + maxElapsed + " ms</div>\n");
                htmlReport.append("                    <div class=\"metric-label\">Max Response Time</div>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("        </div>\n");
                
                // Output file info and test summary
                htmlReport.append("        <!-- Test Information -->\n");
                htmlReport.append("        <div class=\"card mb-4\">\n");
                htmlReport.append("            <div class=\"card-header\">\n");
                htmlReport.append("                <i class=\"fas fa-info-circle\"></i> Test Information\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("            <div class=\"card-body\">\n");
                htmlReport.append("                <div class=\"row\">\n");
                htmlReport.append("                    <div class=\"col-md-6\">\n");
                htmlReport.append("                        <div class=\"summary-item\"><span class=\"summary-label\">JTL File:</span> " + jtlPath + "</div><br>\n");
                htmlReport.append("                        <div class=\"summary-item\"><span class=\"summary-label\">Test ID:</span> " + runDirectoryName + "</div><br>\n");
                htmlReport.append("                        <div class=\"summary-item\"><span class=\"summary-label\">Generated At:</span> " + java.time.LocalDateTime.now() + "</div>\n");
                htmlReport.append("                    </div>\n");
                htmlReport.append("                    <div class=\"col-md-6\">\n");
                htmlReport.append("                        <div class=\"summary-item\"><span class=\"summary-label\">Total Samples:</span> " + totalSamples + "</div><br>\n");
                htmlReport.append("                        <div class=\"summary-item\"><span class=\"summary-label\">Successful Requests:</span> <span class=\"success\">" + successCount + "</span></div><br>\n");
                htmlReport.append("                        <div class=\"summary-item\"><span class=\"summary-label\">Failed Requests:</span> <span class=\"error\">" + errorCount + "</span></div>\n");
                htmlReport.append("                    </div>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("        </div>\n");
                
                // Response time chart
                htmlReport.append("        <!-- Response Time Chart -->\n");
                htmlReport.append("        <div class=\"card mb-4\">\n");
                htmlReport.append("            <div class=\"card-header\">\n");
                htmlReport.append("                <i class=\"fas fa-chart-line\"></i> Response Times\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("            <div class=\"card-body\">\n");
                htmlReport.append("                <div class=\"chart-container\">\n");
                htmlReport.append("                    <canvas id=\"responseTimeChart\"></canvas>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("        </div>\n");
                
                // Success/Failure pie chart
                htmlReport.append("        <!-- Success/Failure Chart -->\n");
                htmlReport.append("        <div class=\"row mb-4\">\n");
                htmlReport.append("            <div class=\"col-md-6\">\n");
                htmlReport.append("                <div class=\"card h-100\">\n");
                htmlReport.append("                    <div class=\"card-header\">\n");
                htmlReport.append("                        <i class=\"fas fa-chart-pie\"></i> Success vs Failure\n");
                htmlReport.append("                    </div>\n");
                htmlReport.append("                    <div class=\"card-body\">\n");
                htmlReport.append("                        <div class=\"chart-container\" style=\"height: 250px;\">\n");
                htmlReport.append("                            <canvas id=\"successRateChart\"></canvas>\n");
                htmlReport.append("                        </div>\n");
                htmlReport.append("                    </div>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("            <div class=\"col-md-6\">\n");
                htmlReport.append("                <div class=\"card h-100\">\n");
                htmlReport.append("                    <div class=\"card-header\">\n");
                htmlReport.append("                        <i class=\"fas fa-chart-bar\"></i> Response Time Statistics\n");
                htmlReport.append("                    </div>\n");
                htmlReport.append("                    <div class=\"card-body\">\n");
                htmlReport.append("                        <table class=\"table table-bordered\">\n");
                htmlReport.append("                            <tbody>\n");
                htmlReport.append("                                <tr>\n");
                htmlReport.append("                                    <th>Minimum</th>\n");
                htmlReport.append("                                    <td>" + (minElapsed == Long.MAX_VALUE ? 0 : minElapsed) + " ms</td>\n");
                htmlReport.append("                                </tr>\n");
                htmlReport.append("                                <tr>\n");
                htmlReport.append("                                    <th>Maximum</th>\n");
                htmlReport.append("                                    <td>" + maxElapsed + " ms</td>\n");
                htmlReport.append("                                </tr>\n");
                htmlReport.append("                                <tr>\n");
                htmlReport.append("                                    <th>Average</th>\n");
                htmlReport.append("                                    <td>" + String.format("%.2f", avgResponseTime) + " ms</td>\n");
                htmlReport.append("                                </tr>\n");
                htmlReport.append("                                <tr>\n");
                htmlReport.append("                                    <th>Total Time</th>\n");
                htmlReport.append("                                    <td>" + totalElapsed + " ms</td>\n");
                htmlReport.append("                                </tr>\n");
                htmlReport.append("                            </tbody>\n");
                htmlReport.append("                        </table>\n");
                htmlReport.append("                    </div>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("        </div>\n");
                
                // Full data table
                htmlReport.append("        <!-- Detailed Results -->\n");
                htmlReport.append("        <div class=\"card mb-4\">\n");
                htmlReport.append("            <div class=\"card-header\">\n");
                htmlReport.append("                <i class=\"fas fa-table\"></i> Detailed Results\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("            <div class=\"card-body\">\n");
                htmlReport.append("                <div class=\"table-responsive\">\n");
                htmlReport.append("                    <table class=\"table table-striped table-bordered\">\n");
                htmlReport.append("                        <thead class=\"table-dark\">\n");
                htmlReport.append("                            <tr>\n");
                
                // Generate table header, using friendly column names
                for (String header : headers) {
                    String displayHeader = friendlyColumnName(header);
                    htmlReport.append("                                <th>").append(displayHeader).append("</th>\n");
                }
                htmlReport.append("                            </tr>\n");
                htmlReport.append("                        </thead>\n");
                htmlReport.append("                        <tbody>\n");
                
                // Generate table rows
                for (int i = 1; i < lines.length; i++) {
                    if (!lines[i].trim().isEmpty()) {
                        htmlReport.append("                            <tr>\n");
                        
                        // Split the CSV line, handling quoted values
                        String[] values = parseCSVLine(lines[i]);
                        
                        for (int j = 0; j < Math.min(values.length, headers.length); j++) {
                            String value = values[j];
                            
                            // Apply special formatting for certain columns
                            if (headers[j].equals("success")) {
                                String badgeClass = "true".equals(value) ? "bg-success" : "bg-danger";
                                String displayValue = "true".equals(value) ? "Success" : "Failure";
                                htmlReport.append("                                <td><span class=\"badge ").append(badgeClass).append("\">")
                                         .append(displayValue).append("</span></td>\n");
                            } else if (headers[j].equals("responseCode")) {
                                String badgeClass = value.startsWith("2") ? "bg-success" : 
                                                  value.startsWith("3") ? "bg-info" :
                                                  value.startsWith("4") ? "bg-warning" : 
                                                  value.startsWith("5") ? "bg-danger" : "bg-secondary";
                                htmlReport.append("                                <td><span class=\"badge ").append(badgeClass).append("\">")
                                         .append(value).append("</span></td>\n");
                            } else if (headers[j].equals("elapsed")) {
                                // Colorize response times
                                long elapsed = tryParseLong(value, 0);
                                String textClass = elapsed < 500 ? "success" : 
                                                 elapsed < 1000 ? "warning" : "error";
                                htmlReport.append("                                <td class=\"").append(textClass).append("\">")
                                         .append(value).append(" ms</td>\n");
                            } else {
                                htmlReport.append("                                <td>").append(value).append("</td>\n");
                            }
                        }
                        
                        // Fill in missing values
                        for (int j = values.length; j < headers.length; j++) {
                            htmlReport.append("                                <td></td>\n");
                        }
                        
                        htmlReport.append("                            </tr>\n");
                    }
                }
                
                htmlReport.append("                        </tbody>\n");
                htmlReport.append("                    </table>\n");
                htmlReport.append("                </div>\n");
                htmlReport.append("            </div>\n");
                htmlReport.append("        </div>\n");
                
                // JavaScript for charts
                htmlReport.append("        <!-- Chart Scripts -->\n");
                htmlReport.append("        <script>\n");
                htmlReport.append("            document.addEventListener('DOMContentLoaded', function() {\n");
                
                // Response time chart
                htmlReport.append("                // Response Time Chart\n");
                htmlReport.append("                const rtCtx = document.getElementById('responseTimeChart').getContext('2d');\n");
                htmlReport.append("                new Chart(rtCtx, {\n");
                htmlReport.append("                    type: 'line',\n");
                htmlReport.append("                    data: {\n");
                htmlReport.append("                        labels: " + convertListToJsArray(truncateLabels(labels)) + ",\n");
                htmlReport.append("                        datasets: [{\n");
                htmlReport.append("                            label: 'Response Time (ms)',\n");
                htmlReport.append("                            data: " + convertListToJsArray(responseTimeData) + ",\n");
                htmlReport.append("                            borderColor: 'rgba(54, 162, 235, 1)',\n");
                htmlReport.append("                            backgroundColor: 'rgba(54, 162, 235, 0.2)',\n");
                htmlReport.append("                            borderWidth: 2,\n");
                htmlReport.append("                            pointRadius: 1,\n");
                htmlReport.append("                            pointHoverRadius: 5,\n");
                htmlReport.append("                            tension: 0.3\n");
                htmlReport.append("                        }]\n");
                htmlReport.append("                    },\n");
                htmlReport.append("                    options: {\n");
                htmlReport.append("                        responsive: true,\n");
                htmlReport.append("                        maintainAspectRatio: false,\n");
                htmlReport.append("                        scales: {\n");
                htmlReport.append("                            y: {\n");
                htmlReport.append("                                beginAtZero: true,\n");
                htmlReport.append("                                title: {\n");
                htmlReport.append("                                    display: true,\n");
                htmlReport.append("                                    text: 'Response Time (ms)'\n");
                htmlReport.append("                                }\n");
                htmlReport.append("                            },\n");
                htmlReport.append("                            x: {\n");
                htmlReport.append("                                title: {\n");
                htmlReport.append("                                    display: true,\n");
                htmlReport.append("                                    text: 'Samples'\n");
                htmlReport.append("                                },\n");
                htmlReport.append("                                ticks: {\n");
                htmlReport.append("                                    maxRotation: 0,\n");
                htmlReport.append("                                    autoSkip: true,\n");
                htmlReport.append("                                    maxTicksLimit: 20\n");
                htmlReport.append("                                }\n");
                htmlReport.append("                            }\n");
                htmlReport.append("                        },\n");
                htmlReport.append("                        plugins: {\n");
                htmlReport.append("                            tooltip: {\n");
                htmlReport.append("                                callbacks: {\n");
                htmlReport.append("                                    title: function(tooltipItems) {\n");
                htmlReport.append("                                        return 'Sample ' + tooltipItems[0].dataIndex;\n");
                htmlReport.append("                                    }\n");
                htmlReport.append("                                }\n");
                htmlReport.append("                            }\n");
                htmlReport.append("                        }\n");
                htmlReport.append("                    }\n");
                htmlReport.append("                });\n");
                
                // Success rate pie chart
                htmlReport.append("                // Success Rate Pie Chart\n");
                htmlReport.append("                const srCtx = document.getElementById('successRateChart').getContext('2d');\n");
                htmlReport.append("                new Chart(srCtx, {\n");
                htmlReport.append("                    type: 'pie',\n");
                htmlReport.append("                    data: {\n");
                htmlReport.append("                        labels: ['Success', 'Error'],\n");
                htmlReport.append("                        datasets: [{\n");
                htmlReport.append("                            data: [" + successCount + ", " + errorCount + "],\n");
                htmlReport.append("                            backgroundColor: [\n");
                htmlReport.append("                                'rgba(40, 167, 69, 0.8)',\n");
                htmlReport.append("                                'rgba(220, 53, 69, 0.8)'\n");
                htmlReport.append("                            ],\n");
                htmlReport.append("                            borderColor: [\n");
                htmlReport.append("                                'rgba(40, 167, 69, 1)',\n");
                htmlReport.append("                                'rgba(220, 53, 69, 1)'\n");
                htmlReport.append("                            ],\n");
                htmlReport.append("                            borderWidth: 1\n");
                htmlReport.append("                        }]\n");
                htmlReport.append("                    },\n");
                htmlReport.append("                    options: {\n");
                htmlReport.append("                        responsive: true,\n");
                htmlReport.append("                        maintainAspectRatio: false,\n");
                htmlReport.append("                        plugins: {\n");
                htmlReport.append("                            tooltip: {\n");
                htmlReport.append("                                callbacks: {\n");
                htmlReport.append("                                    label: function(context) {\n");
                htmlReport.append("                                        let label = context.label || '';\n");
                htmlReport.append("                                        if (label) label += ': ';\n");
                htmlReport.append("                                        const value = context.raw;\n");
                htmlReport.append("                                        const total = " + totalSamples + ";\n");
                htmlReport.append("                                        const percentage = Math.round((value / total) * 100);\n");
                htmlReport.append("                                        return label + value + ' (' + percentage + '%)';\n");
                htmlReport.append("                                    }\n");
                htmlReport.append("                                }\n");
                htmlReport.append("                            },\n");
                htmlReport.append("                            legend: {\n");
                htmlReport.append("                                position: 'right'\n");
                htmlReport.append("                            }\n");
                htmlReport.append("                        }\n");
                htmlReport.append("                    }\n");
                htmlReport.append("                });\n");
                
                htmlReport.append("            });\n");
                htmlReport.append("        </script>\n");
            } else {
                // No valid JTL data - show a message
                htmlReport.append("        <div class=\"alert alert-warning mb-4\">\n");
                htmlReport.append("            <h4><i class=\"fas fa-exclamation-triangle\"></i> No Valid Test Data Found</h4>\n");
                htmlReport.append("            <p>The JTL file either does not exist or does not contain valid test data.</p>\n");
                htmlReport.append("            <p>File path: " + jtlPath + "</p>\n");
                htmlReport.append("        </div>\n");
                
                // Show raw JTL data if available
                if (!jtlContents.equals("No JTL file found.")) {
                    htmlReport.append("        <div class=\"card mb-4\">\n");
                    htmlReport.append("            <div class=\"card-header\">\n");
                    htmlReport.append("                <i class=\"fas fa-file-alt\"></i> Raw JTL Data\n");
                    htmlReport.append("            </div>\n");
                    htmlReport.append("            <div class=\"card-body\">\n");
                    htmlReport.append("                <pre>" + jtlContents + "</pre>\n");
                    htmlReport.append("            </div>\n");
                    htmlReport.append("        </div>\n");
                }
            }
            
            // Footer
            htmlReport.append("        <footer class=\"mt-5 text-center text-muted\">\n");
            htmlReport.append("            <p>Generated by Performance Automation Framework | " + java.time.LocalDate.now().getYear() + "</p>\n");
            htmlReport.append("        </footer>\n");
            htmlReport.append("    </div>\n");
            htmlReport.append("</body>\n");
            htmlReport.append("</html>\n");
            
            // Write the HTML report to file, using a consistent name pattern
            Path reportFile = outputPath.resolve("report_" + runDirectoryName + ".html");
            Files.writeString(reportFile, htmlReport.toString());
            
            // Also create index.html for easier access
            Path indexFile = outputPath.resolve("index.html");
            Files.writeString(indexFile, htmlReport.toString());
            
            logger.info("Generated enhanced HTML report at: {}", reportFile);
            return true;
        } catch (Exception e) {
            logger.error("Error generating enhanced HTML report", e);
            return false;
        }
    }
    
    /**
     * Helper method to convert a list to a JavaScript array
     */
    private static String convertListToJsArray(java.util.List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object item = list.get(i);
            if (item instanceof String) {
                sb.append("'").append(((String) item).replace("'", "\\'")).append("'");
            } else {
                sb.append(item);
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Helper method to truncate long labels for charts
     */
    private static java.util.List<String> truncateLabels(java.util.List<String> labels) {
        java.util.List<String> truncated = new java.util.ArrayList<>();
        for (String label : labels) {
            if (label.length() > 20) {
                truncated.add(label.substring(0, 17) + "...");
            } else {
                truncated.add(label);
            }
        }
        return truncated;
    }
    
    /**
     * Helper method to get index of a header in the array
     */
    private static int indexOfHeader(String[] headers, String headerName) {
        for (int i = 0; i < headers.length; i++) {
            if (headerName.equals(headers[i])) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Helper method to convert a string to a long with a default value
     */
    private static long tryParseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper method to provide friendly column names
     */
    private static String friendlyColumnName(String columnName) {
        switch (columnName) {
            case "timeStamp": return "Timestamp";
            case "elapsed": return "Response Time (ms)";
            case "label": return "Label";
            case "responseCode": return "Status Code";
            case "responseMessage": return "Status Message";
            case "threadName": return "Thread";
            case "dataType": return "Data Type";
            case "success": return "Status";
            case "failureMessage": return "Error Message";
            case "bytes": return "Bytes Received";
            case "sentBytes": return "Bytes Sent";
            case "grpThreads": return "Group Threads";
            case "allThreads": return "All Threads";
            case "URL": return "URL";
            case "Latency": return "Latency (ms)";
            case "IdleTime": return "Idle Time (ms)";
            case "Connect": return "Connect Time (ms)";
            default: return columnName;
        }
    }
    
    /**
     * Generate a simple fallback HTML report with error details
     *
     * @param jtlFilePath Path to the JTL file
     * @param outputDirectory Directory where the HTML report will be saved
     * @param errorMessage Error message to include in the report
     * @param details Additional details to include in the report
     * @return true if report generation was successful
     */
    private static boolean generateFallbackHtmlReport(String jtlFilePath, String outputDirectory, 
                                                    String errorMessage, String details) {
        try {
            Path outputPath = Paths.get(outputDirectory);
            Files.createDirectories(outputPath);
            
            // Use the JMeterLogConfiguration to get consistent naming with logs
            String runDirectoryName = JMeterLogConfiguration.getRunDirectoryName();
            
            // Create a simple HTML report with error details
            StringBuilder htmlReport = new StringBuilder();
            htmlReport.append("<!DOCTYPE html>\n");
            htmlReport.append("<html lang=\"en\">\n");
            htmlReport.append("<head>\n");
            htmlReport.append("    <meta charset=\"UTF-8\">\n");
            htmlReport.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            htmlReport.append("    <title>JMeter Test Report - Error</title>\n");
            htmlReport.append("    <style>\n");
            htmlReport.append("        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; color: #333; }\n");
            htmlReport.append("        .container { max-width: 1200px; margin: 0 auto; }\n");
            htmlReport.append("        h1 { color: #2c3e50; }\n");
            htmlReport.append("        h2 { color: #e74c3c; margin-top: 30px; }\n");
            htmlReport.append("        pre { background-color: #f8f9fa; padding: 15px; border-radius: 5px; overflow-x: auto; }\n");
            htmlReport.append("        .error { background-color: #fee; padding: 15px; border-radius: 5px; border-left: 5px solid #e74c3c; margin-bottom: 20px; }\n");
            htmlReport.append("    </style>\n");
            htmlReport.append("</head>\n");
            htmlReport.append("<body>\n");
            htmlReport.append("    <div class=\"container\">\n");
            htmlReport.append("        <h1>JMeter Test Report</h1>\n");
            htmlReport.append("        <div class=\"error\">\n");
            htmlReport.append("            <h2>Error Generating Report</h2>\n");
            htmlReport.append("            <p>").append(errorMessage).append("</p>\n");
            htmlReport.append("        </div>\n");
            htmlReport.append("        <h2>Details</h2>\n");
            htmlReport.append("        <pre>").append(details).append("</pre>\n");
            htmlReport.append("        <h2>JTL File Path</h2>\n");
            htmlReport.append("        <pre>").append(jtlFilePath).append("</pre>\n");
            htmlReport.append("    </div>\n");
            htmlReport.append("</body>\n");
            htmlReport.append("</html>\n");
            
            // Write the HTML report to file, using a consistent name pattern
            Path reportFile = outputPath.resolve("error_report_" + runDirectoryName + ".html");
            Files.writeString(reportFile, htmlReport.toString());
            
            // Also create index.html for easier access
            Path indexFile = outputPath.resolve("index.html");
            Files.writeString(indexFile, htmlReport.toString());
            
            logger.info("Generated fallback HTML report with error details at: {}", reportFile);
            return true;
        } catch (Exception e) {
            logger.error("Error generating fallback HTML report", e);
            return false;
        }
    }
    
    /**
     * Parse a CSV line, handling quoted values correctly
     *
     * @param line The CSV line to parse
     * @return Array of values from the CSV line
     */
    private static String[] parseCSVLine(String line) {
        // This is a simplified CSV parser for JTL files
        // It handles quoted values but not escaped quotes within quotes
        if (line == null || line.isEmpty()) {
            return new String[0];
        }
        
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        
        // Add the last token
        tokens.add(sb.toString());
        
        return tokens.toArray(new String[0]);
    }
}
