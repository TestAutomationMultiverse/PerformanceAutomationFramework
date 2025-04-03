package io.perftest.examples;

import io.perftest.core.BaseTest;
import io.perftest.core.util.YamlConfigLoader;
import io.perftest.entities.request.GraphQLRequestEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Example of GraphQL tests configured via YAML files
 */
public class GraphQLYamlConfigTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLYamlConfigTest.class);

    /**
     * Test GraphQL requests defined in YAML configuration
     */
    @Test
    public void testGraphQLFromYamlConfig() throws Exception {
        logger.info("Starting test: testGraphQLFromYamlConfig");
        
        // Get default test parameters from config
        Map<String, Object> defaults = getProtocolDefaults("graphql");
        int threads = getIntValue(defaults.getOrDefault("threads", 1));
        int iterations = getIntValue(defaults.getOrDefault("iterations", 1));
        
        // Load GraphQL request configurations from YAML
        List<Map<String, Object>> graphqlConfigs = YamlConfigLoader.loadYamlDocuments("graphql/k6-graphql-test.yml");
        logger.info("Loaded {} GraphQL test configurations from YAML", graphqlConfigs.size());
        
        // Execute each GraphQL request
        for (Map<String, Object> config : graphqlConfigs) {
            // Create GraphQL request entity from configuration
            GraphQLRequestEntity graphqlRequest = createGraphQLRequestFromConfig(config);
            
            // Execute the GraphQL test
            logger.info("Executing GraphQL test: {}", config.get("name"));
            executeGraphQLTest(graphqlRequest, threads, iterations);
        }
    }
    
    /**
     * Create a GraphQLRequestEntity from a YAML configuration map
     * @param config YAML configuration map
     * @return GraphQLRequestEntity configured from YAML
     */
    private GraphQLRequestEntity createGraphQLRequestFromConfig(Map<String, Object> config) {
        // Create GraphQL request entity with URL
        GraphQLRequestEntity graphqlRequest = new GraphQLRequestEntity((String) config.get("url"));
        
        // Set name
        if (config.containsKey("name")) {
            graphqlRequest.setName((String) config.get("name"));
        }
        
        // Set query
        if (config.containsKey("query")) {
            graphqlRequest.setQuery((String) config.get("query"));
        }
        
        // Set operation name
        if (config.containsKey("operationName")) {
            graphqlRequest.setOperationName((String) config.get("operationName"));
        }
        
        // Set expected status
        if (config.containsKey("expectedStatus")) {
            int status = ((Integer) config.get("expectedStatus")).intValue();
            graphqlRequest.setExpectedStatus(status);
        }
        
        // Add headers if defined
        if (config.containsKey("headers")) {
            Map<String, String> headers = (Map<String, String>) config.get("headers");
            for (Map.Entry<String, String> header : headers.entrySet()) {
                graphqlRequest.addHeader(header.getKey(), header.getValue());
            }
        }
        
        // Add GraphQL variables
        if (config.containsKey("graphQLVariables")) {
            Map<String, Object> graphQLVariables = (Map<String, Object>) config.get("graphQLVariables");
            for (Map.Entry<String, Object> var : graphQLVariables.entrySet()) {
                graphqlRequest.addGraphQLVariable(var.getKey(), var.getValue());
            }
        }
        
        // Add template variables if defined
        if (config.containsKey("variables")) {
            Map<String, Object> variables = (Map<String, Object>) config.get("variables");
            for (Map.Entry<String, Object> var : variables.entrySet()) {
                graphqlRequest.addVariable(var.getKey(), var.getValue());
            }
        }
        
        // Add assertions
        if (config.containsKey("assertions")) {
            Map<String, String> assertions = (Map<String, String>) config.get("assertions");
            for (Map.Entry<String, String> assertion : assertions.entrySet()) {
                graphqlRequest.addAssertion(assertion.getKey(), assertion.getValue());
            }
        }
        
        return graphqlRequest;
    }
}