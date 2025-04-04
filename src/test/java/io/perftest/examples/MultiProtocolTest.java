package io.perftest.examples;

import io.perftest.components.graphql.GraphQLComponent;
import io.perftest.components.http.HttpComponent;
import io.perftest.components.jdbc.JdbcComponent;
import io.perftest.core.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.JdbcRequestEntity;
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
 * Example test that uses multiple protocols in a single test
 */
public class MultiProtocolTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(MultiProtocolTest.class);

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "multi-protocol");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testMultipleProtocols() throws IOException {
        // Create test system with all components
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(HttpRequestEntity.class, new HttpComponent());
        testSystem.addComponent(GraphQLRequestEntity.class, new GraphQLComponent());
        testSystem.addComponent(JdbcRequestEntity.class, new JdbcComponent());
        
        // Configure the test engine
        TestEngine engine = new TestEngine(testSystem);
        engine.setThreads(1);
        engine.setIterations(1);
        engine.setRampUp(java.time.Duration.ofSeconds(1));
        
        // Set the protocol name to 'multi-protocol' for generating the report
        engine.setProtocolName("multi-protocol");
        
        // Create and add HTTP request
        HttpRequestEntity httpRequest = new HttpRequestEntity("https://test-api.k6.io/public/crocodiles/");
        httpRequest.setName("HTTP Request");
        httpRequest.setMethod("GET");
        engine.addRequest(httpRequest);
        
        // Create and add GraphQL request
        GraphQLRequestEntity graphQLRequest = new GraphQLRequestEntity("https://graphql-demo.mead.io/");
        graphQLRequest.setName("GraphQL Request");
        graphQLRequest.setQuery("query { hello }");
        engine.addRequest(graphQLRequest);
        
        // Create and add JDBC request
        JdbcRequestEntity jdbcRequest = JdbcRequestEntity.builder()
                .jdbcUrl("jdbc:postgresql://localhost:5432/testdb")
                .driverClass("org.postgresql.Driver")
                .username("postgres")
                .password("postgres")
                .query("SELECT * FROM users LIMIT 10")
                .build();
        jdbcRequest.setName("JDBC Request");
        engine.addRequest(jdbcRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
}
