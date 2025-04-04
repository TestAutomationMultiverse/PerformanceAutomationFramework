package io.perftest.examples;

import io.perftest.components.graphql.GraphQLComponent;
import io.perftest.core.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.systems.TestSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Example test that uses GraphQL API
 */
public class GraphQLApiTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLApiTest.class);

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "graphql");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testGraphQLApi() throws IOException {
        // Create test system with GraphQL component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(GraphQLRequestEntity.class, new GraphQLComponent());
        
        // Configure the test engine
        TestEngine engine = new TestEngine(testSystem);
        engine.setThreads(1);
        engine.setIterations(2);
        engine.setRampUp(java.time.Duration.ofSeconds(1));
        
        // Set the protocol name to 'graphql' for generating the report
        engine.setProtocolName("graphql");
        
        // Update the lastProtocolUsed in BaseTest to match the protocol used in the TestEngine
        setLastProtocolUsed("graphql");
        
        // Create and add GraphQL request
        GraphQLRequestEntity graphQLRequest = new GraphQLRequestEntity("https://graphql-demo.mead.io/");
        graphQLRequest.setName("GraphQL Demo Request");
        graphQLRequest.setQuery("query { hello }");
        engine.addRequest(graphQLRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
}
