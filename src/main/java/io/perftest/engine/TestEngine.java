package io.perftest.engine;

import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.perftest.components.graphql.GraphQLComponent;
import io.perftest.components.http.HttpComponent;
import io.perftest.components.jdbc.JdbcComponent;
import io.perftest.components.soap.SoapComponent;
import io.perftest.components.xml.XmlComponent;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.JdbcRequestEntity;
import io.perftest.entities.request.RequestEntity;
import io.perftest.entities.request.SoapRequestEntity;
import io.perftest.entities.request.XmlRequestEntity;
import io.perftest.exception.ErrorCode;
import io.perftest.exception.ErrorHandler;
import io.perftest.exception.Result;
import io.perftest.exception.TestExecutionException;
import io.perftest.systems.TestSystem;
import io.perftest.util.JMeterLogConfiguration;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * The TestEngine is the main orchestrator for performance test execution.
 * 
 * <p>
 * This class provides a high-level API for configuring and executing performance tests using the
 * JMeter DSL. It serves as the integration point between the ECS architecture and the underlying
 * JMeter test execution engine.
 * </p>
 * 
 * <p>
 * TestEngine is responsible for:
 * </p>
 * <ul>
 * <li>Managing test configuration (threads, iterations, ramp-up time)</li>
 * <li>Registering components for different protocol handlers</li>
 * <li>Coordinating the request processing pipeline</li>
 * <li>Executing the test and collecting results</li>
 * <li>Managing test result files and directories</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * TestEngine engine = new TestEngine();
 * engine.setThreads(10).setIterations(100).setRampUp(Duration.ofSeconds(30));
 * 
 * HttpRequestEntity request =
 *         new HttpRequestEntity().setUrl("https://example.com/api").setMethod("GET");
 * 
 * engine.addRequest(request);
 * TestPlanStats stats = engine.runTest();
 * }</pre>
 * 
 * <p>
 * Updated to use the unified reporting structure that stores JTL files, HTML reports, and logs in a
 * consistent directory structure: target/unified-reports/protocol_timestamp_uniqueId/ - jtl files -
 * html/ (contains HTML reports) - logs/ (contains log files)
 * </p>
 */
public class TestEngine {
    private static final Logger logger = LoggerFactory.getLogger(TestEngine.class);

    private final TestSystem testSystem;
    private final List<RequestEntity> requests;
    private final Map<String, Path> resultPaths;
    private final Map<String, Path> htmlReportPaths;
    private final Map<String, Path> unifiedReportPaths;

    // Base directory for unified reporting structure
    private static final String UNIFIED_REPORTS_BASE = "target/unified-reports";

    private int threads = 1;
    private int iterations = 1;
    private Duration rampUp = Duration.ofSeconds(0);
    private boolean initialized = false;
    private String protocolName = "default";
    private String testPlanName = "Test Plan";

    // Modifiers for different request types
    private Consumer<HttpRequestEntity> httpModifier;
    private Consumer<GraphQLRequestEntity> graphqlModifier;
    private Consumer<XmlRequestEntity> xmlModifier;
    private Consumer<SoapRequestEntity> soapModifier;
    private Consumer<JdbcRequestEntity> jdbcModifier;

    /**
     * Creates a new TestEngine instance with default TestSystem.
     * 
     * <p>
     * The constructor initializes a TestSystem and registers default components for HTTP, GraphQL,
     * XML, SOAP, and JDBC protocols.
     * </p>
     */
    public TestEngine() {
        this(new TestSystem());
    }

    /**
     * Creates a new TestEngine instance with a provided TestSystem.
     * 
     * <p>
     * This constructor allows for dependency injection of a TestSystem instance. It will reuse the
     * provided TestSystem without registering additional components.
     * </p>
     * 
     * <p>
     * This constructor creates a new TestSystem internally to avoid exposing internal
     * representation. Components from the provided TestSystem are not copied - we simply use it to
     * check if we need to register default components.
     * </p>
     * 
     * @param testSystem The TestSystem to use
     */
    public TestEngine(TestSystem testSystem) {
        ErrorHandler.validateNotNull(testSystem, ErrorCode.GENERAL_ERROR,
                "TestSystem cannot be null");

        // Create a new TestSystem to avoid storing the externally provided one directly
        this.testSystem = new TestSystem();
        this.requests = new ArrayList<>();
        this.resultPaths = new HashMap<>();
        this.htmlReportPaths = new HashMap<>();
        this.unifiedReportPaths = new HashMap<>();

        // Register default components if original TestSystem is empty
        if (testSystem.getComponentCount() == 0) {
            registerDefaultComponents();
        } else {
            // Since we can't easily copy components from the provided TestSystem,
            // we register default components in our new instance
            registerDefaultComponents();
            logger.warn(
                    "Created new TestSystem instance with default components. Custom components in provided TestSystem are not copied.");
        }
    }

    /**
     * Registers the default components for various protocols.
     * 
     * <p>
     * This method is called during TestEngine initialization to set up the standard protocol
     * handlers. It registers components for HTTP, GraphQL, XML, SOAP, and JDBC protocols.
     * </p>
     */
    private void registerDefaultComponents() {
        // Register HTTP component
        testSystem.addComponent(HttpRequestEntity.class, new HttpComponent());

        // Register GraphQL component
        testSystem.addComponent(GraphQLRequestEntity.class, new GraphQLComponent());

        // Register XML component
        testSystem.addComponent(XmlRequestEntity.class, new XmlComponent());

        // Register SOAP component
        testSystem.addComponent(SoapRequestEntity.class, new SoapComponent());

        // Register JDBC component
        testSystem.addComponent(JdbcRequestEntity.class, new JdbcComponent());

        initialized = true;
    }

    /**
     * Set the number of threads (virtual users) for the test.
     * 
     * <p>
     * This sets the number of concurrent threads that will execute the test plan. Each thread will
     * execute the test iterations, simulating a virtual user.
     * </p>
     * 
     * @param threads Number of threads (virtual users)
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    /**
     * Get the number of threads configured for the test.
     * 
     * @return Number of threads (virtual users)
     */
    public int getThreads() {
        return threads;
    }

    /**
     * Set the number of iterations for the test.
     * 
     * <p>
     * This sets the number of times each thread will execute the test samplers. The total number of
     * requests will be threads * iterations * number of samplers.
     * </p>
     * 
     * @param iterations Number of iterations per thread
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    /**
     * Get the number of iterations configured for the test.
     * 
     * @return Number of iterations per thread
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Set the ramp-up period for the test.
     * 
     * <p>
     * This sets the time period over which JMeter will start all threads. For example, if you have
     * 100 threads and a ramp-up period of 50 seconds, then JMeter will take 50 seconds to start all
     * 100 threads, with each thread starting 0.5 seconds after the previous thread.
     * </p>
     * 
     * @param rampUp The ramp-up period as a Duration
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setRampUp(Duration rampUp) {
        this.rampUp = rampUp;
        return this;
    }

    /**
     * Get the ramp-up period configured for the test.
     * 
     * @return The ramp-up period as a Duration
     */
    public Duration getRampUp() {
        return rampUp;
    }

    /**
     * Set the protocol name for reporting purposes.
     * 
     * <p>
     * This name is used in log files and results directories to identify the protocol being tested
     * (e.g., "http", "graphql", "jdbc", etc.).
     * </p>
     * 
     * @param protocolName The protocol name
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setProtocolName(String protocolName) {
        this.protocolName = protocolName;
        return this;
    }

    /**
     * Get the protocol name configured for reporting.
     * 
     * @return The protocol name
     */
    public String getProtocolName() {
        return protocolName;
    }

    /**
     * Set the test plan name.
     * 
     * <p>
     * This name is used in JMeter reports to identify the test plan. It's primarily for
     * documentation purposes.
     * </p>
     * 
     * @param testPlanName The test plan name
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setTestPlanName(String testPlanName) {
        this.testPlanName = testPlanName;
        return this;
    }

    /**
     * Get the test plan name.
     * 
     * @return The test plan name
     */
    public String getTestPlanName() {
        return testPlanName;
    }

    /**
     * Add a request to the test plan.
     * 
     * <p>
     * The request will be processed by the appropriate component based on its type when the test is
     * executed.
     * </p>
     * 
     * @param request The RequestEntity to add
     * @return This TestEngine instance for method chaining
     */
    public TestEngine addRequest(RequestEntity request) {
        requests.add(request);
        return this;
    }

    /**
     * Set a modifier function for HTTP requests.
     * 
     * <p>
     * This modifier will be applied to each HttpRequestEntity before it's processed by the
     * HttpComponent. It can be used to add common headers, query parameters, or other request
     * modifications.
     * </p>
     * 
     * @param modifier A Consumer function that modifies HttpRequestEntity instances
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setHttpRequestModifier(Consumer<HttpRequestEntity> modifier) {
        this.httpModifier = modifier;
        return this;
    }

    /**
     * Set a modifier function for GraphQL requests.
     * 
     * <p>
     * This modifier will be applied to each GraphQLRequestEntity before it's processed by the
     * GraphQLComponent. It can be used to add common headers, variables, or other request
     * modifications.
     * </p>
     * 
     * @param modifier A Consumer function that modifies GraphQLRequestEntity instances
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setGraphQLRequestModifier(Consumer<GraphQLRequestEntity> modifier) {
        this.graphqlModifier = modifier;
        return this;
    }

    /**
     * Set a modifier function for XML requests.
     * 
     * <p>
     * This modifier will be applied to each XmlRequestEntity before it's processed by the
     * XmlComponent. It can be used to add common headers, XML attributes, or other request
     * modifications.
     * </p>
     * 
     * @param modifier A Consumer function that modifies XmlRequestEntity instances
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setXmlRequestModifier(Consumer<XmlRequestEntity> modifier) {
        this.xmlModifier = modifier;
        return this;
    }

    /**
     * Set a modifier function for SOAP requests.
     * 
     * <p>
     * This modifier will be applied to each SoapRequestEntity before it's processed by the
     * SoapComponent. It can be used to add common headers, SOAP actions, or other request
     * modifications.
     * </p>
     * 
     * @param modifier A Consumer function that modifies SoapRequestEntity instances
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setSoapRequestModifier(Consumer<SoapRequestEntity> modifier) {
        this.soapModifier = modifier;
        return this;
    }

    /**
     * Set a modifier function for JDBC requests.
     * 
     * <p>
     * This modifier will be applied to each JdbcRequestEntity before it's processed by the
     * JdbcComponent. It can be used to add common query parameters, connection pool settings, or
     * other request modifications.
     * </p>
     * 
     * @param modifier A Consumer function that modifies JdbcRequestEntity instances
     * @return This TestEngine instance for method chaining
     */
    public TestEngine setJdbcRequestModifier(Consumer<JdbcRequestEntity> modifier) {
        this.jdbcModifier = modifier;
        return this;
    }

    /**
     * Run the test without additional configuration.
     * 
     * <p>
     * This method is an alias for runTest() to maintain compatibility with older code. It simply
     * delegates to the runTest() method.
     * </p>
     * 
     * @return TestPlanStats containing the results of the test execution
     * @throws IOException If there's an error creating result directories
     * @throws TestExecutionException If an error occurs during test execution
     */
    public TestPlanStats run() throws IOException {
        return runTest();
    }

    /**
     * Execute the test plan with all configured requests and settings.
     * 
     * <p>
     * This method processes all requests added to the TestEngine, generating JMeter samplers,
     * configuring the test execution, and running the test.
     * </p>
     * 
     * <p>
     * It handles setting up the result directories, configuring logging, creating the JMeter test
     * plan, and returning the test statistics.
     * </p>
     * 
     * <p>
     * This method has been updated to work with the new unified reporting structure that stores
     * JTL, HTML, and log files in a consistent directory layout.
     * </p>
     * 
     * @return TestPlanStats containing the results of the test execution
     * @throws IOException If there's an error creating result directories
     * @throws TestExecutionException If an error occurs during test setup or execution
     */
    public TestPlanStats runTest() throws IOException {
        if (requests.isEmpty()) {
            throw new TestExecutionException(ErrorCode.TEST_EXECUTION_ERROR,
                    "No requests added to test plan");
        }

        logger.info("Starting test execution with {} threads, {} iterations, {} ramp-up", threads,
                iterations, rampUp);

        // Configure JMeter logging for this specific test execution
        // The JMeterLogConfiguration now creates a unified directory structure
        String testLogFile = JMeterLogConfiguration.configureLogging(protocolName);
        logger.info("JMeter logs will be written to: {}", testLogFile);

        // Get the unified directory path that was created by the log configuration
        Path unifiedReportDir = JMeterLogConfiguration.getUnifiedReportDirectory();

        // Create results directories within the unified structure
        ResultsDirectories resultDirs = createResultsDirectories(protocolName, unifiedReportDir);
        Path jtlPath = resultDirs.jtlPath;
        Path htmlPath = resultDirs.htmlPath;
        logger.info("Test results will be written to: {}", jtlPath);
        logger.info("HTML reports will be generated at: {}", htmlPath);

        // Create thread group for the test with ThreadGroupChild[] samplers
        ThreadGroupChild[] samplers = generateSamplers();
        BaseThreadGroup threadGroup = threadGroup(threads, iterations, samplers);

        // Create the test plan with both JTL writer and HTML reporter
        DslTestPlan testPlan = testPlan(threadGroup, jtlWriter(jtlPath.toString()),
                htmlReporter(htmlPath.toString()));

        try {
            // Run the test and return the statistics
            TestPlanStats stats = testPlan.run();
            logger.info("Test execution completed successfully");

            // Store the unified report directory path for reference
            // Create a summary file to help navigate the unified structure
            createReportSummary(unifiedReportDir, jtlPath, htmlPath);

            return stats;
        } catch (Exception e) {
            logger.error("Test execution failed", e);
            throw new TestExecutionException(ErrorCode.TEST_EXECUTION_ERROR,
                    "Test execution failed", e);
        }
    }

    /**
     * Creates a summary file within the unified report directory to help navigate between the
     * different report files.
     * 
     * @param unifiedDir The unified report directory
     * @param jtlPath The path to the JTL file
     * @param htmlPath The path to the HTML report directory
     * @throws IOException If there's an error creating the summary file
     */
    private void createReportSummary(Path unifiedDir, Path jtlPath, Path htmlPath)
            throws IOException {
        if (unifiedDir == null) {
            logger.warn("Unable to create report summary: unified directory is null");
            return;
        }

        Path summaryPath = unifiedDir.resolve("summary.html");

        String dirName = unifiedDir.getFileName().toString();
        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>\n");
        content.append("<html>\n");
        content.append("<head>\n");
        content.append("    <meta charset=\"UTF-8\">\n");
        content.append("    <title>Unified Test Report Summary</title>\n");
        content.append("    <style>\n");
        content.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        content.append("        h1 { color: #333366; }\n");
        content.append("        h2 { color: #336699; margin-top: 20px; }\n");
        content.append(
                "        .info { margin: 20px 0; padding: 10px; background-color: #f0f0f0; border-radius: 5px; }\n");
        content.append("        .info div { margin: 10px 0; }\n");
        content.append(
                "        .label { font-weight: bold; display: inline-block; width: 150px; }\n");
        content.append(
                "        .section { margin: 20px 0; padding: 10px; background-color: #f8f8f8; border-radius: 5px; }\n");
        content.append("        .not-available { color: #999; font-style: italic; }\n");
        content.append("        ul { margin-top: 5px; }\n");
        content.append("    </style>\n");
        content.append("</head>\n");
        content.append("<body>\n");
        content.append("    <h1>Unified Test Report Summary</h1>\n");
        content.append("    <div class=\"info\">\n");
        content.append("        <div><span class=\"label\">Test Directory:</span> ").append(dirName)
                .append("</div>\n");
        content.append("        <div><span class=\"label\">Protocol:</span> ").append(protocolName)
                .append("</div>\n");
        content.append("        <div><span class=\"label\">Generated:</span> ").append(timestamp)
                .append("</div>\n");
        content.append("    </div>\n");

        // Add JTL file section
        content.append("    <div class=\"section\">\n");
        content.append("        <h2>JTL Results</h2>\n");
        if (jtlPath != null && Files.exists(jtlPath)) {
            content.append("        <ul>\n");
            content.append("            <li><a href=\"").append(jtlPath.getFileName()).append("\">")
                    .append(jtlPath.getFileName()).append("</a></li>\n");
            content.append("        </ul>\n");
        } else {
            content.append("        <p class=\"not-available\">No JTL results available</p>\n");
        }
        content.append("    </div>\n");

        // Add HTML report section
        content.append("    <div class=\"section\">\n");
        content.append("        <h2>HTML Reports</h2>\n");
        if (htmlPath != null && Files.exists(htmlPath)) {
            content.append("        <ul>\n");
            Path indexPath = htmlPath.resolve("index.html");
            if (Files.exists(indexPath)) {
                content.append(
                        "            <li><a href=\"html/index.html\">HTML Report</a></li>\n");
            } else {
                content.append(
                        "            <li>HTML directory exists but no index.html found</li>\n");
            }
            content.append("        </ul>\n");
        } else {
            content.append("        <p class=\"not-available\">No HTML reports available</p>\n");
        }
        content.append("    </div>\n");

        // Add log files section
        Path logsDir = unifiedDir.resolve("logs");
        content.append("    <div class=\"section\">\n");
        content.append("        <h2>Log Files</h2>\n");
        if (Files.exists(logsDir) && Files.isDirectory(logsDir)) {
            content.append("        <ul>\n");
            try {
                Files.list(logsDir).filter(path -> path.toString().endsWith(".log"))
                        .forEach(path -> content.append("            <li><a href=\"logs/")
                                .append(path.getFileName()).append("\">").append(path.getFileName())
                                .append("</a></li>\n"));
            } catch (IOException e) {
                content.append("            <li>Error listing log files: ").append(e.getMessage())
                        .append("</li>\n");
            }
            content.append("        </ul>\n");
        } else {
            content.append("        <p class=\"not-available\">No log files available</p>\n");
        }
        content.append("    </div>\n");

        // Add test configuration section
        content.append("    <div class=\"section\">\n");
        content.append("        <h2>Test Configuration</h2>\n");
        content.append("        <ul>\n");
        content.append("            <li><span class=\"label\">Test Plan Name:</span> ")
                .append(testPlanName).append("</li>\n");
        content.append("            <li><span class=\"label\">Threads:</span> ").append(threads)
                .append("</li>\n");
        content.append("            <li><span class=\"label\">Iterations:</span> ")
                .append(iterations).append("</li>\n");
        content.append("            <li><span class=\"label\">Ramp-up:</span> ").append(rampUp)
                .append("</li>\n");
        content.append("            <li><span class=\"label\">Requests:</span> ")
                .append(requests.size()).append("</li>\n");
        content.append("        </ul>\n");
        content.append("    </div>\n");

        content.append("</body>\n");
        content.append("</html>\n");

        Files.writeString(summaryPath, content.toString());
        logger.info("Created report summary at: {}", summaryPath);
    }

    /**
     * Generates JMeter samplers for all requests in the test plan.
     * 
     * <p>
     * This method processes each request entity using the appropriate component registered with the
     * TestSystem, applying any modifiers before processing.
     * </p>
     * 
     * @return Array of JMeter samplers for the test plan
     */
    private ThreadGroupChild[] generateSamplers() {
        List<ThreadGroupChild> samplers = new ArrayList<>();

        for (RequestEntity request : requests) {
            // Apply modifiers based on request type
            if (request instanceof HttpRequestEntity && httpModifier != null) {
                httpModifier.accept((HttpRequestEntity) request);
            } else if (request instanceof GraphQLRequestEntity && graphqlModifier != null) {
                graphqlModifier.accept((GraphQLRequestEntity) request);
            } else if (request instanceof XmlRequestEntity && xmlModifier != null) {
                xmlModifier.accept((XmlRequestEntity) request);
            } else if (request instanceof SoapRequestEntity && soapModifier != null) {
                soapModifier.accept((SoapRequestEntity) request);
            } else if (request instanceof JdbcRequestEntity && jdbcModifier != null) {
                jdbcModifier.accept((JdbcRequestEntity) request);
            }

            // Process the request with the TestSystem to generate the sampler
            Result<BaseThreadGroup.ThreadGroupChild> result =
                    testSystem.safeProcessRequest(request);

            if (result.isSuccess()) {
                // Add the generated sampler to the list
                samplers.add(result.getValue());
            } else {
                // Log the error but continue with other samplers
                logger.error("Failed to process request: {} - {}", result.getErrorCode(),
                        result.getErrorMessage());
            }
        }

        // Convert the list to an array for the threadGroup method
        return samplers.toArray(new ThreadGroupChild[0]);
    }

    /**
     * Creates the necessary directories for storing test results.
     * 
     * <p>
     * This method creates directories for JTL files and HTML reports using the new unified
     * directory structure.
     * </p>
     * 
     * @param protocol Protocol name for directory naming
     * @param unifiedReportDir The unified report directory to use (created by
     *        JMeterLogConfiguration)
     * @return A ResultsDirectories object containing the paths
     * @throws IOException If directory creation fails
     */
    private ResultsDirectories createResultsDirectories(String protocol, Path unifiedReportDir)
            throws IOException {
        // Create a JTL file path within the unified directory
        Path jtlPath = unifiedReportDir.resolve(String.format("%s.jtl", protocol));

        // HTML reports will be in a subdirectory of the unified directory
        Path htmlPath = unifiedReportDir.resolve("html");
        if (!Files.exists(htmlPath)) {
            Files.createDirectories(htmlPath);
        }

        // Store the paths for later reference
        resultPaths.put(protocol, jtlPath);
        htmlReportPaths.put(protocol, htmlPath);
        unifiedReportPaths.put(protocol, unifiedReportDir);

        logger.info("Configured unified directory structure:");
        logger.info("  Unified Directory: {}", unifiedReportDir);
        logger.info("  JTL File: {}", jtlPath);
        logger.info("  HTML Reports: {}", htmlPath);
        logger.info("  Log Files: {}", unifiedReportDir.resolve("logs"));

        return new ResultsDirectories(jtlPath, htmlPath);
    }

    /**
     * Get the path to the JTL results file for a protocol.
     * 
     * <p>
     * If you've run multiple tests with the same protocol, this will return the path for the most
     * recent test.
     * </p>
     * 
     * @param protocol The protocol name
     * @return The path to the JTL file, or null if not found
     */
    public Path getResultsPath(String protocol) {
        return resultPaths.get(protocol);
    }

    /**
     * Get the path to the HTML report directory for a protocol.
     * 
     * <p>
     * If you've run multiple tests with the same protocol, this will return the path for the most
     * recent test.
     * </p>
     * 
     * @param protocol The protocol name
     * @return The path to the HTML report directory, or null if not found
     */
    public Path getHtmlReportPath(String protocol) {
        return htmlReportPaths.get(protocol);
    }

    /**
     * Get the path to the unified report directory for a protocol.
     * 
     * <p>
     * If you've run multiple tests with the same protocol, this will return the path for the most
     * recent test.
     * </p>
     * 
     * @param protocol The protocol name
     * @return The path to the unified report directory, or null if not found
     */
    public Path getUnifiedReportPath(String protocol) {
        return unifiedReportPaths.get(protocol);
    }

    /**
     * Simple class to hold the paths for test results.
     */
    private static class ResultsDirectories {
        final Path jtlPath;
        final Path htmlPath;

        ResultsDirectories(Path jtlPath, Path htmlPath) {
            this.jtlPath = jtlPath;
            this.htmlPath = htmlPath;
        }
    }
}
