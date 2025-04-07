package io.perftest.protocol.graphql;

import io.perftest.components.graphql.GraphQLComponent;
import io.perftest.core.test.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.factories.EntityFactory;
import io.perftest.systems.TestSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Test that demonstrates GraphQL API testing
 */
public class GraphQLApiTest extends BaseTest {

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "graphql");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    @DisplayName("Test GraphQL API with simple query")
    public void testGraphQL() throws IOException {
        // Create test system with GraphQL component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(GraphQLRequestEntity.class, new GraphQLComponent());
        
        // Configure the test engine
        TestEngine engine = new TestEngine(testSystem);
        engine.setThreads(1);
        engine.setIterations(1);
        engine.setRampUp(java.time.Duration.ofSeconds(1));
        
        // Set the protocol name for generating the report
        engine.setProtocolName("graphql");
        
        // Create and add GraphQL request using EntityFactory
        GraphQLRequestEntity graphQLRequest = EntityFactory.createGraphQLEntity(
            "https://graphql-demo.mead.io/",
            "query { hello }"
        );
        graphQLRequest.setProperty("name", "GraphQL Hello Query");
        graphQLRequest.setProperty("variables", Map.of("name", "Performance Testing"));
        engine.addRequest(graphQLRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
}
