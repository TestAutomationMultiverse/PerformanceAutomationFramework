package io.perftest.components.graphql;

import io.perftest.components.core.Component;
import io.perftest.entities.request.GraphQLRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

/**
 * Component for processing GraphQL requests
 */
public class GraphQLComponent implements Component<GraphQLRequestEntity, DslHttpSampler> {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLComponent.class);
    
    @Override
    public DslHttpSampler process(GraphQLRequestEntity entity) {
        logger.info("Processing GraphQL request for URL: {}", entity.getUrl());
        
        // Build the GraphQL payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", entity.getQuery());
        
        // Add variables if available
        if (entity.getVariables() != null && !entity.getVariables().isEmpty()) {
            payload.put("variables", entity.getVariables());
        }
        
        try {
            // Convert payload to JSON
            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            
            // Build HTTP sampler with application/json content-type
            DslHttpSampler sampler = httpSampler(entity.getUrl())
                .method("POST")
                .body(jsonPayload)
                .header("Content-Type", "application/json");
            
            // Add all headers
            if (entity.getHeaders() != null && !entity.getHeaders().isEmpty()) {
                for (Map.Entry<String, String> header : entity.getHeaders().entrySet()) {
                    sampler = sampler.header(header.getKey(), header.getValue());
                }
            }
            
            // Set timeouts if they are defined
            if (entity.getConnectTimeout() > 0) {
                sampler = sampler.connectionTimeout(Duration.ofMillis(entity.getConnectTimeout()));
            }
            
            if (entity.getResponseTimeout() > 0) {
                sampler = sampler.responseTimeout(Duration.ofMillis(entity.getResponseTimeout()));
            }
            
            return sampler;
        } catch (Exception e) {
            logger.error("Error creating GraphQL request", e);
            throw new RuntimeException("Failed to create GraphQL request: " + e.getMessage(), e);
        }
    }
}
