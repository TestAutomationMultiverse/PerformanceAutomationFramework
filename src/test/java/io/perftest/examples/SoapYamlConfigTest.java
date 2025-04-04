package io.perftest.examples;

import io.perftest.components.soap.SoapComponent;
import io.perftest.core.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.SoapRequestEntity;
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
 * Example test that loads SOAP test configuration from YAML
 */
public class SoapYamlConfigTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(SoapYamlConfigTest.class);
    private static final String CONFIG_FILE = "soap-config.yml";

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "soap");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testSoapWithYamlConfig() throws IOException {
        // Load configuration from YAML file
        Map<String, Object> config = YamlConfigLoader.loadConfig(CONFIG_FILE);
        
        // Create test system with SOAP component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(SoapRequestEntity.class, new SoapComponent());
        
        // Configure test engine from YAML
        TestEngine engine = configureTestEngine(testSystem, config);
        
        // Set the protocol name to 'soap' for generating the report
        engine.setProtocolName("soap");
        
        // Create and configure SOAP request entity from YAML
        SoapRequestEntity soapRequest = createSoapRequestEntity(config);
        engine.addRequest(soapRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
    
    private SoapRequestEntity createSoapRequestEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = (Map<String, Object>) config.getOrDefault("request", Map.of());
        String name = (String) requestConfig.getOrDefault("name", "SOAP Request");
        String endpoint = (String) requestConfig.getOrDefault("endpoint", "https://example.com/soap");
        String soapAction = (String) requestConfig.getOrDefault("soapAction", "");
        String payload = (String) requestConfig.getOrDefault("payload", "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body></soap:Body></soap:Envelope>");
        
        // Create entity with the URL and then set the other properties
        SoapRequestEntity entity = new SoapRequestEntity(endpoint);
        entity.setName(name);
        entity.setSoapAction(soapAction);
        entity.setPayload(payload);
        
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
