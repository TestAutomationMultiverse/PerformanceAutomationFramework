package io.perftest.factories;

import io.perftest.entities.base.Entity;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.JdbcRequestEntity;
import io.perftest.entities.request.SoapRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating request entity instances.
 */
public class EntityFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityFactory.class);
    
    /**
     * Creates an entity based on the provided configuration.
     * 
     * @param requestConfig The request configuration
     * @return The created entity
     * @throws UnsupportedOperationException If the request type is not supported
     */
    @SuppressWarnings("unchecked")
    public static Entity createEntity(Map<String, Object> requestConfig) {
        if (requestConfig == null || requestConfig.isEmpty()) {
            logger.warn("Empty request configuration provided");
            return null;
        }
        
        String type = (String) requestConfig.getOrDefault("type", "http");
        
        switch (type.toLowerCase()) {
            case "http":
                return createHttpEntity(requestConfig);
            case "graphql":
                return createGraphQLEntity(requestConfig);
            case "soap":
                return createSoapEntity(requestConfig);
            case "jdbc":
                return createJdbcEntity(requestConfig);
            default:
                logger.error("Unsupported request type: {}", type);
                throw new UnsupportedOperationException("Unsupported request type: " + type);
        }
    }
    
    /**
     * Creates an HTTP request entity.
     * 
     * @param requestConfig The request configuration
     * @return A configured HttpRequestEntity
     */
    @SuppressWarnings("unchecked")
    public static HttpRequestEntity createHttpEntity(Map<String, Object> requestConfig) {
        String url = (String) requestConfig.getOrDefault("url", "");
        String method = (String) requestConfig.getOrDefault("method", "GET");
        String body = (String) requestConfig.getOrDefault("body", "");
        Map<String, String> headers = (Map<String, String>) requestConfig.getOrDefault("headers", Collections.emptyMap());
        
        HttpRequestEntity entity = new HttpRequestEntity(url);
        entity.setMethod(method);
        entity.setBody(body);
        entity.setHeaders(headers);
        
        // Set name if present
        String name = (String) requestConfig.getOrDefault("name", method + " " + url);
        entity.setProperty("name", name);
        
        logger.debug("Created HTTP entity with URL: {}", url);
        return entity;
    }
    
    /**
     * Creates an HTTP request entity with URL and method.
     * 
     * @param url The URL for the request
     * @param method The HTTP method (GET, POST, etc.)
     * @return A configured HttpRequestEntity
     */
    public static HttpRequestEntity createHttpEntity(String url, String method) {
        HttpRequestEntity entity = new HttpRequestEntity(url);
        entity.setMethod(method);
        entity.setProperty("name", method + " " + url);
        
        logger.debug("Created HTTP entity with URL: {}", url);
        return entity;
    }
    
    /**
     * Creates a GraphQL request entity.
     * 
     * @param requestConfig The request configuration
     * @return A configured GraphQLRequestEntity
     */
    @SuppressWarnings("unchecked")
    public static GraphQLRequestEntity createGraphQLEntity(Map<String, Object> requestConfig) {
        String url = (String) requestConfig.getOrDefault("url", "");
        String query = (String) requestConfig.getOrDefault("query", "");
        Map<String, Object> variables = (Map<String, Object>) requestConfig.getOrDefault("variables", Collections.emptyMap());
        Map<String, String> headers = (Map<String, String>) requestConfig.getOrDefault("headers", Collections.emptyMap());
        
        GraphQLRequestEntity entity = new GraphQLRequestEntity(url);
        entity.setQuery(query);
        entity.setVariables(variables);
        entity.setHeaders(headers);
        
        // Set name if present
        String name = (String) requestConfig.getOrDefault("name", "GraphQL Query");
        entity.setProperty("name", name);
        
        logger.debug("Created GraphQL entity with URL: {}", url);
        return entity;
    }
    
    /**
     * Creates a GraphQL request entity with URL and query.
     * 
     * @param url The URL for the GraphQL endpoint
     * @param query The GraphQL query
     * @return A configured GraphQLRequestEntity
     */
    public static GraphQLRequestEntity createGraphQLEntity(String url, String query) {
        GraphQLRequestEntity entity = new GraphQLRequestEntity(url);
        entity.setQuery(query);
        entity.setProperty("name", "GraphQL Query");
        
        logger.debug("Created GraphQL entity with URL: {}", url);
        return entity;
    }
    
    /**
     * Creates a SOAP request entity.
     * 
     * @param requestConfig The request configuration
     * @return A configured SoapRequestEntity
     */
    @SuppressWarnings("unchecked")
    public static SoapRequestEntity createSoapEntity(Map<String, Object> requestConfig) {
        String url = (String) requestConfig.getOrDefault("url", "");
        String soapAction = (String) requestConfig.getOrDefault("soapAction", "");
        String payload = (String) requestConfig.getOrDefault("payload", "");
        Map<String, String> headers = (Map<String, String>) requestConfig.getOrDefault("headers", Collections.emptyMap());
        
        SoapRequestEntity entity = new SoapRequestEntity(url);
        entity.setSoapAction(soapAction);
        entity.setPayload(payload);
        entity.setHeaders(headers);
        
        // Set name if present
        String name = (String) requestConfig.getOrDefault("name", "SOAP Action: " + soapAction);
        entity.setProperty("name", name);
        
        logger.debug("Created SOAP entity with URL: {}", url);
        return entity;
    }
    
    /**
     * Creates a JDBC request entity.
     * 
     * @param requestConfig The request configuration
     * @return A configured JdbcRequestEntity
     */
    @SuppressWarnings("unchecked")
    public static JdbcRequestEntity createJdbcEntity(Map<String, Object> requestConfig) {
        String jdbcUrl = (String) requestConfig.getOrDefault("jdbcUrl", "");
        String query = (String) requestConfig.getOrDefault("query", "");
        String username = (String) requestConfig.getOrDefault("username", "");
        String password = (String) requestConfig.getOrDefault("password", "");
        String driverClass = (String) requestConfig.getOrDefault("driverClass", "");
        
        JdbcRequestEntity entity = new JdbcRequestEntity(jdbcUrl, query);
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setJdbcDriverClass(driverClass);
        
        // Set name if present
        String name = (String) requestConfig.getOrDefault("name", "JDBC Query");
        entity.setProperty("name", name);
        
        logger.debug("Created JDBC entity with query: {}", query);
        return entity;
    }
    
    /**
     * Creates a JDBC request entity.
     * 
     * @param jdbcUrl The JDBC URL
     * @param username The database username
     * @param password The database password
     * @param query The SQL query to execute
     * @return A configured JdbcRequestEntity
     */
    public static JdbcRequestEntity createJdbcEntity(String jdbcUrl, String username, String password, String query) {
        JdbcRequestEntity entity = new JdbcRequestEntity(jdbcUrl, query);
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setJdbcDriverClass("org.postgresql.Driver");
        entity.setProperty("name", "JDBC Query");
        
        logger.debug("Created JDBC entity with query: {}", query);
        return entity;
    }
    
    /**
     * Creates a JDBC request entity with default driver.
     * 
     * @param jdbcUrl The JDBC URL
     * @param query The SQL query to execute
     * @return A configured JdbcRequestEntity
     */
    public static JdbcRequestEntity createJdbcEntity(String jdbcUrl, String query) {
        JdbcRequestEntity entity = new JdbcRequestEntity(jdbcUrl, query);
        entity.setJdbcDriverClass("org.postgresql.Driver");
        entity.setProperty("name", "JDBC Query");
        
        logger.debug("Created JDBC entity with query: {}", query);
        return entity;
    }
    
    /**
     * Creates a list of entities from a list of request configurations.
     * 
     * @param requestConfigs The list of request configurations
     * @return A list of created entities
     */
    public static List<Entity> createEntities(List<Map<String, Object>> requestConfigs) {
        if (requestConfigs == null || requestConfigs.isEmpty()) {
            logger.warn("Empty or null request configurations list provided");
            return Collections.emptyList();
        }
        
        List<Entity> entities = new ArrayList<>();
        
        for (Map<String, Object> config : requestConfigs) {
            Entity entity = createEntity(config);
            if (entity != null) {
                entities.add(entity);
            }
        }
        
        return entities;
    }
}
