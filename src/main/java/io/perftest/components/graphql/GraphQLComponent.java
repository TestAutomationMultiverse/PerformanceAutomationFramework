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
 * GraphQL Component implementation for processing GraphQL request entities.
 * 
 * <p>This component transforms {@link GraphQLRequestEntity} objects into JMeter HTTP samplers
 * configured to perform GraphQL requests. It handles GraphQL-specific aspects such as 
 * query formatting, variables, and operation names, and converts them into the standard
 * JSON format expected by GraphQL endpoints.</p>
 * 
 * <p>GraphQL requests are sent as HTTP POST requests with a JSON payload containing the
 * query and optional variables. The component builds this payload dynamically based on
 * the entity's properties.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * GraphQLRequestEntity request = new GraphQLRequestEntity()
 *     .setUrl("https://graphql.example.com/graphql")
 *     .setQuery("query { user(id: 1) { name email } }")
 *     .addVariable("id", 1);
 *     
 * GraphQLComponent component = new GraphQLComponent();
 * DslHttpSampler sampler = component.process(request);
 * </pre>
 * 
 * @since 1.0
 */
public class GraphQLComponent implements Component<GraphQLRequestEntity, DslHttpSampler> {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLComponent.class);
    
    /**
     * Processes a GraphQL request entity and converts it to a JMeter HTTP sampler.
     * 
     * <p>This method constructs a proper GraphQL request payload including the query
     * and optional variables, converts it to JSON, and configures an HTTP sampler
     * to send it to the specified endpoint.</p>
     * 
     * <p>The GraphQL request is sent as a POST request with Content-Type: application/json,
     * following the standard GraphQL over HTTP protocol.</p>
     * 
     * @param entity The GraphQL request entity to process, must not be null
     * @return A configured JMeter HTTP sampler ready to be added to a test plan
     * @throws RuntimeException If JSON serialization fails or if the entity contains invalid data
     */
    @Override
    public DslHttpSampler process(GraphQLRequestEntity entity) {
        logger.info("Processing GraphQL request for URL: {}", entity.getUrl());
        
        // Build the GraphQL payload with query and optional elements
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", entity.getQuery());
        
        // Add variables if available
        if (entity.getVariables() != null && !entity.getVariables().isEmpty()) {
            payload.put("variables", entity.getVariables());
        }
        
        // Add operationName if available
        if (entity.getOperationName() != null && !entity.getOperationName().isEmpty()) {
            payload.put("operationName", entity.getOperationName());
        }
        
        try {
            // Convert payload to JSON using Jackson ObjectMapper
            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            logger.debug("GraphQL payload: {}", jsonPayload);
            
            // Build HTTP sampler with application/json content-type
            // GraphQL requests are always sent as POST with JSON payload
            DslHttpSampler sampler = httpSampler(entity.getUrl())
                .method("POST")
                .body(jsonPayload)
                .header("Content-Type", "application/json");
            
            // Add all custom headers from the entity
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
        } catch (Exception e) {
            logger.error("Error creating GraphQL request", e);
            throw new RuntimeException("Failed to create GraphQL request: " + e.getMessage(), e);
        }
    }
}
