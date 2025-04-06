package io.perftest.components.http;

import io.perftest.components.core.Component;
import io.perftest.entities.request.HttpRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.time.Duration;
import java.util.Map;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

/**
 * HTTP Component implementation for processing HTTP request entities.
 * 
 * <p>This component transforms {@link HttpRequestEntity} objects into JMeter HTTP samplers
 * that can be added to a test plan. It handles all aspects of HTTP requests including
 * URL, method, headers, body, content type, and timeouts.</p>
 * 
 * <p>The component leverages JMeter DSL to create HTTP samplers with the correct
 * configuration based on the entity's properties. It supports all standard HTTP methods
 * (GET, POST, PUT, DELETE, etc.) and can handle various content types.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * HttpRequestEntity request = new HttpRequestEntity()
 *     .setUrl("https://api.example.com/resource")
 *     .setMethod("POST")
 *     .setBody("{\"key\": \"value\"}")
 *     .setContentType("application/json")
 *     .addHeader("Authorization", "Bearer token");
 *     
 * HttpComponent component = new HttpComponent();
 * DslHttpSampler sampler = component.process(request);
 * </pre>
 * 
 * @since 1.0
 */
public class HttpComponent implements Component<HttpRequestEntity, DslHttpSampler> {
    private static final Logger logger = LoggerFactory.getLogger(HttpComponent.class);
    
    /**
     * Processes an HTTP request entity and converts it to a JMeter HTTP sampler.
     * 
     * <p>This method extracts all relevant properties from the HTTP request entity
     * and configures a JMeter HTTP sampler accordingly. It handles URL, method, headers,
     * body content, content type, and timeout settings.</p>
     * 
     * <p>The method builds the sampler in a fluent style, adding each configuration
     * element as needed based on the entity's properties.</p>
     * 
     * @param entity The HTTP request entity to process, must not be null
     * @return A configured JMeter HTTP sampler ready to be added to a test plan
     * @throws io.perftest.exception.ComponentException If processing fails due to invalid entity data
     */
    @Override
    public DslHttpSampler process(HttpRequestEntity entity) {
        logger.info("Processing HTTP request for URL: {}", entity.getUrl());
        
        // Build HTTP sampler with URL and method
        DslHttpSampler sampler = httpSampler(entity.getUrl())
            .method(entity.getMethod());
        
        // Add content type and body if available
        if (entity.getBody() != null && !entity.getBody().isEmpty()) {
            sampler = sampler.body(entity.getBody());
            
            // Add content type header if available
            if (entity.getContentType() != null && !entity.getContentType().isEmpty()) {
                // Use header approach for content type as directly using contentType method might not be compatible
                sampler = sampler.header("Content-Type", entity.getContentType());
            }
        }
        
        // Add all headers from the entity
        if (entity.getHeaders() != null && !entity.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : entity.getHeaders().entrySet()) {
                sampler = sampler.header(header.getKey(), header.getValue());
            }
        }
        
        // Set connection and response timeouts if specified
        if (entity.getConnectTimeout() > 0) {
            sampler = sampler.connectionTimeout(Duration.ofMillis(entity.getConnectTimeout()));
        }
        
        if (entity.getResponseTimeout() > 0) {
            sampler = sampler.responseTimeout(Duration.ofMillis(entity.getResponseTimeout()));
        }
        
        return sampler;
    }
}
