package io.perftest.graphql_tests;

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
 * GraphQL test for the Countries API
 */
public class CountriesApiTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(CountriesApiTest.class);
    private static final String CONFIG_FILE = "graphql_tests/countries-config.yml";

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "countries-api");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testCountriesApi() throws IOException {
        // Load configuration from YAML file
        Map<String, Object> config = YamlConfigLoader.loadConfig(CONFIG_FILE);
        
        // Create test system with GraphQL component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(GraphQLRequestEntity.class, new GraphQLComponent());
        
        // Configure test engine from YAML
        TestEngine engine = configureTestEngine(testSystem, config);
        
        // Set the specific protocol and test name for reporting
        engine.setProtocolName("countries-api");
        
        // Create GraphQL request entity from templates or inline config
        GraphQLRequestEntity graphQLRequest = createGraphQLRequestEntity(config);
        engine.addRequest(graphQLRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
    
    private GraphQLRequestEntity createGraphQLRequestEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = YamlConfigLoader.getMap(config, "request");
        String name = YamlConfigLoader.getString(requestConfig, "name", "Countries API Test");
        String endpoint = YamlConfigLoader.getString(requestConfig, "endpoint", "https://countries.trevorblades.com/");
        
        // Try to load query from template first
        String query = null;
        String queryPath = YamlConfigLoader.getTemplatePath(config, "queryPath");
        if (queryPath != null) {
            try {
                query = YamlConfigLoader.loadTemplateContent(queryPath);
                logger.info("Loaded GraphQL query from template: {}", queryPath);
            } catch (IOException e) {
                logger.warn("Failed to load GraphQL query template: {}", e.getMessage());
            }
        }
        
        // If template loading failed, use inline query from config
        if (query == null) {
            // Look for both 'query' and 'graphqlQuery' in the config to handle both formats
            query = YamlConfigLoader.getString(requestConfig, "graphqlQuery", null);
            if (query == null) {
                query = YamlConfigLoader.getString(requestConfig, "query", "query { countries { code name } }");
            }
        }
        
        // Create entity with the URL and set the query
        GraphQLRequestEntity entity = new GraphQLRequestEntity(endpoint);
        entity.setName(name);
        entity.setQuery(query);
        
        // Try to load headers from template
        String headersPath = YamlConfigLoader.getTemplatePath(config, "headersPath");
        if (headersPath != null) {
            try {
                // In a real implementation, you would parse the JSON headers template
                // For now, we'll just add a default Content-Type header
                entity.addHeader("Content-Type", "application/json");
                logger.info("Processed headers from template: {}", headersPath);
            } catch (Exception e) {
                logger.warn("Failed to process headers template: {}", e.getMessage());
            }
        } else if (requestConfig.containsKey("headers")) {
            // Use inline headers from config
            Map<String, Object> headers = YamlConfigLoader.getMap(requestConfig, "headers");
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                entity.addHeader(header.getKey(), header.getValue().toString());
            }
        } else {
            // Add default header if none specified
            entity.addHeader("Content-Type", "application/json");
        }
        
        return entity;
    }
}
