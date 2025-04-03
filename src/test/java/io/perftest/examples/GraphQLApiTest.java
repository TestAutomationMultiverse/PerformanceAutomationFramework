package io.perftest.examples;

import io.perftest.core.engine.TestEngine;
import io.perftest.entities.request.GraphQLRequestEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for GraphQL API testing
 * This example uses the public Countries GraphQL API
 */
public class GraphQLApiTest {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLApiTest.class);
    private static TestEngine testEngine;
    
    @BeforeAll
    public static void setup() {
        testEngine = new TestEngine();
    }
    
    @Test
    public void testCountriesGraphQL() throws IOException {
        logger.info("Starting testCountriesGraphQL test");
        
        // Create a GraphQL request entity
        GraphQLRequestEntity requestEntity = new GraphQLRequestEntity("https://countries.trevorblades.com");
        requestEntity.setName("Get European Countries");
        
        String query = "query GetEuropeanCountries {\n" +
            "  continent(code: \"EU\") {\n" +
            "    name\n" +
            "    countries {\n" +
            "      name\n" +
            "      capital\n" +
            "      currency\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        requestEntity.setQuery(query);
        
        // Add assertions
        requestEntity.addAssertion("$.data.continent.name", "Europe");
        requestEntity.addAssertion("$.data.continent.countries[0].name", "*");
        
        // Execute test
        TestPlanStats stats = testEngine.executeGraphQLTest(requestEntity, 1, 1);
        
        // Log results
        logger.info("Test completed with average time: {}ms", stats.overall().sampleTime().mean());
        logger.info("Error count: {}", stats.overall().errorsCount());
    }
    
    @Test
    public void testCountriesWithVariables() throws IOException {
        logger.info("Starting testCountriesWithVariables test");
        
        // Create a GraphQL request entity
        GraphQLRequestEntity requestEntity = new GraphQLRequestEntity("https://countries.trevorblades.com");
        requestEntity.setName("Get Continent by Code");
        
        // Query with variable
        String query = "query GetContinent($code: ID!) {\n" +
            "  continent(code: $code) {\n" +
            "    name\n" +
            "    countries {\n" +
            "      name\n" +
            "      capital\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        requestEntity.setQuery(query);
        
        // Set variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("code", "AS");  // Asia
        requestEntity.setVariables(variables);
        
        // Add assertions
        requestEntity.addAssertion("$.data.continent.name", "Asia");
        
        // Execute test
        TestPlanStats stats = testEngine.executeGraphQLTest(requestEntity, 1, 1);
        
        // Log results
        logger.info("Test completed with average time: {}ms", stats.overall().sampleTime().mean());
        logger.info("Error count: {}", stats.overall().errorsCount());
    }
}