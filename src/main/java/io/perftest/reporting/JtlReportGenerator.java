package io.perftest.reporting;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * JTL to HTML Report Generator.
 * This class provides a command-line utility to generate HTML reports from JTL files
 * using the JMeter HTML Reporter.
 * 
 * Updated to work with the unified reporting structure that stores JTL files, HTML reports,
 * and logs in a consistent directory structure:
 * target/unified-reports/protocol_timestamp_uniqueId/
 *   - jtl files
 *   - html/ (contains HTML reports)
 *   - logs/ (contains log files)
 */
public class JtlReportGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JtlReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    // Base directories for unified reporting structure
    private static final String UNIFIED_REPORTS_BASE = "target/unified-reports";
    
    /**
     * Main method to execute the report generator.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption("h")) {
                printHelp(options);
                return;
            }
            
            String inputFile = cmd.getOptionValue("input");
            String outputDir = cmd.getOptionValue("output");
            
            if (inputFile == null || outputDir == null) {
                System.err.println("Both input and output parameters are required.");
                printHelp(options);
                return;
            }
            
            generateReport(inputFile, outputDir);
            
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            printHelp(options);
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create command line options.
     *
     * @return Options object
     */
    private static Options createOptions() {
        Options options = new Options();
        
        Option input = Option.builder("i")
                .longOpt("input")
                .hasArg()
                .desc("Input JTL file path")
                .required()
                .build();
        
        Option output = Option.builder("o")
                .longOpt("output")
                .hasArg()
                .desc("Output directory for HTML report")
                .required()
                .build();
        
        Option help = Option.builder("h")
                .longOpt("help")
                .desc("Print help message")
                .build();
        
        options.addOption(input);
        options.addOption(output);
        options.addOption(help);
        
        return options;
    }

    /**
     * Print help message.
     *
     * @param options Command line options
     */
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("JtlReportGenerator", 
                "Generate HTML report from JTL file", 
                options, 
                "Example: java -cp target/classes io.perftest.reporting.JtlReportGenerator --input=results.jtl --output=html-report", 
                true);
    }

    /**
     * Generate HTML report from JTL file.
     * This method supports both the legacy structure and the new unified structure.
     *
     * @param jtlFilePath  JTL file path
     * @param outputDirPath Output directory path (can be an existing unified report directory)
     * @throws IOException if there's an error reading or writing files
     */
    public static void generateReport(String jtlFilePath, String outputDirPath) throws IOException {
        File jtlFile = new File(jtlFilePath);
        File outputDir = new File(outputDirPath);
        
        if (!jtlFile.exists() || !jtlFile.isFile()) {
            throw new IOException("JTL file does not exist or is not a file: " + jtlFilePath);
        }
        
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                throw new IOException("Could not create output directory: " + outputDirPath);
            }
        }
        
        LOGGER.info("Generating HTML report from JTL file: {}", jtlFilePath);
        LOGGER.info("Output directory: {}", outputDirPath);
        
        try {
            // Extract metadata from the filename or parent directory
            String protocol = extractProtocol(jtlFile);
            String uniqueId = extractUniqueId(jtlFile);
            
            // Determine if we're using the unified directory structure
            boolean isUnifiedStructure = isUnifiedReportDirectory(outputDir);
            
            // Set up the HTML output directory
            File htmlOutputDir;
            if (isUnifiedStructure) {
                // Use the unified structure's html subdirectory
                htmlOutputDir = new File(outputDir, "html");
                if (!htmlOutputDir.exists()) {
                    htmlOutputDir.mkdirs();
                }
            } else {
                // Use the provided output directory directly
                htmlOutputDir = outputDir;
            }
            
            // Create a properties file for the report configuration
            Path propertiesPath = createReportProperties(htmlOutputDir, protocol);
            
            // Generate the HTML report
            createHtmlReport(htmlOutputDir, protocol, jtlFile);
            
            // Create a summary file with links to all the different report components
            if (isUnifiedStructure) {
                createReportSummary(outputDir, jtlFile);
            }
            
            LOGGER.info("HTML report generated successfully at: {}", htmlOutputDir);
            LOGGER.info("To view the report, open: {}/index.html", htmlOutputDir);
            
        } catch (Exception e) {
            LOGGER.error("Error generating HTML report", e);
            throw new IOException("Failed to generate HTML report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract protocol from the JTL file path.
     *
     * @param jtlFile JTL file
     * @return Protocol name
     */
    private static String extractProtocol(File jtlFile) {
        // Try to extract protocol from the filename first
        String filename = jtlFile.getName();
        String[] parts = filename.split("_");
        if (parts.length >= 1) {
            return parts[0];
        }
        
        // If that fails, try to extract from parent directory (unified structure)
        File parentDir = jtlFile.getParentFile();
        if (parentDir != null) {
            String dirName = parentDir.getName();
            parts = dirName.split("_");
            if (parts.length >= 1) {
                return parts[0];
            }
        }
        
        // Default
        return "unknown";
    }
    
    /**
     * Extract unique ID from the JTL file path (if available in the unified structure).
     *
     * @param jtlFile JTL file
     * @return Unique ID or a new random ID
     */
    private static String extractUniqueId(File jtlFile) {
        // Try to extract from parent directory (unified structure)
        File parentDir = jtlFile.getParentFile();
        if (parentDir != null) {
            String dirName = parentDir.getName();
            String[] parts = dirName.split("_");
            if (parts.length >= 3) {
                return parts[2];
            }
        }
        
        // Generate a new unique ID if not found
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Check if the given directory is a unified report directory.
     *
     * @param dir Directory to check
     * @return true if it's a unified report directory
     */
    private static boolean isUnifiedReportDirectory(File dir) {
        // Check if this is a direct unified report directory 
        // (e.g., target/unified-reports/protocol_timestamp_uniqueId)
        if (dir.getAbsolutePath().contains(UNIFIED_REPORTS_BASE) && 
            dir.getName().matches("^[a-zA-Z0-9\\-]+_\\d{8}_\\d{6}_[a-zA-Z0-9]+$")) {
            return true;
        }
        
        // Check if this directory has the pattern of protocol_timestamp_uniqueId
        return dir.getName().matches("^[a-zA-Z0-9\\-]+_\\d{8}_\\d{6}_[a-zA-Z0-9]+$");
    }

    /**
     * Create a properties file for configuring the HTML report.
     *
     * @param outputDir Output directory
     * @param protocol  Protocol name extracted from JTL filename
     * @return Path to the properties file
     * @throws IOException if there's an error writing the file
     */
    private static Path createReportProperties(File outputDir, String protocol) throws IOException {
        Path propertiesPath = Paths.get(outputDir.getAbsolutePath(), "report.properties");
        
        String currentDateTime = LocalDateTime.now().format(DATE_FORMATTER);
        
        String properties = String.join("\n", Arrays.asList(
                "jmeter.reportgenerator.report_title=Performance Test Report - " + protocol.toUpperCase(),
                "jmeter.reportgenerator.overall_granularity=1000",
                "jmeter.reportgenerator.graph.custom_metrics.prefix=custom_",
                "jmeter.reportgenerator.graph.activeThreads.exclude_controllers=true",
                "jmeter.reportgenerator.graph.bytes.exclude_controllers=true",
                "jmeter.reportgenerator.graph.custom_percentiles.enabled=true",
                "jmeter.reportgenerator.graph.custom_percentiles.percentiles=90;95;99",
                "jmeter.reportgenerator.exporter.html.series_filter=^(protocol=" + protocol + ").*$",
                "jmeter.reportgenerator.date_format=yyyy-MM-dd HH:mm:ss",
                "jmeter.reportgenerator.exporter.html.show_empty_charts=false",
                "jmeter.reportgenerator.report_name=" + protocol + "_report_" + currentDateTime,
                "jmeter.reportgenerator.sample_filter=INCLUDE",
                "jmeter.reportgenerator.sample_filter.include_pattern=.*",
                "jmeter.reportgenerator.statistic_window=200000",
                "jmeter.reportgenerator.temp_dir=" + outputDir.getAbsolutePath() + "/temp",
                "jmeter.reportgenerator.apdex_per_transaction=true",
                "jmeter.reportgenerator.apdex_satisfied_threshold=500",
                "jmeter.reportgenerator.apdex_tolerated_threshold=1500"
        ));
        
        Files.write(propertiesPath, properties.getBytes());
        return propertiesPath;
    }
    
    /**
     * Create an HTML report from JTL file.
     * In a real implementation, this would use JMeter's HTML reporter.
     *
     * @param outputDir Output directory
     * @param protocol  Protocol name
     * @param jtlFile   JTL file
     * @throws IOException if there's an error writing files
     */
    private static void createHtmlReport(File outputDir, String protocol, File jtlFile) throws IOException {
        // Create index.html
        Path indexPath = Paths.get(outputDir.getAbsolutePath(), "index.html");
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String filename = jtlFile.getName();
        
        String htmlContent = String.join("\n", Arrays.asList(
                "<!DOCTYPE html>",
                "<html>",
                "<head>",
                "    <meta charset=\"UTF-8\">",
                "    <title>Performance Test Report - " + protocol.toUpperCase() + "</title>",
                "    <style>",
                "        body { font-family: Arial, sans-serif; margin: 20px; }",
                "        h1 { color: #333366; }",
                "        .info { margin: 20px 0; }",
                "        .info div { margin: 10px 0; }",
                "        .label { font-weight: bold; display: inline-block; width: 150px; }",
                "        .note { color: #666; font-style: italic; margin-top: 30px; }",
                "    </style>",
                "</head>",
                "<body>",
                "    <h1>Performance Test Report - " + protocol.toUpperCase() + "</h1>",
                "    <div class=\"info\">",
                "        <div><span class=\"label\">Protocol:</span> " + protocol + "</div>",
                "        <div><span class=\"label\">JTL File:</span> " + filename + "</div>",
                "        <div><span class=\"label\">Generated:</span> " + timestamp + "</div>",
                "    </div>",
                "    <div class=\"note\">",
                "        Note: This is a placeholder for JMeter's HTML reporter output. In a production environment, ",
                "        this would be replaced with the actual JMeter HTML report with graphs and detailed statistics.",
                "    </div>",
                "</body>",
                "</html>"
        ));
        
        Files.write(indexPath, htmlContent.getBytes());
        
        // Create a css directory and stylesheet
        File cssDir = new File(outputDir, "css");
        cssDir.mkdir();
        
        Path cssPath = Paths.get(cssDir.getAbsolutePath(), "style.css");
        String cssContent = "/* Placeholder for JMeter HTML report CSS */";
        Files.write(cssPath, cssContent.getBytes());
        
        // Create a js directory and script
        File jsDir = new File(outputDir, "js");
        jsDir.mkdir();
        
        Path jsPath = Paths.get(jsDir.getAbsolutePath(), "dashboard.js");
        String jsContent = "// Placeholder for JMeter HTML report JavaScript";
        Files.write(jsPath, jsContent.getBytes());
    }
    
    /**
     * Create a summary file with links to all the different report components.
     * This is used for the unified report structure to provide an overview.
     *
     * @param unifiedDir Unified report directory
     * @param jtlFile JTL file
     * @throws IOException if there's an error writing files
     */
    private static void createReportSummary(File unifiedDir, File jtlFile) throws IOException {
        Path summaryPath = Paths.get(unifiedDir.getAbsolutePath(), "summary.html");
        
        // Find the HTML report directory
        File htmlDir = new File(unifiedDir, "html");
        boolean htmlExists = htmlDir.exists() && htmlDir.isDirectory();
        
        // Find the logs directory
        File logsDir = new File(unifiedDir, "logs");
        boolean logsExist = logsDir.exists() && logsDir.isDirectory();
        
        // Get the directory name (which has protocol, timestamp, and uniqueId)
        String dirName = unifiedDir.getName();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        StringBuilder fileLinks = new StringBuilder();
        
        // Add JTL file link
        fileLinks.append("    <div class=\"section\">\n");
        fileLinks.append("        <h2>JTL Files</h2>\n");
        fileLinks.append("        <ul>\n");
        fileLinks.append("            <li><a href=\"").append(jtlFile.getName()).append("\">")
                .append(jtlFile.getName()).append("</a></li>\n");
        fileLinks.append("        </ul>\n");
        fileLinks.append("    </div>\n");
        
        // Add HTML report link
        fileLinks.append("    <div class=\"section\">\n");
        fileLinks.append("        <h2>HTML Reports</h2>\n");
        if (htmlExists) {
            fileLinks.append("        <ul>\n");
            fileLinks.append("            <li><a href=\"html/index.html\">HTML Report</a></li>\n");
            fileLinks.append("        </ul>\n");
        } else {
            fileLinks.append("        <p class=\"not-available\">No HTML reports available</p>\n");
        }
        fileLinks.append("    </div>\n");
        
        // Add log files link
        fileLinks.append("    <div class=\"section\">\n");
        fileLinks.append("        <h2>Log Files</h2>\n");
        if (logsExist) {
            fileLinks.append("        <ul>\n");
            File[] logFiles = logsDir.listFiles((dir, name) -> name.endsWith(".log"));
            if (logFiles != null && logFiles.length > 0) {
                for (File logFile : logFiles) {
                    fileLinks.append("            <li><a href=\"logs/").append(logFile.getName()).append("\">")
                            .append(logFile.getName()).append("</a></li>\n");
                }
            } else {
                fileLinks.append("            <li>No log files found</li>\n");
            }
            fileLinks.append("        </ul>\n");
        } else {
            fileLinks.append("        <p class=\"not-available\">No log files available</p>\n");
        }
        fileLinks.append("    </div>\n");
        
        String htmlContent = String.join("\n", Arrays.asList(
                "<!DOCTYPE html>",
                "<html>",
                "<head>",
                "    <meta charset=\"UTF-8\">",
                "    <title>Unified Test Report Summary</title>",
                "    <style>",
                "        body { font-family: Arial, sans-serif; margin: 20px; }",
                "        h1 { color: #333366; }",
                "        h2 { color: #336699; margin-top: 20px; }",
                "        .info { margin: 20px 0; padding: 10px; background-color: #f0f0f0; border-radius: 5px; }",
                "        .info div { margin: 10px 0; }",
                "        .label { font-weight: bold; display: inline-block; width: 150px; }",
                "        .section { margin: 20px 0; padding: 10px; background-color: #f8f8f8; border-radius: 5px; }",
                "        .not-available { color: #999; font-style: italic; }",
                "        ul { margin-top: 5px; }",
                "    </style>",
                "</head>",
                "<body>",
                "    <h1>Unified Test Report Summary</h1>",
                "    <div class=\"info\">",
                "        <div><span class=\"label\">Test Directory:</span> " + dirName + "</div>",
                "        <div><span class=\"label\">Generated:</span> " + timestamp + "</div>",
                "    </div>",
                fileLinks.toString(),
                "</body>",
                "</html>"
        ));
        
        Files.write(summaryPath, htmlContent.getBytes());
    }
}
