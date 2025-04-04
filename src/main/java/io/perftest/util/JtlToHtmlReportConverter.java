package io.perftest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            logger.info("JMeter is not available, generating simple HTML report");
            return generateSimpleHtmlReport(actualJtlPath.toString(), outputDirectory);
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
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "jmeter",
                    "-g", jtlFilePath,
                    "-o", outputDirectory
            );
            
            Process process = processBuilder.start();
            
            // Log the output from JMeter
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[JMeter Report Generator] {}", line);
                }
            }
            
            // Log errors from JMeter
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.error("[JMeter Report Generator] {}", line);
                }
            }
            
            boolean completed = process.waitFor(60, TimeUnit.SECONDS);
            int exitCode = process.exitValue();
            
            if (completed && exitCode == 0) {
                logger.info("Successfully generated HTML report at: {}", outputDirectory);
                return true;
            } else {
                logger.error("Failed to generate HTML report. JMeter exited with code: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error generating HTML report using JMeter", e);
            return false;
        }
    }
    
    /**
     * Generate a simple HTML report from JTL file
     * This is a fallback when JMeter is not available
     *
     * @param jtlFilePath Path to the JTL file
     * @param outputDirectory Directory where the HTML report will be saved
     * @return true if report generation was successful
     */
    private static boolean generateSimpleHtmlReport(String jtlFilePath, String outputDirectory) {
        try {
            // Read JTL file content (CSV or XML)
            String jtlContent = Files.readString(Paths.get(jtlFilePath));
            boolean isXml = jtlContent.trim().startsWith("<?xml");
            
            // Generate a very simple HTML report
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>\n")
                    .append("<html lang=\"en\">\n")
                    .append("<head>\n")
                    .append("    <meta charset=\"UTF-8\">\n")
                    .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                    .append("    <title>Performance Test Results</title>\n")
                    .append("    <style>\n")
                    .append("        body { font-family: Arial, sans-serif; margin: 20px; }\n")
                    .append("        h1 { color: #2c3e50; }\n")
                    .append("        .summary { background-color: #f8f9fa; padding: 15px; border-radius: 5px; }\n")
                    .append("        pre { background-color: #f1f1f1; padding: 10px; overflow-x: auto; }\n")
                    .append("    </style>\n")
                    .append("</head>\n")
                    .append("<body>\n")
                    .append("    <h1>Performance Test Results</h1>\n")
                    .append("    <div class=\"summary\">\n")
                    .append("        <h2>Test Summary</h2>\n")
                    .append("        <p>This is a simple HTML report generated from the JTL file.</p>\n")
                    .append("        <p>For more detailed reports, run JMeter and use its reporting capabilities.</p>\n")
                    .append("    </div>\n")
                    .append("    <h2>Raw JTL Data</h2>\n")
                    .append("    <pre>").append(jtlContent).append("</pre>\n")
                    .append("</body>\n")
                    .append("</html>");
            
            // Write HTML file
            Files.writeString(Paths.get(outputDirectory, "index.html"), htmlBuilder.toString());
            
            logger.info("Generated simple HTML report at: {}/index.html", outputDirectory);
            return true;
        } catch (IOException e) {
            logger.error("Error generating simple HTML report", e);
            return false;
        }
    }
}
