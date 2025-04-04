package io.perftest.examples;

import io.perftest.components.graphql.GraphQLComponent;
import io.perftest.core.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.GraphQLRequestEntity;
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
 * Example test that loads GraphQL test configuration from YAML
 */
public class GraphQLYamlConfigTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLYamlConfigTest.class);
    private static final String CONFIG_FILE = "graphql-config.yml";

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "graphql");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testGraphQLWithYamlConfig() throws IOException {
        // Load configuration from YAML file
        Map<String, Object> config = YamlConfigLoader.loadConfig(CONFIG_FILE);
        
        // Create test system with GraphQL component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(GraphQLRequestEntity.class, new GraphQLComponent());
        
        // Configure test engine from YAML
        TestEngine engine = configureTestEngine(testSystem, config);
        
        // Set the protocol name to 'graphql' for generating the report
        engine.setProtocolName("graphql");
        
        // Create and configure GraphQL request entity from YAML
        GraphQLRequestEntity graphQLRequest = createGraphQLRequestEntity(config);
        engine.addRequest(graphQLRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
    
    private GraphQLRequestEntity createGraphQLRequestEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = (Map<String, Object>) config.getOrDefault("request", Map.of());
        String name = (String) requestConfig.getOrDefault("name", "GraphQL Request");
        String endpoint = (String) requestConfig.getOrDefault("endpoint", "https://example.com/graphql");
        
        // Try to load query from template first, if not found use the inline query from config
        String query = null;
        String queryPath = YamlConfigLoader.getTemplatePath(config, "queryPath");
        if (queryPath != null) {
            try {
                query = YamlConfigLoader.loadTemplateContent(queryPath);
                logger.info("Loaded GraphQL query from template: {}", queryPath);
            } catch (IOException e) {
                logger.warn("Failed to load GraphQL query template, falling back to inline query: {}", e.getMessage());
            }
        }
        
        // If template loading failed, use inline query from config
        if (query == null) {
            query = (String) requestConfig.getOrDefault("query", "query { example }");
        }
        
        // Create entity with the URL and then set the other properties
        GraphQLRequestEntity entity = new GraphQLRequestEntity(endpoint);
        entity.setName(name);
        entity.setQuery(query);
        
        // Set variables if present
        if (requestConfig.containsKey("variables")) {
            String variables = (String) requestConfig.get("variables");
            entity.setVariables(variables);
        }
        
        // Try to load headers from template first, if not found use the inline headers from config
        String headersPath = YamlConfigLoader.getTemplatePath(config, "headersPath");
        if (headersPath != null) {
            try {
                String headersJson = YamlConfigLoader.loadTemplateContent(headersPath);
                // In a real implementation, you would parse the JSON and add headers to the entity
                logger.info("Loaded headers from template: {}", headersPath);
                // This is just a placeholder - in a real implementation you would need to parse the JSON
                entity.addHeader("Content-Type", "application/json");
            } catch (IOException e) {
                logger.warn("Failed to load headers template, falling back to inline headers: {}", e.getMessage());
            }
        } else if (requestConfig.containsKey("headers")) {
            // Set headers from inline config
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) requestConfig.get("headers");
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                entity.addHeader(header.getKey(), header.getValue().toString());
            }
        }
        
        return entity;
    }
}
