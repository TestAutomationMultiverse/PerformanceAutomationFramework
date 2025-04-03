package io.perftest.components.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.perftest.core.data.TemplateProcessor;
import io.perftest.entities.request.GraphQLRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;

/**
 * Component for GraphQL operations
 */
public class GraphQLComponent {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLComponent.class);
    private final TemplateProcessor templateProcessor;
    private final ObjectMapper objectMapper;
    
    /**
     * Creates a new GraphQL component
     */
    public GraphQLComponent() {
        this.templateProcessor = new TemplateProcessor();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Creates a JMeter DSL HTTP sampler configured for a GraphQL request
     * @param requestEntity GraphQL request entity
     * @return Configured HTTP sampler for the GraphQL request
     */
    public DslHttpSampler createGraphQLSampler(GraphQLRequestEntity requestEntity) {
        logger.info("Creating GraphQL sampler for URL: {}", requestEntity.getUrl());
        
        // Process query if templating is involved
        String query = requestEntity.getQuery();
        if (requestEntity.getTemplateVars() != null && !requestEntity.getTemplateVars().isEmpty()) {
            query = templateProcessor.processTemplate(query, requestEntity.getTemplateVars());
        }
        
        // Build GraphQL request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(requestEntity.getQueryField(), query);
        
        if (requestEntity.getOperationName() != null && !requestEntity.getOperationName().isEmpty()) {
            requestBody.put(requestEntity.getOperationNameField(), requestEntity.getOperationName());
        }
        
        if (requestEntity.getVariables() != null && !requestEntity.getVariables().isEmpty()) {
            requestBody.put(requestEntity.getVariablesField(), requestEntity.getVariables());
        }
        
        // Convert request body to JSON
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
            logger.info("GraphQL request body: {}", jsonBody);
        } catch (Exception e) {
            logger.error("Error serializing GraphQL request body", e);
            throw new RuntimeException("Error creating GraphQL request", e);
        }
        
        // Create HTTP sampler
        DslHttpSampler sampler = httpSampler(requestEntity.getUrl())
            .method(requestEntity.getMethod())
            .body(jsonBody)
            .followRedirects(requestEntity.getFollowRedirects());
        
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