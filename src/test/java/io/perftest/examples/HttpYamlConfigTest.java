package io.perftest.examples;

import io.perftest.components.http.HttpComponent;
import io.perftest.core.BaseTest;
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
 * Example test that loads HTTP test configuration from YAML
 */
public class HttpYamlConfigTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpYamlConfigTest.class);
    private static final String CONFIG_FILE = "http-config.yml";

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "http");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testHttpWithYamlConfig() throws IOException {
        // Load configuration from YAML file
        Map<String, Object> config = YamlConfigLoader.loadConfig(CONFIG_FILE);
        
        // Create test system with HTTP component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(HttpRequestEntity.class, new HttpComponent());
        
        // Configure test engine from YAML
        TestEngine engine = configureTestEngine(testSystem, config);
        
        // Set the protocol name to 'http' for generating the report
        engine.setProtocolName("http");
        
        // Create and configure HTTP request entity from YAML
        HttpRequestEntity httpRequest = createHttpRequestEntity(config);
        engine.addRequest(httpRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
    
    private HttpRequestEntity createHttpRequestEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = (Map<String, Object>) config.getOrDefault("request", Map.of());
        String name = (String) requestConfig.getOrDefault("name", "HTTP Request");
        String endpoint = (String) requestConfig.getOrDefault("endpoint", "https://example.com/api");
        
        // Create entity with the URL and then set the name as a property
        HttpRequestEntity entity = new HttpRequestEntity(endpoint);
        entity.setName(name);
        
        // Set method if present (default to GET)
        String method = (String) requestConfig.getOrDefault("method", "GET");
        entity.setMethod(method);
        
        // Set body if present
        if (requestConfig.containsKey("body")) {
            String body = (String) requestConfig.get("body");
            entity.setBody(body);
        }
        
        // Set headers if present
        if (requestConfig.containsKey("headers")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) requestConfig.get("headers");
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                entity.addHeader(header.getKey(), header.getValue().toString());
            }
        }
        
        return entity;
    }
}
