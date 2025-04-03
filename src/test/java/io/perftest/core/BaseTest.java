package io.perftest.core;

import io.perftest.adapters.TestEngineAdapter;
import io.perftest.core.config.ConfigManager;
import io.perftest.core.engine.TestEngine;
import io.perftest.core.templating.TemplateEngine;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.RequestEntity;
import io.perftest.entities.request.SoapRequestEntity;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base test class that provides common functionality for all test classes
 */
public abstract class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    
    // Shared static instances to avoid recreating for each test
    protected static ConfigManager configManager;
    protected static TemplateEngine templateEngine;
    protected static TestEngine testEngine;
    protected static TestEngineAdapter testEngineAdapter;
    protected static Map<String, Map<String, Object>> configs;

    /**
     * Initialize all the shared components and configurations
     * This method loads all the standard configuration files
     */
    @BeforeAll
    public static void initializeFramework() throws IOException {
        // Initialize only once
        if (configManager == null) {
            logger.info("Initializing test framework components");
            configManager = new ConfigManager();
            templateEngine = new TemplateEngine();
            testEngine = new TestEngine();
            testEngineAdapter = new TestEngineAdapter();
            
            // Load all standard configurations
            configManager.loadConfigsFromResources(
                "http-config.yml", 
                "graphql-config.yml", 
                "soap-config.yml"
            );
            
            // Get all configs by protocol
            configs = configManager.getAllConfigs();
            logger.info("Loaded configurations for {} protocols", configs.size());
        }
    }

    /**
     * Get integer value from a configuration object
     * @param value Object value from configuration
     * @param defaultValue Default value if conversion fails
     * @return Converted integer value
     */
    protected int getIntValue(Object value, int defaultValue) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get integer value with default 1
     * @param value Object value from configuration
     * @return Converted integer value, default 1
     */
    protected int getIntValue(Object value) {
        return getIntValue(value, 1);
    }

    /**
     * Execute an HTTP test and validate the results
     * @param requestEntity The HTTP request entity
     * @param threads Number of threads
     * @param iterations Number of iterations
     * @return Test statistics
     * @throws IOException If an error occurs
     */
    protected TestPlanStats executeHttpTest(HttpRequestEntity requestEntity, int threads, int iterations) throws IOException {
        logger.info("Executing HTTP test: {} (URL: {})", requestEntity.getName(), requestEntity.getUrl());
        TestPlanStats stats = testEngine.executeHttpTest(requestEntity, threads, iterations);
        logTestResults(stats, requestEntity);
        return stats;
    }

    /**
     * Execute a GraphQL test and validate the results
     * @param requestEntity The GraphQL request entity
     * @param threads Number of threads
     * @param iterations Number of iterations
     * @return Test statistics
     * @throws IOException If an error occurs
     */
    protected TestPlanStats executeGraphQLTest(GraphQLRequestEntity requestEntity, int threads, int iterations) throws IOException {
        logger.info("Executing GraphQL test: {} (URL: {})", requestEntity.getName(), requestEntity.getUrl());
        TestPlanStats stats = testEngine.executeGraphQLTest(requestEntity, threads, iterations);
        logTestResults(stats, requestEntity);
        return stats;
    }

    /**
     * Execute a SOAP test and validate the results
     * @param requestEntity The SOAP request entity
     * @param threads Number of threads
     * @param iterations Number of iterations
     * @return Test statistics
     * @throws IOException If an error occurs
     */
    protected TestPlanStats executeSoapTest(SoapRequestEntity requestEntity, int threads, int iterations) throws IOException {
        logger.info("Executing SOAP test: {} (URL: {})", requestEntity.getName(), requestEntity.getUrl());
        TestPlanStats stats = testEngine.executeSoapTest(requestEntity, threads, iterations);
        logTestResults(stats, requestEntity);
        return stats;
    }

    /**
     * Log test results and handle any errors
     * @param stats Test statistics
     * @param requestEntity Request entity
     */
    protected void logTestResults(TestPlanStats stats, RequestEntity requestEntity) {
        // Log performance metrics
        logger.info("Test completed with average response time: {}ms", stats.overall().sampleTime().mean());
        
        // Get and log any errors
        long errorCount = stats.overall().errorsCount();
        logger.info("Error count: {}", errorCount);
        
        if (errorCount > 0) {
            logger.error("Test errors detected: {}", errorCount);
            logger.error("Sample response time: {}", stats.overall().sampleTime());
            logger.error("Test URL: {}", requestEntity.getUrl());
            logger.error("Test metrics: {}", stats.overall().toString());
        }
        
        // Assert that there are no errors
        assert errorCount == 0 : "Expected no errors but got " + errorCount;
    }

    /**
     * Read a template file from resources
     * @param templatePath Path to template in resources
     * @return Template content
     * @throws IOException If file cannot be read
     */
    protected String readTemplateFile(String templatePath) throws IOException {
        // First try as absolute path
        Path path = Paths.get(templatePath);
        if (Files.exists(path)) {
            return Files.readString(path);
        }
        
        // Then try as classpath resource
        String resourcePath = templatePath.startsWith("/") ? templatePath : "/" + templatePath;
        try (var is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                return new String(is.readAllBytes());
            }
        }
        
        // Finally try as relative path to templates directory
        Path relPath = Paths.get("src/test/resources/templates", templatePath);
        if (Files.exists(relPath)) {
            return Files.readString(relPath);
        }
        
        throw new IOException("Template file not found: " + templatePath);
    }
    
    /**
     * Create HTTP request entity from configuration
     * @param testName Name of the test in configuration
     * @return Configured HTTP request entity
     */
    protected HttpRequestEntity createHttpRequestFromConfig(String testName) {
        // Get HTTP config
        Map<String, Object> httpConfig = configs.get("http");
        if (httpConfig == null) {
            throw new IllegalStateException("HTTP configuration not found");
        }
        
        // Get defaults
        Map<String, Object> defaults = (Map<String, Object>) httpConfig.get("defaults");
        if (defaults == null) {
            throw new IllegalStateException("HTTP defaults not found in configuration");
        }
        
        // Get tests list
        List<Map<String, Object>> tests = (List<Map<String, Object>>) httpConfig.get("tests");
        if (tests == null) {
            throw new IllegalStateException("HTTP tests not found in configuration");
        }
        
        // Find test by name
        Map<String, Object> testConfig = tests.stream()
            .filter(test -> testName.equals(test.get("name")))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Test not found: " + testName));
        
        // Create request entity using the config-based constructor
        HttpRequestEntity requestEntity = new HttpRequestEntity("tests", testName);
        
        // Customize if needed using the test configuration
        if (testConfig.containsKey("method")) {
            requestEntity.setMethod((String) testConfig.get("method"));
        }
        
        if (testConfig.containsKey("expected_status")) {
            requestEntity.setExpectedStatus((Integer) testConfig.get("expected_status"));
        }
        
        // Add body if present
        if (testConfig.containsKey("body")) {
            requestEntity.setBody((String) testConfig.get("body"));
        }
        
        // Set timeouts from defaults
        if (defaults.containsKey("connectTimeout")) {
            requestEntity.setConnectTimeout((Integer) defaults.get("connectTimeout"));
        }
        
        if (defaults.containsKey("responseTimeout")) {
            requestEntity.setResponseTimeout((Integer) defaults.get("responseTimeout"));
        }
        
        // Add assertions if present
        if (testConfig.containsKey("assertions")) {
            Map<String, String> assertions = (Map<String, String>) testConfig.get("assertions");
            assertions.forEach(requestEntity::addAssertion);
        }
        
        return requestEntity;
    }
    
    /**
     * Get test configuration by protocol and test name
     * @param protocol Protocol name (http, graphql, soap)
     * @param testName Test name
     * @return Test configuration map
     */
    protected Map<String, Object> getTestConfig(String protocol, String testName) {
        // Get protocol config
        Map<String, Object> protocolConfig = configs.get(protocol);
        if (protocolConfig == null) {
            throw new IllegalStateException(protocol + " configuration not found");
        }
        
        // Get tests from the protocol
        List<Map<String, Object>> tests;
        if (protocolConfig.containsKey("tests")) {
            tests = (List<Map<String, Object>>) protocolConfig.get("tests");
        } else {
            // Try to find nested test sections (like k6_tests, auth_tests, etc.)
            tests = protocolConfig.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith("_tests") && entry.getValue() instanceof Map)
                .flatMap(entry -> {
                    Map<String, Object> section = (Map<String, Object>) entry.getValue();
                    if (section.containsKey("tests") && section.get("tests") instanceof List) {
                        return ((List<Map<String, Object>>) section.get("tests")).stream();
                    }
                    return null;
                })
                .filter(test -> test != null)
                .collect(Collectors.toList());
        }
        
        if (tests == null || tests.isEmpty()) {
            throw new IllegalStateException("No tests found for protocol: " + protocol);
        }
        
        // Find test by name
        return tests.stream()
            .filter(test -> testName.equals(test.get("name")))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Test not found: " + testName + " in protocol: " + protocol));
    }
    
    /**
     * Get protocol defaults from configuration
     * @param protocol Protocol name (http, graphql, soap)
     * @return Defaults map
     */
    protected Map<String, Object> getProtocolDefaults(String protocol) {
        // Get protocol config
        Map<String, Object> protocolConfig = configs.get(protocol);
        if (protocolConfig == null) {
            throw new IllegalStateException(protocol + " configuration not found");
        }
        
        // Get defaults
        Map<String, Object> defaults = (Map<String, Object>) protocolConfig.get("defaults");
        if (defaults == null) {
            // Create empty defaults
            defaults = new HashMap<>();
        }
        
        return defaults;
    }
}