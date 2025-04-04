package io.perftest.examples;

import io.perftest.components.jdbc.JdbcComponent;
import io.perftest.core.BaseTest;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.JdbcRequestEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Example test that loads JDBC test configuration from YAML
 */
public class JdbcYamlConfigTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(JdbcYamlConfigTest.class);
    private static final String CONFIG_FILE = "jdbc-config.yml";

    @BeforeEach
    public void setup() throws IOException {
        // Create the HTML reports directory if it doesn't exist
        Path htmlReportDir = Paths.get("target", "html-reports", "jdbc");
        Files.createDirectories(htmlReportDir);
    }

    @Test
    public void testJdbcWithYamlConfig() throws IOException {
        // Load configuration from YAML file
        Map<String, Object> config = YamlConfigLoader.loadConfig(CONFIG_FILE);
        
        // Create test system with JDBC component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(JdbcRequestEntity.class, new JdbcComponent());
        
        // Configure test engine from YAML
        TestEngine engine = configureTestEngine(testSystem, config);
        
        // Set the protocol name to 'jdbc' for generating the report
        engine.setProtocolName("jdbc");
        setLastProtocolUsed("jdbc");
        
        // Create and configure JDBC request entity from YAML
        JdbcRequestEntity jdbcRequest = createJdbcRequestEntity(config);
        engine.addRequest(jdbcRequest);
        
        // Run the test
        TestPlanStats stats = engine.run();
        
        // Log test results
        logTestResults(stats);
    }
    
    @SuppressWarnings("unchecked")
    private JdbcRequestEntity createJdbcRequestEntity(Map<String, Object> config) {
        // Get database configuration
        Map<String, Object> dbConfig = (Map<String, Object>) config.getOrDefault("database", Map.of());
        
        // Get request configuration
        Map<String, Object> requestConfig = (Map<String, Object>) config.getOrDefault("request", Map.of());
        
        // Extract connection details
        String driver = (String) dbConfig.getOrDefault("driver", "org.postgresql.Driver");
        String url = (String) dbConfig.getOrDefault("url", "jdbc:postgresql://localhost:5432/testdb");
        String username = (String) dbConfig.getOrDefault("username", "postgres");
        String password = (String) dbConfig.getOrDefault("password", "postgres");
        
        // Extract request details
        String name = (String) requestConfig.getOrDefault("name", "JDBC Request");
        String query = (String) requestConfig.getOrDefault("query", "SELECT 1");
        
        // Create JDBC entity using builder to ensure correct parameter order
        JdbcRequestEntity entity = JdbcRequestEntity.builder()
                .jdbcUrl(url)
                .driverClass(driver)
                .username(username)
                .password(password)
                .query(query)
                .build();
                
        entity.setName(name);
        
        logger.info("Created JDBC request with query: {}", query);
        
        return entity;
    }
}
