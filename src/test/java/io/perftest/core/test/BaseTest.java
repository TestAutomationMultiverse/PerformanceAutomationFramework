package io.perftest.core.test;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.perftest.engine.TestEngine;
import io.perftest.systems.TestSystem;
import io.perftest.util.JtlToHtmlReportConverter;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

/**
 * Base test class with common utility methods for all test types.
 */
public abstract class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    // Keep track of the last protocol used
    private String lastProtocolUsed = "default";

    // Keep track of the last report directory
    private Path lastReportDirectory;

    // Keep track of the last report file
    private Path lastReportFile;

    /**
     * Configure the test engine from YAML configuration.
     *
     * @param testSystem The test system with registered components
     * @param config The configuration map loaded from YAML
     * @return Configured TestEngine instance
     */
    protected TestEngine configureTestEngine(TestSystem testSystem, Map<String, Object> config) {
        Map<String, Object> executionConfig =
                (Map<String, Object>) config.getOrDefault("execution", Map.of());

        int threads = (Integer) executionConfig.getOrDefault("threads", 1);
        int iterations = (Integer) executionConfig.getOrDefault("iterations", 1);
        int rampUpSeconds = (Integer) executionConfig.getOrDefault("rampUpSeconds", 0);
        int holdSeconds = (Integer) executionConfig.getOrDefault("holdSeconds", 0);

        // Determine protocol from the config file name or content if possible
        String protocol = determineProtocol(config);

        TestEngine engine = new TestEngine(testSystem);
        engine.setThreads(threads);
        engine.setIterations(iterations);
        engine.setRampUp(java.time.Duration.ofSeconds(rampUpSeconds));

        // Set protocol name for reporting
        if (protocol != null && !protocol.isEmpty()) {
            engine.setProtocolName(protocol);
            lastProtocolUsed = protocol;
        }

        return engine;
    }

    /**
     * Set the protocol name for reporting This helps with keeping track of which protocol was used
     *
     * @param protocolName The protocol name
     */
    protected void setLastProtocolUsed(String protocolName) {
        if (protocolName != null && !protocolName.isEmpty()) {
            this.lastProtocolUsed = protocolName;
        }
    }

    /**
     * Get the last used protocol name
     * 
     * @return The protocol name
     */
    public String getLastProtocolUsed() {
        return lastProtocolUsed;
    }

    /**
     * Get the last report directory
     * 
     * @return Path to the last report directory
     */
    public Path getLastReportDirectory() {
        return lastReportDirectory;
    }

    /**
     * Get the last report file
     * 
     * @return Path to the last report file
     */
    public Path getLastReportFile() {
        return lastReportFile;
    }

    /**
     * Determine the protocol from the configuration
     *
     * @param config The configuration map loaded from YAML
     * @return Protocol name
     */
    protected String determineProtocol(Map<String, Object> config) {
        // First, check if protocol is explicitly specified
        if (config.containsKey("protocol")) {
            return (String) config.get("protocol");
        }

        // Try to infer protocol from the configuration structure
        if (config.containsKey("request")) {
            Map<String, Object> request = (Map<String, Object>) config.get("request");

            if (request.containsKey("graphql") || request.containsKey("graphqlQuery")) {
                return "graphql";
            } else if (request.containsKey("soap") || request.containsKey("soapAction")) {
                return "soap";
            } else if (request.containsKey("jdbc") || request.containsKey("query")) {
                return "jdbc";
            } else if (request.containsKey("method") || request.containsKey("endpoint")) {
                return "http";
            }
        }

        // Default to a generic protocol name
        return "api";
    }

    /**
     * Log test results and statistics
     *
     * @param stats TestPlanStats from the test run
     */
    protected void logTestResults(TestPlanStats stats) {
        logger.info("Test completed successfully!");
        logger.info("Total samples: {}", stats.overall().samplesCount());
        logger.info("Average response time: {} ms", stats.overall().sampleTime().mean());
        logger.info("Median response time: {} ms", stats.overall().sampleTime().median());
        logger.info("90th percentile response time: {} ms", stats.overall().sampleTime().perc90());
        logger.info("Error rate: {}%",
                stats.overall().errorsCount() * 100.0 / stats.overall().samplesCount());
        // Calculate throughput manually
        double durationSeconds =
                stats.duration().getSeconds() + stats.duration().getNano() / 1_000_000_000.0;
        double throughput =
                durationSeconds > 0 ? stats.overall().samplesCount() / durationSeconds : 0;
        logger.info("Throughput: {} requests/sec", String.format("%.2f", throughput));

        // Generate HTML report from JTL file
        generateHtmlReport();

        // Print a summary report that includes the report URL
        logger.info("\n{}", createReportSummary(stats));
    }

    /**
     * Generate HTML report from JTL file
     * 
     * @return Path to the generated HTML report file
     */
    protected Path generateHtmlReport() {
        try {
            // Define paths for JTL file and HTML report directory
            Path jtlFile =
                    Paths.get("target", "jtl-results", lastProtocolUsed + "-test-results.jtl");
            Path htmlReportDir =
                    Paths.get("target", "html-reports", lastProtocolUsed).toAbsolutePath();

            // Store the report directory for later reference
            lastReportDirectory = htmlReportDir;

            logger.info("Generating HTML report for protocol '{}' from JTL file: {}",
                    lastProtocolUsed, jtlFile);

            // Generate HTML report
            boolean success = JtlToHtmlReportConverter.generateHtmlReport(jtlFile.toString(),
                    htmlReportDir.toString());

            if (success) {
                logger.info("HTML report generated successfully at: {}", htmlReportDir);

                // Find the latest report file by timestamp pattern
                Path reportFile = findLatestReportFile(htmlReportDir);
                if (reportFile != null) {
                    lastReportFile = reportFile;
                    logger.info("Latest report file: {}", reportFile);
                    return reportFile;
                } else {
                    // If no timestamped report is found, look for index.html
                    Path indexFile = htmlReportDir.resolve("index.html");
                    if (Files.exists(indexFile)) {
                        lastReportFile = indexFile;
                        return indexFile;
                    }
                }
            } else {
                logger.warn("Failed to generate HTML report");
            }
        } catch (Exception e) {
            logger.error("Error generating HTML report", e);
        }
        return null;
    }

    /**
     * Find the latest report file in the given directory Looks for files matching the pattern
     * index_timestamp.html
     * 
     * @param reportDir Directory containing HTML reports
     * @return Path to the latest report file, or null if none found
     */
    private Path findLatestReportFile(Path reportDir) {
        try {
            if (!Files.exists(reportDir) || !Files.isDirectory(reportDir)) {
                return null;
            }

            List<Path> timestampedFiles = new ArrayList<>();

            // Find all files matching the pattern index_timestamp.html
            Files.list(reportDir)
                    .filter(p -> p.getFileName().toString().matches("index_\\d+\\.html"))
                    .forEach(timestampedFiles::add);

            if (timestampedFiles.isEmpty()) {
                return null;
            }

            // Sort by filename (which contains timestamp) in descending order to get latest
            Collections.sort(timestampedFiles, (p1, p2) -> {
                String name1 = p1.getFileName().toString();
                String name2 = p2.getFileName().toString();
                return name2.compareTo(name1);
            });

            return timestampedFiles.get(0);
        } catch (IOException e) {
            logger.error("Error finding latest report file", e);
            return null;
        }
    }

    /**
     * Get all report directories
     * 
     * @return List of report directories
     */
    public List<Path> getAllReportDirectories() {
        try {
            Path baseReportDir = Paths.get("target", "html-reports").toAbsolutePath();
            if (!Files.exists(baseReportDir) || !Files.isDirectory(baseReportDir)) {
                return Collections.emptyList();
            }

            return Files.list(baseReportDir).filter(Files::isDirectory)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error getting report directories", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get all report files for a specific protocol
     * 
     * @param protocol Protocol name
     * @return List of report files
     */
    public List<Path> getReportFilesForProtocol(String protocol) {
        try {
            Path protocolReportDir = Paths.get("target", "html-reports", protocol).toAbsolutePath();
            if (!Files.exists(protocolReportDir) || !Files.isDirectory(protocolReportDir)) {
                return Collections.emptyList();
            }

            return Files.list(protocolReportDir)
                    .filter(p -> p.getFileName().toString().matches("index.*\\.html"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error getting report files for protocol: {}", protocol, e);
            return Collections.emptyList();
        }
    }

    /**
     * Open the latest report in a browser This method attempts to open the default browser to view
     * the report
     * 
     * @return true if the browser was opened successfully
     */
    public boolean openReportInBrowser() {
        if (lastReportFile != null && Files.exists(lastReportFile)) {
            try {
                // Check if Desktop is supported on this platform
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(lastReportFile.toUri());
                        logger.info("Opened report in browser: {}", lastReportFile);
                        return true;
                    }
                }

                // Fallback to system-specific commands
                String osName = System.getProperty("os.name").toLowerCase();

                if (osName.contains("windows")) {
                    new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler",
                            lastReportFile.toString()).start();
                } else if (osName.contains("mac")) {
                    new ProcessBuilder("open", lastReportFile.toString()).start();
                } else if (osName.contains("nix") || osName.contains("nux")) {
                    // Try common Linux browsers
                    try {
                        new ProcessBuilder("xdg-open", lastReportFile.toString()).start();
                    } catch (IOException e) {
                        // If xdg-open fails, try with specific browsers
                        String[] browsers = {"firefox", "google-chrome", "chromium-browser"};
                        boolean opened = false;
                        for (String browser : browsers) {
                            try {
                                new ProcessBuilder(browser, lastReportFile.toString()).start();
                                opened = true;
                                break;
                            } catch (IOException ignored) {
                                // Try next browser
                            }
                        }
                        if (!opened) {
                            throw new IOException("No browser found to open report");
                        }
                    }
                }

                logger.info("Attempted to open report in browser: {}", lastReportFile);
                return true;
            } catch (Exception e) {
                logger.error("Failed to open report in browser: {}", e.getMessage());
                return false;
            }
        } else {
            logger.warn("No report file available to open");
            return false;
        }
    }

    /**
     * Get URL for the latest report This can be used to display the URL in the console for manual
     * access
     * 
     * @return String URL for the latest report, or null if none available
     */
    public String getReportUrl() {
        if (lastReportFile != null && Files.exists(lastReportFile)) {
            return lastReportFile.toUri().toString();
        }
        return null;
    }

    /**
     * Create a report summary that can be displayed in the console or UI This provides a quick
     * overview of the test results
     * 
     * @param stats TestPlanStats from the test run
     * @return String summary of the test results
     */
    public String createReportSummary(TestPlanStats stats) {
        if (stats == null) {
            return "No test statistics available.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("\n============== PERFORMANCE TEST SUMMARY ==============\n");
        summary.append(String.format("Protocol: %s\n", lastProtocolUsed));
        summary.append(String.format("Timestamp: %s\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        summary.append(String.format("Total Samples: %d\n", stats.overall().samplesCount()));
        summary.append(String.format("Duration: %d seconds\n", stats.duration().getSeconds()));
        summary.append(String.format("Average Response Time: %s ms\n",
                stats.overall().sampleTime().mean()));
        summary.append(String.format("Median Response Time: %s ms\n",
                stats.overall().sampleTime().median()));
        summary.append(
                String.format("90th Percentile: %s ms\n", stats.overall().sampleTime().perc90()));
        summary.append(String.format("Error Rate: %.2f%%\n",
                stats.overall().errorsCount() * 100.0 / stats.overall().samplesCount()));

        // Calculate throughput
        double durationSeconds =
                stats.duration().getSeconds() + stats.duration().getNano() / 1_000_000_000.0;
        double throughput =
                durationSeconds > 0 ? stats.overall().samplesCount() / durationSeconds : 0;
        summary.append(String.format("Throughput: %.2f requests/sec\n", throughput));

        // Add report location
        if (lastReportFile != null) {
            summary.append(String.format("Report: %s\n", lastReportFile));
        }

        summary.append("===================================================\n");

        return summary.toString();
    }
}
