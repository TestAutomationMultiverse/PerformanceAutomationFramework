package io.perftest.demo;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.HttpRequestEntity;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

/**
 * Simple demonstration of HTML report generation capabilities.
 * 
 * <p>
 * This test creates a simple HTTP request and executes it using the TestEngine, which now generates
 * both JTL and HTML reports.
 * </p>
 */
public class HtmlReportDemoTest {
    private static final Logger logger = LoggerFactory.getLogger(HtmlReportDemoTest.class);

    /**
     * Main method to run the demo.
     * 
     * @param args Command line arguments (not used)
     * @throws IOException If an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        logger.info("Starting HTML Report Demo Test");

        // Create a test engine with default settings
        TestEngine engine = new TestEngine();

        // Configure the test with method chaining
        engine.setThreads(5).setIterations(10).setRampUp(Duration.ofSeconds(5))
                .setProtocolName("http_demo").setTestPlanName("HTML Report Demo Test");

        // Create a simple HTTP request
        HttpRequestEntity request = new HttpRequestEntity("https://postman-echo.com/get");
        request.setName("Example Request");
        request.setMethod("GET");

        // Add query parameters as URL parameters instead of using setQueryParams
        String url = request.getUrl() + "?test=html_report_demo";
        request.setUrl(url);

        // Add a header
        request.addHeader("Accept", "application/json");

        // Add the request to the engine
        engine.addRequest(request);

        try {
            // Run the test
            logger.info("Executing test...");
            TestPlanStats stats = engine.runTest();

            // Log test statistics
            logger.info("Test completed successfully");
            logger.info("Total requests: {}", stats.overall().samplesCount());
            logger.info("Average response time: {} ms", stats.overall().sampleTime().mean());
            logger.info("Error count: {}", stats.overall().errorsCount());

            // Get report paths
            Path jtlPath = engine.getResultsPath("http_demo");
            Path htmlPath = engine.getHtmlReportPath("http_demo");

            // Log report locations
            logger.info("JTL results available at: {}", jtlPath);
            logger.info("HTML reports available at: {}", htmlPath);
            logger.info("Open {}/index.html in a browser to view the HTML report", htmlPath);
        } catch (Exception e) {
            logger.error("Test execution failed", e);
        }
    }
}
