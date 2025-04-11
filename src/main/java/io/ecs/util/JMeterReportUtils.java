package io.ecs.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for JMeter report operations
 * 
 * This class provides utility methods for working with JMeter JTL files and reports: - Finding JTL
 * files in a directory - Generating JMeter reports from JTL files - Creating JMeter properties
 * files for report configuration
 */
public class JMeterReportUtils {

    private static final Logger logger = LoggerFactory.getLogger(JMeterReportUtils.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    /**
     * Find all JTL files in a directory and its subdirectories
     * 
     * @param directory The directory to search in
     * @return A list of paths to JTL files
     * @throws IOException If an error occurs during directory traversal
     */
    public static List<Path> findJtlFiles(String directory) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".jtl")).collect(Collectors.toList());
        }
    }

    /**
     * Generate JMeter HTML reports from all JTL files in a directory
     * 
     * @param sourceDirectory The directory containing JTL files
     * @param outputDirectory The directory where reports should be generated
     * @return The number of reports successfully generated
     */
    public static int generateReportsFromDirectory(String sourceDirectory, String outputDirectory) {
        int successCount = 0;

        try {
            List<Path> jtlFiles = findJtlFiles(sourceDirectory);
            logger.info("Found {} JTL files in {}", jtlFiles.size(), sourceDirectory);

            // Create output directory if it doesn't exist
            new File(outputDirectory).mkdirs();

            // Generate a report for each JTL file
            for (Path jtlFile : jtlFiles) {
                String reportDir = outputDirectory + "/"
                        + jtlFile.getFileName().toString().replace(".jtl", "");

                logger.info("Generating report for {} in {}", jtlFile, reportDir);
                boolean success = generateJMeterReport(jtlFile.toString(), reportDir);

                if (success) {
                    successCount++;
                }
            }

            logger.info("Generated {} reports successfully", successCount);

        } catch (IOException e) {
            logger.error("Error searching for JTL files: {}", e.getMessage());
        }

        return successCount;
    }

    /**
     * Generate a JMeter HTML report from a JTL file
     * 
     * @param jtlFilePath The path to the JTL file
     * @param reportDirectory The directory where the report should be generated
     * @return true if the report was generated successfully, false otherwise
     */
    public static boolean generateJMeterReport(String jtlFilePath, String reportDirectory) {
        // Ensure report directory exists
        File reportDir = new File(reportDirectory);
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }

        try {
            // Check if JMeter is available
            Process process = Runtime.getRuntime().exec("jmeter -v");
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logger.warn("JMeter not found in PATH. Report generation skipped.");
                return false;
            }

            // Create JMeter properties file for report configuration
            String propertiesFile = createJMeterProperties(reportDirectory);

            // Create JMeter command
            String jmeterCommand = "jmeter -g " + jtlFilePath + " -o " + reportDirectory + " -p "
                    + propertiesFile + " -j jmeter-report.log";

            logger.info("Executing JMeter report command: {}", jmeterCommand);

            // Execute command
            process = Runtime.getRuntime().exec(jmeterCommand);
            boolean completed = process.waitFor(60, TimeUnit.SECONDS); // Wait up to 60 seconds for
                                                                       // completion

            if (completed && process.exitValue() == 0) {
                logger.info("JMeter report generated successfully at: {}", reportDirectory);
                return true;
            } else {
                logger.error("JMeter report generation failed with exit code: {}",
                        process.exitValue());
                return false;
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error generating JMeter report: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Create a JMeter properties file for report configuration
     * 
     * @param directory The directory where to create the properties file
     * @return The path to the created properties file
     * @throws IOException If an error occurs during file creation
     */
    private static String createJMeterProperties(String directory) throws IOException {
        String propertiesFile = directory + "/jmeter.properties";

        try (FileWriter writer = new FileWriter(propertiesFile)) {
            writer.write("# JMeter Properties for Report Generation\n");
            writer.write("jmeter.reportgenerator.overall_granularity=1000\n");
            writer.write(
                    "jmeter.reportgenerator.graph.activeThreadsOverTime.exclude_controllers=true\n");
            writer.write(
                    "jmeter.reportgenerator.exporter.html.series_filter=^(success|failure).*\n");
            writer.write("jmeter.reportgenerator.exporter.html.filters_only_sample_series=true\n");
            writer.write(
                    "jmeter.reportgenerator.graph.custom_mm_response_time.exclude_controllers=true\n");
            writer.write(
                    "jmeter.reportgenerator.graph.custom_mm_response_time.property_values=avg,min,max\n");
        }

        return propertiesFile;
    }

    /**
     * Command line interface for JMeter report generation
     * 
     * Usage: java -cp <classpath> io.ecs.util.JMeterReportUtils <source-dir> <output-dir>
     * 
     * @param args Command line arguments: args[0] - Source directory containing JTL files args[1] -
     *        Output directory for reports (optional)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: JMeterReportUtils <source-dir> [<output-dir>]");
            System.exit(1);
        }

        String sourceDir = args[0];
        String outputDir = args.length > 1 ? args[1]
                : "target/reports/jmeter-reports-" + DATE_FORMAT.format(new Date());

        System.out.println("Generating JMeter reports from JTL files in: " + sourceDir);
        System.out.println("Output directory: " + outputDir);

        int reportCount = generateReportsFromDirectory(sourceDir, outputDir);

        System.out.println("Generated " + reportCount + " reports successfully");
    }
}

