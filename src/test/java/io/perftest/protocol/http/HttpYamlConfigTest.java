package io.perftest.protocol.http;

import io.perftest.components.http.HttpComponent;
import io.perftest.core.test.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.factories.EntityFactory;
import io.perftest.systems.TestSystem;
import io.perftest.util.YamlConfigLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Test that loads HTTP test configuration from YAML
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
    @DisplayName("Run HTTP test using YAML configuration")
    public void testHttpWithYamlConfig() throws IOException {
        // Load configuration from YAML file
        Map<String, Object> config = YamlConfigLoader.loadConfig(CONFIG_FILE);
        
        // Create test system with HTTP component
        TestSystem testSystem = new TestSystem();
        // Register the HTTP component to handle HttpRequestEntity instances
        testSystem.addComponent(HttpRequestEntity.class, new HttpComponent());
        
        // Configure test engine from YAML
        TestEngine engine = configureTestEngine(testSystem, config);
        
        // Set the protocol name to 'http' for generating the report
        engine.setProtocolName("http");
        
        // Create and configure HTTP request entity from YAML using the EntityFactory
        HttpRequestEntity httpRequest = EntityFactory.createHttpEntity(config);
        engine.addRequest(httpRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
}
