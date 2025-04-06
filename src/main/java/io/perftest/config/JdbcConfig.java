package io.perftest.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
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
     * Get database connection configuration (defensive copy)
     * @return A copy of the connection configuration
     */
    public ConnectionConfig getConnection() {
        // Return a deep copy of the connection to prevent exposing internal representation
        ConnectionConfig copy = new ConnectionConfig();
        copy.setJdbcUrl(connection.getJdbcUrl());
        copy.setUsername(connection.getUsername());
        copy.setPassword(connection.getPassword());
        copy.setDriverClass(connection.getDriverClass());
        copy.setConnectionPoolSize(connection.getConnectionPoolSize());
        copy.setValidateConnection(connection.isValidateConnection());
        return copy;
    }
    
    /**
     * Set database connection configuration (defensive copy)
     * @param connection The connection configuration to set
     */
    public void setConnection(ConnectionConfig connection) {
        if (connection == null) {
            this.connection = new ConnectionConfig();
            return;
        }
        // Make a defensive copy to avoid storing externally mutable object
        this.connection = new ConnectionConfig();
        this.connection.setJdbcUrl(connection.getJdbcUrl());
        this.connection.setUsername(connection.getUsername());
        this.connection.setPassword(connection.getPassword());
        this.connection.setDriverClass(connection.getDriverClass());
        this.connection.setConnectionPoolSize(connection.getConnectionPoolSize());
        this.connection.setValidateConnection(connection.isValidateConnection());
    }
    
    /**
     * Get list of queries (defensive copy)
     * @return An unmodifiable copy of the query list
     */
    public List<QueryConfig> getQueries() {
        return Collections.unmodifiableList(queries);
    }
    
    /**
     * Set list of queries (defensive copy)
     * @param queries The query list to set
     */
    public void setQueries(List<QueryConfig> queries) {
        if (queries == null) {
            this.queries = new ArrayList<>();
            return;
        }
        // Make a defensive copy to avoid storing externally mutable object
        this.queries = new ArrayList<>(queries);
    }
    
    /**
     * Get test settings (defensive copy)
     * @return A copy of the test settings
     */
    public TestSettings getTestSettings() {
        // Return a deep copy of the test settings to prevent exposing internal representation
        TestSettings copy = new TestSettings();
        copy.setThreads(testSettings.getThreads());
        copy.setIterations(testSettings.isIterations());
        copy.setIterationCount(testSettings.getIterationCount());
        copy.setDuration(testSettings.getDuration());
        copy.setRampUp(testSettings.getRampUp());
        return copy;
    }
    
    /**
     * Set test settings (defensive copy)
     * @param testSettings The test settings to set
     */
    public void setTestSettings(TestSettings testSettings) {
        if (testSettings == null) {
            this.testSettings = new TestSettings();
            return;
        }
        // Make a defensive copy to avoid storing externally mutable object
        this.testSettings = new TestSettings();
        this.testSettings.setThreads(testSettings.getThreads());
        this.testSettings.setIterations(testSettings.isIterations());
        this.testSettings.setIterationCount(testSettings.getIterationCount());
        this.testSettings.setDuration(testSettings.getDuration());
        this.testSettings.setRampUp(testSettings.getRampUp());
    }
    
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
        
        /**
         * Get query parameters (defensive copy)
         * @return An unmodifiable copy of the parameters list
         */
        public List<Object> getParameters() {
            return Collections.unmodifiableList(parameters);
        }
        
        /**
         * Set query parameters (defensive copy)
         * @param parameters The parameters list to set
         */
        public void setParameters(List<Object> parameters) {
            if (parameters == null) {
                this.parameters = new ArrayList<>();
                return;
            }
            // Make a defensive copy to avoid storing externally mutable object
            this.parameters = new ArrayList<>(parameters);
        }
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
