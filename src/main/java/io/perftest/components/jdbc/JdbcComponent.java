package io.perftest.components.jdbc;

import io.perftest.components.core.Component;
import io.perftest.entities.request.JdbcRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.jdbc.DslJdbcSampler;
import us.abstracta.jmeter.javadsl.jdbc.DslJdbcConnectionPool;

import java.sql.Driver;
import java.util.List;

import static us.abstracta.jmeter.javadsl.jdbc.JdbcJmeterDsl.*;

/**
 * JDBC Component implementation for processing database request entities.
 * 
 * <p>This component transforms {@link JdbcRequestEntity} objects into JMeter JDBC samplers
 * that can execute SQL queries against databases. It handles database connections, 
 * query execution, and parameter binding for prepared statements.</p>
 * 
 * <p>The component creates a unique connection pool for each request entity and configures
 * it with the appropriate driver class, connection URL, credentials, and other settings.
 * It then creates a JDBC sampler that uses this connection pool to execute the query.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * JdbcRequestEntity request = new JdbcRequestEntity()
 *     .setJdbcUrl("jdbc:postgresql://localhost:5432/testdb")
 *     .setJdbcDriverClass("org.postgresql.Driver")
 *     .setUsername("user")
 *     .setPassword("password")
 *     .setQuery("SELECT * FROM users WHERE id = ?")
 *     .addQueryParam(123);
 *     
 * JdbcComponent component = new JdbcComponent();
 * DslJdbcSampler sampler = component.process(request);
 * </pre>
 * 
 * @since 1.0
 */
public class JdbcComponent implements Component<JdbcRequestEntity, DslJdbcSampler> {
    private static final Logger logger = LoggerFactory.getLogger(JdbcComponent.class);
    
    /**
     * Processes a JDBC request entity and converts it to a JMeter JDBC sampler.
     * 
     * <p>This method validates the entity, creates a connection pool, and configures
     * a JDBC sampler to execute the SQL query. If the entity includes query parameters,
     * they are bound to the query as prepared statement parameters.</p>
     * 
     * @param entity The JDBC request entity to process, must not be null
     * @return A configured JMeter JDBC sampler ready to be added to a test plan
     * @throws RuntimeException If the JDBC driver cannot be loaded or if the entity contains invalid data
     */
    @Override
    public DslJdbcSampler process(JdbcRequestEntity entity) {
        logger.info("Processing JDBC request for URL: {}", entity.getJdbcUrl());
        
        try {
            // Validate required parameters before creating the sampler
            validateRequiredParameters(entity);
            
            // Get the effective query to use (either main query or first from list)
            String queryToUse = getEffectiveQuery(entity);
            
            // Create a unique connection pool name to avoid conflicts
            String poolName = "JDBC-Pool-" + System.currentTimeMillis();
            
            // Create the connection pool with required parameters from configuration
            // Connection pool is automatically registered with JMeter by name
            createConnectionPool(poolName, entity);
            
            // Create a JDBC sampler with the pool name and query
            DslJdbcSampler sampler = jdbcSampler(poolName, queryToUse);
            
            // Add parameters if provided for prepared statements
            List<Object> params = entity.getQueryParams();
            if (params != null && !params.isEmpty()) {
                logger.info("Adding {} parameters to JDBC query", params.size());
                
                for (int i = 0; i < params.size(); i++) {
                    Object param = params.get(i);
                    if (param != null) {
                        // JMeter DSL JDBC requires the parameter value first, then the parameter index (1-based)
                        sampler = sampler.param(param, i+1);
                    }
                }
            }
            
            return sampler;
        } catch (ClassNotFoundException e) {
            logger.error("JDBC driver class not found: {}", entity.getJdbcDriverClass(), e);
            throw new RuntimeException("Failed to load JDBC driver: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid JDBC configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid JDBC configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Determines the effective SQL query to use from the entity.
     * 
     * <p>This method checks for a primary query first, and if not found,
     * falls back to the first query in the queries list.</p>
     * 
     * @param entity The JDBC request entity
     * @return The SQL query to execute, or null if none is defined
     */
    private String getEffectiveQuery(JdbcRequestEntity entity) {
        // If the main query is set, use it
        if (entity.getQuery() != null && !entity.getQuery().isEmpty()) {
            return entity.getQuery();
        }
        
        // Otherwise, check if there are any queries in the list
        if (entity.getQueries() != null && !entity.getQueries().isEmpty()) {
            return entity.getQueries().get(0);
        }
        
        // No query found
        return null;
    }
    
    /**
     * Validates that the entity contains all required parameters.
     * 
     * <p>This method ensures that the JDBC URL, driver class, and SQL query
     * are all specified and non-empty.</p>
     * 
     * @param entity The JDBC request entity to validate
     * @throws IllegalArgumentException If any required parameter is missing
     */
    private void validateRequiredParameters(JdbcRequestEntity entity) {
        if (entity.getJdbcUrl() == null || entity.getJdbcUrl().isEmpty()) {
            throw new IllegalArgumentException("JDBC URL is required");
        }
        
        if (entity.getJdbcDriverClass() == null || entity.getJdbcDriverClass().isEmpty()) {
            throw new IllegalArgumentException("JDBC driver class is required");
        }
        
        // Check for either the main query or at least one query in the list
        String query = getEffectiveQuery(entity);
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("SQL query is required");
        }
    }
    
    /**
     * Creates a JDBC connection pool with the configuration from the entity.
     * 
     * <p>This method loads the JDBC driver class, creates a connection pool,
     * and configures it with the URL, credentials, and other settings from
     * the entity.</p>
     * 
     * @param poolName The name for the connection pool
     * @param entity The JDBC request entity containing connection information
     * @return A configured JMeter JDBC connection pool
     * @throws ClassNotFoundException If the JDBC driver class cannot be loaded
     */
    private DslJdbcConnectionPool createConnectionPool(String poolName, JdbcRequestEntity entity) 
            throws ClassNotFoundException {
        // Load the JDBC driver class
        Class<? extends Driver> driverClass = 
            Class.forName(entity.getJdbcDriverClass()).asSubclass(Driver.class);
        
        // Create a connection pool with the driver and URL
        DslJdbcConnectionPool connectionPool = jdbcConnectionPool(poolName, driverClass, entity.getJdbcUrl());
        
        // Apply additional connection settings if provided
        if (entity.getUsername() != null && !entity.getUsername().isEmpty()) {
            connectionPool.user(entity.getUsername());
        }
        
        if (entity.getPassword() != null) {
            connectionPool.password(entity.getPassword());
        }
        
        // Set auto-commit to true by default
        connectionPool.autoCommit(true);
        
        return connectionPool;
    }
}
