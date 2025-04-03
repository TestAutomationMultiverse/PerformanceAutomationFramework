package io.perftest.examples;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.perftest.core.engine.TestEngine;
import io.perftest.entities.request.SoapRequestEntity;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Example of SOAP tests configured via YAML files
 */
public class SoapYamlConfigTest {

    /**
     * Test SOAP requests defined in YAML configuration
     */
    @Test
    public void testSoapFromYamlConfig() throws Exception {
        // Initialize the test engine
        TestEngine testEngine = new TestEngine();
        
        // Load SOAP request configurations from YAML
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream inputStream = getClass().getResourceAsStream("/soap/sample-soap-request.yml");
        List<Map<String, Object>> soapConfigs = mapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        
        // Execute each SOAP request
        for (Map<String, Object> config : soapConfigs) {
            // Create SOAP request entity from configuration
            SoapRequestEntity soapRequest = createSoapRequestFromConfig(config);
            
            // Execute the SOAP test
            System.out.println("Executing SOAP test: " + config.get("name"));
            TestPlanStats stats = testEngine.executeSoapTest(soapRequest, 1, 1);
            
            // Print statistics
            System.out.println("Test completed with 90th percentile: " + 
                stats.overall().sampleTime().perc90().toMillis() + "ms");
            System.out.println("Error count: " + stats.overall().errorsCount());
            System.out.println();
        }
    }
    
    /**
     * Create a SoapRequestEntity from a YAML configuration map
     * @param config YAML configuration map
     * @return SoapRequestEntity configured from YAML
     */
    private SoapRequestEntity createSoapRequestFromConfig(Map<String, Object> config) {
        // Create SOAP request entity with URL
        SoapRequestEntity soapRequest = new SoapRequestEntity((String) config.get("url"));
        
        // Set XML body
        if (config.containsKey("xmlBody")) {
            soapRequest.setXmlBody((String) config.get("xmlBody"));
        }
        
        // Set SOAP action
        if (config.containsKey("soapAction")) {
            soapRequest.setSoapAction((String) config.get("soapAction"));
        }
        
        // Set expected status
        if (config.containsKey("expectedStatus")) {
            int status = ((Integer) config.get("expectedStatus")).intValue();
            soapRequest.setExpectedStatus(status);
        }
        
        // Add headers if defined
        if (config.containsKey("headers")) {
            Map<String, String> headers = (Map<String, String>) config.get("headers");
            for (Map.Entry<String, String> header : headers.entrySet()) {
                soapRequest.addHeader(header.getKey(), header.getValue());
            }
        }
        
        // Add template variables if defined
        if (config.containsKey("variables")) {
            Map<String, Object> variables = (Map<String, Object>) config.get("variables");
            for (Map.Entry<String, Object> var : variables.entrySet()) {
                soapRequest.addVariable(var.getKey(), var.getValue());
            }
        }
        
        // Add assertions
        if (config.containsKey("assertions")) {
            Map<String, String> assertions = (Map<String, String>) config.get("assertions");
            for (Map.Entry<String, String> assertion : assertions.entrySet()) {
                soapRequest.addAssertion(assertion.getKey(), assertion.getValue());
            }
        }
        
        return soapRequest;
    }
}