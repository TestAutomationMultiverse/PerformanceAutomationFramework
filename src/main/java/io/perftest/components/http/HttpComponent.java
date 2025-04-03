package io.perftest.components.http;

import io.perftest.core.data.TemplateProcessor;
import io.perftest.entities.request.HttpRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.time.Duration;
import java.util.Map;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;

/**
 * Component for HTTP operations
 */
public class HttpComponent {
    private static final Logger logger = LoggerFactory.getLogger(HttpComponent.class);
    private final TemplateProcessor templateProcessor;
    
    /**
     * Creates a new HTTP component
     */
    public HttpComponent() {
        this.templateProcessor = new TemplateProcessor();
    }
    
    /**
     * Creates a JMeter DSL HTTP sampler configured for an HTTP request
     * @param requestEntity HTTP request entity
     * @return Configured HTTP sampler
     */
    public DslHttpSampler createHttpSampler(HttpRequestEntity requestEntity) {
        logger.info("Creating HTTP sampler for URL: {}", requestEntity.getUrl());
        
        // Process body if templating is involved and body exists
        String body = requestEntity.getBody();
        if (body != null && requestEntity.getVariables() != null && !requestEntity.getVariables().isEmpty()) {
            body = templateProcessor.processTemplate(body, requestEntity.getVariables());
        }
        
        // Create HTTP sampler
        DslHttpSampler sampler = httpSampler(requestEntity.getUrl())
            .method(requestEntity.getMethod())
            .followRedirects(requestEntity.getFollowRedirects());
        
        // Add body if not null
        if (body != null && !body.isEmpty()) {
            sampler = sampler.body(body);
        }
        
        // Add headers
        for (Map.Entry<String, String> header : requestEntity.getHeaders().entrySet()) {
            sampler = sampler.header(header.getKey(), header.getValue());
        }
        
        // Set timeouts through JMeter properties
        // Note: In earlier versions of JMeter DSL, timeouts are not directly exposed as methods
        // For version 1.29.1, we'll add the connect/response timeout manually
        // We can also remove this entirely since defaults will be used
        
        return sampler;
    }
}