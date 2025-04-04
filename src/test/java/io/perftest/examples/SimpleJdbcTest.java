package io.perftest.examples;

import io.perftest.components.jdbc.JdbcComponent;
import io.perftest.core.BaseTest;
import io.perftest.engine.TestEngine;
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
import java.time.Duration;

/**
 * Example of a simple database-agnostic JDBC test
 * This test can be configured to work with any database by setting the appropriate driver and connection parameters
 */
public class SimpleJdbcTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleJdbcTest.class);

    // These values can be overridden through system properties
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver"; // Default driver (can be changed)
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/testdb"; // Default URL (can be changed)
    private static final String DEFAULT_USERNAME = "root"; // Default username (can be changed)
    private static final String DEFAULT_PASSWORD = "password"; // Default password (can be changed)
    private static final String DEFAULT_QUERY = "SELECT 1 AS test_column"; // Works with most databases

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "jdbc");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testGenericJdbcQuery() throws IOException {
        // Get configuration from system properties or use defaults
        String driver = System.getProperty("jdbc.driver", DEFAULT_DRIVER);
        String url = System.getProperty("jdbc.url", DEFAULT_URL);
        String username = System.getProperty("jdbc.username", DEFAULT_USERNAME);
        String password = System.getProperty("jdbc.password", DEFAULT_PASSWORD);
        String query = System.getProperty("jdbc.query", DEFAULT_QUERY);
        
        logger.info("Running JDBC test with driver: {}", driver);
        logger.info("Connection URL: {}", url);
        
        // Create test system with JDBC component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(JdbcRequestEntity.class, new JdbcComponent());
        
        // Configure the test engine
        TestEngine engine = new TestEngine(testSystem);
        engine.setThreads(1);
        engine.setIterations(1);
        engine.setRampUp(Duration.ofSeconds(1));
        
        // Set the protocol name to 'jdbc' for generating the report
        engine.setProtocolName("jdbc");
        
        // Create JDBC request entity
        JdbcRequestEntity jdbcRequest = JdbcRequestEntity.builder()
                .driverClass(driver)
                .jdbcUrl(url)
                .username(username)
                .password(password)
                .query(query)
                .build();
        
        jdbcRequest.setName("Generic JDBC Query");
        jdbcRequest.setQueryType("SELECT");
        jdbcRequest.setQueryTimeout(30);
        
        // Add the request to the engine
        engine.addRequest(jdbcRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
}
