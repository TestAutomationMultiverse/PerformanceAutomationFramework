package io.perftest.entities.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Entity for JDBC database requests
 */
public class JdbcRequestEntity extends RequestEntity {
    private String query;
    private List<Object> queryParams = new ArrayList<>();
    private List<String> queries = new ArrayList<>();
    private Map<String, Object> dataSource = new HashMap<>();
    private int fetchSize = 1000;
    private int queryTimeout = 30;
    private boolean autoCommit = true;
    
    // Additional fields to match what JdbcComponent expects
    private String jdbcUrl;
    private String jdbcDriverClass;
    private String username;
    private String password;
    private String queryType;
    
    /**
     * Default constructor
     */
    public JdbcRequestEntity() {
        super();
    }
    
    /**
     * Constructor with JDBC URL
     * 
     * @param url The JDBC connection URL
     */
    public JdbcRequestEntity(String url) {
        super(url);
        this.jdbcUrl = url;
    }
    
    /**
     * Constructor with JDBC URL and query
     * 
     * @param url The JDBC connection URL
     * @param query The SQL query to execute
     */
    public JdbcRequestEntity(String url, String query) {
        super(url);
        this.jdbcUrl = url;
        this.query = query;
    }
    
    /**
     * Constructor with all JDBC connection parameters
     * 
     * @param url The JDBC connection URL
     * @param driverClass The JDBC driver class name
     * @param username The database username
     * @param password The database password
     */
    public JdbcRequestEntity(String url, String driverClass, String username, String password) {
        super(url);
        this.jdbcUrl = url;
        this.jdbcDriverClass = driverClass;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Create a new builder instance for this entity
     * 
     * @return A new builder instance
     */
    public static JdbcRequestEntityBuilder builder() {
        return new JdbcRequestEntityBuilder();
    }
    
    /**
     * Builder for JdbcRequestEntity
     */
    public static class JdbcRequestEntityBuilder {
        private JdbcRequestEntity entity = new JdbcRequestEntity();
        
        private JdbcRequestEntityBuilder() {
        }
        
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
        
        public JdbcRequestEntityBuilder queryType(String queryType) {
            entity.setQueryType(queryType);
            return this;
        }
        
        public JdbcRequestEntityBuilder addQueryParam(Object param) {
            entity.addQueryParam(param);
            return this;
        }
        
        public JdbcRequestEntityBuilder queryParams(List<Object> params) {
            entity.setQueryParams(params);
            return this;
        }
        
        public JdbcRequestEntityBuilder addQuery(String query) {
            entity.addQuery(query);
            return this;
        }
        
        public JdbcRequestEntityBuilder fetchSize(int fetchSize) {
            entity.setFetchSize(fetchSize);
            return this;
        }
        
        public JdbcRequestEntityBuilder queryTimeout(int timeout) {
            entity.setQueryTimeout(timeout);
            return this;
        }
        
        public JdbcRequestEntityBuilder autoCommit(boolean autoCommit) {
            entity.setAutoCommit(autoCommit);
            return this;
        }
        
        public JdbcRequestEntityBuilder name(String name) {
            entity.setProperty("name", name);
            return this;
        }
        
        /**
         * Build the JdbcRequestEntity
         * 
         * @return A new JdbcRequestEntity instance
         */
        public JdbcRequestEntity build() {
            // Create a defensive copy to prevent exposing internal representation
            JdbcRequestEntity result = new JdbcRequestEntity();
            
            // Copy all properties
            result.setUrl(entity.getUrl());
            result.setJdbcUrl(entity.getJdbcUrl());
            result.setJdbcDriverClass(entity.getJdbcDriverClass());
            result.setUsername(entity.getUsername());
            result.setPassword(entity.getPassword());
            result.setQuery(entity.getQuery());
            result.setQueries(entity.getQueries());
            result.setQueryParams(entity.getQueryParams());
            result.setFetchSize(entity.getFetchSize());
            result.setQueryTimeout(entity.getQueryTimeout());
            result.setAutoCommit(entity.isAutoCommit());
            result.setQueryType(entity.getQueryType());
            
            // Copy properties from base class
            Map<String, Object> properties = entity.getProperties();
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    result.setProperty(entry.getKey(), entry.getValue());
                }
            }
            
            return result;
        }
    }
    
    /**
     * Add a query parameter
     * 
     * @param param The parameter to add
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity addQueryParam(Object param) {
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<>();
        }
        this.queryParams.add(param);
        return this;
    }
    
    /**
     * Add multiple query parameters
     * 
     * @param params The parameters to add
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity addQueryParams(List<Object> params) {
        if (params == null) {
            return this;
        }
        
        if (this.queryParams == null) {
            this.queryParams = new ArrayList<>();
        }
        
        this.queryParams.addAll(params);
        return this;
    }
    
    /**
     * Add a SQL query to the list of queries to execute
     * 
     * @param query The SQL query to add
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity addQuery(String query) {
        if (this.queries == null) {
            this.queries = new ArrayList<>();
        }
        this.queries.add(query);
        return this;
    }
    
    /**
     * Configure a data source property
     * 
     * @param key The data source property name
     * @param value The data source property value
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity dataSourceProperty(String key, Object value) {
        if (this.dataSource == null) {
            this.dataSource = new HashMap<>();
        }
        this.dataSource.put(key, value);
        return this;
    }
    
    /**
     * Set the database driver class name
     * 
     * @param driverClass The driver class name
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity driver(String driverClass) {
        this.jdbcDriverClass = driverClass;
        return dataSourceProperty("driver", driverClass);
    }
    
    /**
     * Set the JDBC username
     * 
     * @param username The username
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity username(String username) {
        this.username = username;
        return dataSourceProperty("username", username);
    }
    
    /**
     * Set the JDBC password
     * 
     * @param password The password
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity password(String password) {
        this.password = password;
        return dataSourceProperty("password", password);
    }
    
    /**
     * Set the JDBC connection URL and update the base URL
     * 
     * @param url The JDBC connection URL
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity jdbcUrl(String url) {
        super.setUrl(url);
        this.jdbcUrl = url;
        return this;
    }
    
    /**
     * Set the fetch size for result sets
     * 
     * @param fetchSize The fetch size
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity fetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }
    
    /**
     * Set the query timeout in seconds
     * 
     * @param queryTimeout The query timeout
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity queryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
        return this;
    }
    
    /**
     * Set the auto-commit mode
     * 
     * @param autoCommit Whether auto-commit should be enabled
     * @return This entity instance for method chaining
     */
    public JdbcRequestEntity autoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }
    
    /**
     * @return The SQL query
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
     * @return Parameters for the SQL query with defensive copy to prevent external modification
     */
    public List<Object> getQueryParams() {
        if (queryParams == null) return null;
        return Collections.unmodifiableList(queryParams);
    }
    
    /**
     * @param queryParams Parameters for the SQL query
     */
    public void setQueryParams(List<Object> queryParams) {
        if (queryParams == null) {
            this.queryParams = null;
        } else {
            this.queryParams = new ArrayList<>(queryParams);
        }
    }
    
    /**
     * @return List of SQL queries to execute with defensive copy to prevent external modification
     */
    public List<String> getQueries() {
        if (queries == null) return null;
        return Collections.unmodifiableList(queries);
    }
    
    /**
     * @param queries List of SQL queries to execute
     */
    public void setQueries(List<String> queries) {
        if (queries == null) {
            this.queries = null;
        } else {
            this.queries = new ArrayList<>(queries);
        }
    }
    
    /**
     * @return Data source properties with defensive copy to prevent external modification
     */
    public Map<String, Object> getDataSource() {
        if (dataSource == null) return null;
        return Collections.unmodifiableMap(dataSource);
    }
    
    /**
     * @param dataSource Data source properties
     */
    public void setDataSource(Map<String, Object> dataSource) {
        if (dataSource == null) {
            this.dataSource = null;
        } else {
            this.dataSource = new HashMap<>(dataSource);
        }
    }
    
    /**
     * @return Fetch size for result sets
     */
    public int getFetchSize() {
        return fetchSize;
    }
    
    /**
     * @param fetchSize Fetch size for result sets
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
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
    
    /**
     * @return Whether auto-commit is enabled
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }
    
    /**
     * @param autoCommit Whether auto-commit should be enabled
     */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
    
    /**
     * @return The JDBC connection URL
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    /**
     * @param jdbcUrl The JDBC connection URL to set
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        super.setUrl(jdbcUrl);  // Keep the base URL in sync
    }
    
    /**
     * Overrides parent method to set both URL and JDBC URL 
     * 
     * @param url The URL to set
     */
    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        this.jdbcUrl = url;
    }
    
    /**
     * @return The JDBC driver class name
     */
    public String getJdbcDriverClass() {
        return jdbcDriverClass;
    }
    
    /**
     * @param jdbcDriverClass The JDBC driver class name to set
     */
    public void setJdbcDriverClass(String jdbcDriverClass) {
        this.jdbcDriverClass = jdbcDriverClass;
    }
    
    /**
     * @return The database username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * @param username The database username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * @return The database password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * @param password The database password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * @return The query type (e.g., SELECT, UPDATE, INSERT)
     */
    public String getQueryType() {
        return queryType;
    }
    
    /**
     * @param queryType The query type to set
     */
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JdbcRequestEntity that = (JdbcRequestEntity) o;
        return fetchSize == that.fetchSize &&
               queryTimeout == that.queryTimeout &&
               autoCommit == that.autoCommit &&
               Objects.equals(query, that.query) &&
               Objects.equals(queryParams, that.queryParams) &&
               Objects.equals(queries, that.queries) &&
               Objects.equals(dataSource, that.dataSource) &&
               Objects.equals(jdbcUrl, that.jdbcUrl) &&
               Objects.equals(jdbcDriverClass, that.jdbcDriverClass) &&
               Objects.equals(username, that.username) &&
               Objects.equals(password, that.password) &&
               Objects.equals(queryType, that.queryType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), query, queryParams, queries, 
                            dataSource, fetchSize, queryTimeout, autoCommit,
                            jdbcUrl, jdbcDriverClass, username, password, queryType);
    }
}
