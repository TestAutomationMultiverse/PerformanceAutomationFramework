package io.perftest.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for JDBC database performance testing
 */
@Data
@NoArgsConstructor
public class JdbcConfig {
    
    /**
     * Database connection configuration
     */
    private ConnectionConfig connection = new ConnectionConfig();
    
    /**
     * List of queries to test
     */
    private List<QueryConfig> queries = new ArrayList<>();
    
    /**
     * Test execution settings
     */
    private TestSettings testSettings = new TestSettings();
    
    /**
     * Configuration for database connection
     */
    @Data
    @NoArgsConstructor
    public static class ConnectionConfig {
        private String jdbcUrl;
        private String username;
        private String password;
        private String driverClass;
        private int connectionPoolSize = 10;
        private boolean validateConnection = true;
    }
    
    /**
     * Configuration for a single database query
     */
    @Data
    @NoArgsConstructor
    public static class QueryConfig {
        private String name;
        private String sql;
        private Duration timeout;
        private List<Object> parameters = new ArrayList<>();
    }
    
    /**
     * Test execution settings
     */
    @Data
    @NoArgsConstructor
    public static class TestSettings {
        private int threads = 1;
        private boolean iterations = true;
        private int iterationCount = 10;
        private Duration duration = Duration.ofMinutes(1);
        private Duration rampUp;
    }
}
