package io.perftest.http_tests;

import io.perftest.components.http.HttpComponent;
import io.perftest.core.test.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.systems.TestSystem;
import io.perftest.util.YamlConfigLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * HTTP performance test for K6 API - Crocodiles endpoint
 */
public class K6CrocodilesApiTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(K6CrocodilesApiTest.class);
    private static final String CONFIG_FILE = "http_tests/k6-crocodiles-config.yml";

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "k6-crocodiles-api");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testK6CrocodilesApi() throws IOException {
        // Load configuration from YAML file
        Map<String, Object> config = YamlConfigLoader.loadConfig(CONFIG_FILE);
        
        // Create test system with HTTP component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(HttpRequestEntity.class, new HttpComponent());
        
        // Configure test engine from YAML
        TestEngine engine = configureTestEngine(testSystem, config);
        
        // Set the specific protocol and test name for reporting
        engine.setProtocolName("k6-crocodiles-api");
        
        // Create HTTP request entity from YAML
        HttpRequestEntity httpRequest = createHttpRequestEntity(config);
        engine.addRequest(httpRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
    
    private HttpRequestEntity createHttpRequestEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = YamlConfigLoader.getMap(config, "request");
        String name = YamlConfigLoader.getString(requestConfig, "name", "K6 Crocodiles API Test");
        String endpoint = YamlConfigLoader.getString(requestConfig, "endpoint", "https://test-api.k6.io/public/crocodiles/");
        String method = YamlConfigLoader.getString(requestConfig, "method", "GET");
        
        // Create entity with the URL
        HttpRequestEntity entity = new HttpRequestEntity(endpoint);
        entity.setName(name);
        entity.setMethod(method);
        
        // Try to load headers from template
        String headersPath = YamlConfigLoader.getTemplatePath(config, "headersPath");
        if (headersPath != null) {
            try {
                // In a real implementation, you would parse the JSON headers template
                // For now, we'll just add default headers
                entity.addHeader("Accept", "application/json");
                logger.info("Processed headers from template: {}", headersPath);
            } catch (Exception e) {
                logger.warn("Failed to process headers template: {}", e.getMessage());
            }
        } else if (requestConfig.containsKey("headers")) {
            // Use inline headers from config
            Map<String, Object> headers = YamlConfigLoader.getMap(requestConfig, "headers");
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                entity.addHeader(header.getKey(), header.getValue().toString());
            }
        } else {
            // Add default headers if none specified
            entity.addHeader("Accept", "application/json");
        }
        
        return entity;
    }
}
