package io.perftest.entities.request;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a JDBC database request
 */
public class JdbcRequestEntity extends RequestEntity {
    private String jdbcUrl;
    private String jdbcDriverClass;
    private String username;
    private String password;
    private String query;
    private String queryType; // SELECT, UPDATE, etc.
    private int queryTimeout = 30; // Default timeout in seconds
    private List<Object> queryParams = new ArrayList<>();
    private List<String> queries = new ArrayList<>();
    
    /**
     * Default constructor
     */
    public JdbcRequestEntity() {
    }
    
    /**
     * Constructor with common parameters
     * 
     * @param jdbcUrl JDBC URL for the database
     * @param username Database username
     * @param password Database password
     * @param jdbcDriverClass JDBC driver class name
     */
    public JdbcRequestEntity(String jdbcUrl, String username, String password, String jdbcDriverClass) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.jdbcDriverClass = jdbcDriverClass;
    }
    
    /**
     * Add a query to the list of queries to execute
     * 
     * @param query SQL query string
     */
    public void addQuery(String query) {
        this.queries.add(query);
    }
    
    /**
     * Get all queries to execute
     * 
     * @return List of SQL queries
     */
    public List<String> getQueries() {
        return queries;
    }
    
    /**
     * Set all queries to execute
     * 
     * @param queries List of SQL queries
     */
    public void setQueries(List<String> queries) {
        this.queries = queries;
    }
    
    /**
     * Create a new builder for JdbcRequestEntity
     * @return A new builder instance
     */
    public static JdbcRequestEntityBuilder builder() {
        return new JdbcRequestEntityBuilder();
    }
    
    /**
     * Builder class for JdbcRequestEntity
     */
    public static class JdbcRequestEntityBuilder {
        private final JdbcRequestEntity entity = new JdbcRequestEntity();
        
        public JdbcRequestEntityBuilder jdbcUrl(String jdbcUrl) {
            entity.setJdbcUrl(jdbcUrl);
            return this;
        }
        
        public JdbcRequestEntityBuilder driverClass(String driverClass) {
            entity.setJdbcDriverClass(driverClass);
            return this;
        }
        
        public JdbcRequestEntityBuilder username(String username) {
            entity.setUsername(username);
            return this;
        }
        
        public JdbcRequestEntityBuilder password(String password) {
            entity.setPassword(password);
            return this;
        }
        
        public JdbcRequestEntityBuilder query(String query) {
            entity.setQuery(query);
            return this;
        }
        
        public JdbcRequestEntityBuilder queryParams(List<Object> queryParams) {
            entity.setQueryParams(queryParams);
            return this;
        }
        
        public JdbcRequestEntityBuilder addQuery(String query) {
            entity.addQuery(query);
            return this;
        }
        
        public JdbcRequestEntity build() {
            return entity;
        }
    }
    
    /**
     * @return JDBC URL for database connection
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    /**
     * @param jdbcUrl JDBC URL for database connection
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
    
    /**
     * @return JDBC driver class name
     */
    public String getJdbcDriverClass() {
        return jdbcDriverClass;
    }
    
    /**
     * @param jdbcDriverClass JDBC driver class name
     */
    public void setJdbcDriverClass(String jdbcDriverClass) {
        this.jdbcDriverClass = jdbcDriverClass;
    }
    
    /**
     * @return Database username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * @param username Database username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * @return Database password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * @param password Database password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @return SQL query to execute
     */
    public String getQuery() {
        return query;
    }
    
    /**
     * @param query SQL query to execute
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * @return Parameters for the SQL query
     */
    public List<Object> getQueryParams() {
        return queryParams;
    }
    
    /**
     * @param queryParams Parameters for the SQL query
     */
    public void setQueryParams(List<Object> queryParams) {
        this.queryParams = queryParams;
    }
    
    /**
     * @return Query type (SELECT, UPDATE, etc.)
     */
    public String getQueryType() {
        return queryType;
    }
    
    /**
     * @param queryType Query type (SELECT, UPDATE, etc.)
     */
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
    
    /**
     * @return Query timeout in seconds
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }
    
    /**
     * @param queryTimeout Query timeout in seconds
     */
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
}
