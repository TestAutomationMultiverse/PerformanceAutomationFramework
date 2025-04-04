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
 * Component for processing HTTP requests
 */
public class HttpComponent implements Component<HttpRequestEntity, DslHttpSampler> {
    private static final Logger logger = LoggerFactory.getLogger(HttpComponent.class);
    
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
        
        // Add all headers
        if (entity.getHeaders() != null && !entity.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : entity.getHeaders().entrySet()) {
                sampler = sampler.header(header.getKey(), header.getValue());
            }
        }
        
        // Set timeouts
        if (entity.getConnectTimeout() > 0) {
            sampler = sampler.connectionTimeout(Duration.ofMillis(entity.getConnectTimeout()));
        }
        
        if (entity.getResponseTimeout() > 0) {
            sampler = sampler.responseTimeout(Duration.ofMillis(entity.getResponseTimeout()));
        }
        
        return sampler;
    }
}
