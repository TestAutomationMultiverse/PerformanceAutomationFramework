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
 * Component for processing JDBC database requests
 */
public class JdbcComponent implements Component<JdbcRequestEntity, DslJdbcSampler> {
    private static final Logger logger = LoggerFactory.getLogger(JdbcComponent.class);
    
    @Override
    public DslJdbcSampler process(JdbcRequestEntity entity) {
        logger.info("Processing JDBC request for URL: {}", entity.getJdbcUrl());
        
        try {
            // Validate required parameters
            validateRequiredParameters(entity);
            
            // Get the query to use - use the first from the queries list if the main query is not set
            String queryToUse = getEffectiveQuery(entity);
            
            // Create a unique connection pool name
            String poolName = "JDBC-Pool-" + System.currentTimeMillis();
            
            // Create the connection pool with required parameters from configuration
            DslJdbcConnectionPool connectionPool = createConnectionPool(poolName, entity);
            
            // Create a JDBC sampler
            DslJdbcSampler sampler = jdbcSampler(poolName, queryToUse);
            
            // Add parameters if provided
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
    
    private DslJdbcConnectionPool createConnectionPool(String poolName, JdbcRequestEntity entity) 
            throws ClassNotFoundException {
        Class<? extends Driver> driverClass = 
            Class.forName(entity.getJdbcDriverClass()).asSubclass(Driver.class);
        
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
