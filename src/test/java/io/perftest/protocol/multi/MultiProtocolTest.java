package io.perftest.protocol.multi;

import io.perftest.components.graphql.GraphQLComponent;
import io.perftest.components.http.HttpComponent;
import io.perftest.components.jdbc.JdbcComponent;
import io.perftest.core.test.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.JdbcRequestEntity;
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

/**
 * Test that demonstrates using multiple protocols in a single test
 */
public class MultiProtocolTest extends BaseTest {

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "multi-protocol");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    @DisplayName("Run test with HTTP and GraphQL protocols in one test")
    public void testMultipleProtocols() throws IOException {
        // Create test system with all components
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(HttpRequestEntity.class, new HttpComponent());
        testSystem.addComponent(GraphQLRequestEntity.class, new GraphQLComponent());
        // Not using JDBC in this test
        // testSystem.addComponent(JdbcRequestEntity.class, new JdbcComponent());
        
        // Configure the test engine
        TestEngine engine = new TestEngine(testSystem);
        engine.setThreads(1);
        engine.setIterations(1);
        engine.setRampUp(java.time.Duration.ofSeconds(1));
        
        // Set the protocol name to 'multi-protocol' for generating the report
        engine.setProtocolName("multi-protocol");
        
        // Create and add HTTP request using EntityFactory
        HttpRequestEntity httpRequest = EntityFactory.createHttpEntity(
            "https://test-api.k6.io/public/crocodiles/", 
            "GET"
        );
        httpRequest.setName("HTTP Request");
        engine.addRequest(httpRequest);
        
        // Create and add GraphQL request using EntityFactory
        GraphQLRequestEntity graphQLRequest = EntityFactory.createGraphQLEntity(
            "https://graphql-demo.mead.io/",
            "query { hello }"
        );
        graphQLRequest.setProperty("name", "GraphQL Request");
        engine.addRequest(graphQLRequest);
        
        // Skip JDBC for now to avoid database setup issues
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
}
