package io.perftest.examples;

import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.SoapRequestEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for configuration system integration
 */
public class ConfigurationTest {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationTest.class);
    
    @Test
    public void testHttpConfigurationDefaults() {
        // Create HTTP entity
        HttpRequestEntity entity = new HttpRequestEntity("https://test-api.k6.io/public/crocodiles/");
        
        // Check defaults were loaded from config
        assertEquals("GET", entity.getMethod(), "Default HTTP method should be GET");
        assertTrue(entity.getFollowRedirects(), "Follow redirects should be true by default");
        
        // Check headers
        Map<String, String> headers = entity.getHeaders();
        boolean found = false;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if ("Content-Type".equalsIgnoreCase(header.getKey())) {
                assertTrue(header.getValue().contains("application/json"), 
                          "Default content type should be application/json");
                found = true;
                break;
            }
        }
        assertTrue(found, "Default Content-Type header should be set");
        
        // Check timeouts
        assertNotNull(entity.getConnectTimeout(), "Connect timeout should be set");
        assertNotNull(entity.getResponseTimeout(), "Response timeout should be set");
        
        logger.info("HTTP configuration test passed");
    }
    
    @Test
    public void testGraphQLConfigurationDefaults() {
        // Create GraphQL entity
        GraphQLRequestEntity entity = new GraphQLRequestEntity("https://test-api.k6.io/graphql");
        
        // Check defaults were loaded from config
        assertEquals("POST", entity.getMethod(), "Default GraphQL method should be POST");
        assertTrue(entity.getFollowRedirects(), "Follow redirects should be true by default");
        
        // Check headers
        Map<String, String> headers = entity.getHeaders();
        boolean found = false;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if ("Content-Type".equalsIgnoreCase(header.getKey())) {
                assertTrue(header.getValue().contains("application/json"), 
                          "Default content type should be application/json");
                found = true;
                break;
            }
        }
        assertTrue(found, "Default Content-Type header should be set");
        
        // Check timeouts
        assertNotNull(entity.getConnectTimeout(), "Connect timeout should be set");
        assertNotNull(entity.getResponseTimeout(), "Response timeout should be set");
        
        // Check GraphQL specific default field names
        assertEquals("operationName", entity.getOperationNameField(), "Default operation name field should be 'operationName'");
        assertEquals("query", entity.getQueryField(), "Default query field should be 'query'");
        assertEquals("variables", entity.getVariablesField(), "Default variables field should be 'variables'");
        
        logger.info("GraphQL configuration test passed");
    }
    
    @Test
    public void testSoapConfigurationDefaults() {
        // Create SOAP entity
        SoapRequestEntity entity = new SoapRequestEntity("http://www.dneonline.com/calculator.asmx");
        
        // Check defaults were loaded from config
        assertEquals("POST", entity.getMethod(), "Default SOAP method should be POST");
        assertEquals("1.1", entity.getSoapVersion(), "Default SOAP version should be 1.1");
        assertTrue(entity.getFollowRedirects(), "Follow redirects should be true by default");
        
        // Check headers
        Map<String, String> headers = entity.getHeaders();
        boolean found = false;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if ("Content-Type".equalsIgnoreCase(header.getKey())) {
                assertTrue(header.getValue().contains("text/xml"), 
                          "Default content type for SOAP 1.1 should be text/xml");
                found = true;
                break;
            }
        }
        assertTrue(found, "Default Content-Type header should be set");
        
        // Check envelope template
        String envelope = entity.getSoapEnvelopeTemplate();
        assertNotNull(envelope, "SOAP envelope template should not be null");
        assertTrue(envelope.contains("<soap:Envelope"), "Envelope template should contain soap:Envelope");
        assertTrue(envelope.contains("$PAYLOAD$"), "Envelope template should contain $PAYLOAD$ placeholder");
        
        // Test SOAP 1.2 version change
        entity.setSoapVersion("1.2");
        assertEquals("1.2", entity.getSoapVersion(), "SOAP version should be updated to 1.2");
        
        // Check content type was updated
        headers = entity.getHeaders();
        found = false;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if ("Content-Type".equalsIgnoreCase(header.getKey())) {
                assertTrue(header.getValue().contains("application/soap+xml"), 
                          "Content type for SOAP 1.2 should be application/soap+xml");
                found = true;
                break;
            }
        }
        assertTrue(found, "Updated Content-Type header should be set for SOAP 1.2");
        
        // Check envelope template for 1.2
        envelope = entity.getSoapEnvelopeTemplate();
        assertTrue(envelope.contains("http://www.w3.org/2003/05/soap-envelope"), 
                  "SOAP 1.2 envelope should use the correct namespace");
        
        logger.info("SOAP configuration test passed");
    }
}