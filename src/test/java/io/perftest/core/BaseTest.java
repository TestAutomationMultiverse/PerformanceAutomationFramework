package io.perftest.core;

import io.perftest.engine.TestEngine;
import io.perftest.systems.TestSystem;
import io.perftest.util.JtlToHtmlReportConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Base test class with common utility methods for all test types.
 */
public abstract class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    
    // Keep track of the last protocol used
    private String lastProtocolUsed = "default";

    /**
     * Configure the test engine from YAML configuration.
     *
     * @param testSystem The test system with registered components
     * @param config The configuration map loaded from YAML
     * @return Configured TestEngine instance
     */
    protected TestEngine configureTestEngine(TestSystem testSystem, Map<String, Object> config) {
        Map<String, Object> executionConfig = (Map<String, Object>) config.getOrDefault("execution", Map.of());
        
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
     * Set the protocol name for reporting
     * This helps with keeping track of which protocol was used
     *
     * @param protocolName The protocol name
     */
    protected void setLastProtocolUsed(String protocolName) {
        if (protocolName != null && !protocolName.isEmpty()) {
            this.lastProtocolUsed = protocolName;
        }
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
        logger.info("Error rate: {}%", stats.overall().errorsCount() * 100.0 / stats.overall().samplesCount());
        // Calculate throughput manually
        double durationSeconds = stats.duration().getSeconds() + stats.duration().getNano() / 1_000_000_000.0;
        double throughput = durationSeconds > 0 ? stats.overall().samplesCount() / durationSeconds : 0;
        logger.info("Throughput: {} requests/sec", String.format("%.2f", throughput));
        
        // Generate HTML report from JTL file
        generateHtmlReport();
    }
    
    /**
     * Generate HTML report from JTL file
     */
    protected void generateHtmlReport() {
        try {
            // Define paths for JTL file and HTML report directory
            Path jtlFile = Paths.get("target", "jtl-results", lastProtocolUsed + "-test-results.jtl");
            Path htmlReportDir = Paths.get("target", "html-reports", lastProtocolUsed).toAbsolutePath();
            
            logger.info("Generating HTML report for protocol '{}' from JTL file: {}", lastProtocolUsed, jtlFile);
            
            // Generate HTML report
            boolean success = JtlToHtmlReportConverter.generateHtmlReport(
                    jtlFile.toString(), 
                    htmlReportDir.toString()
            );
            
            if (success) {
                logger.info("HTML report generated successfully at: {}", htmlReportDir);
            } else {
                logger.warn("Failed to generate HTML report");
            }
        } catch (Exception e) {
            logger.error("Error generating HTML report", e);
        }
    }
}
