package io.perftest.factories;

import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.JdbcRequestEntity;
import io.perftest.entities.request.RequestEntity;
import io.perftest.entities.request.SoapRequestEntity;
import io.perftest.entities.request.XmlRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Factory class for creating request entities.
 * 
 * <p>This class centralizes the creation of different request entities,
 * ensuring consistent entity creation across the application. It follows
 * the Entity-Component-System (ECS) architecture by keeping entity creation
 * logic separate from test implementation.</p>
 * 
 * <p>Each method creates a specific type of entity with appropriate configuration
 * based on the provided parameters or config maps.</p>
 */
public class EntityFactory {
    private static final Logger logger = LoggerFactory.getLogger(EntityFactory.class);

    /**
     * Creates an HTTP request entity from a configuration map.
     * 
     * @param config The configuration map containing request parameters
     * @return A configured HttpRequestEntity
     */
    public static HttpRequestEntity createHttpEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = getRequestConfig(config);
        
        String name = (String) requestConfig.getOrDefault("name", "HTTP Request");
        String endpoint = (String) requestConfig.getOrDefault("endpoint", "https://example.com/api");
        
        // Create entity with the URL and then set the name as a property
        HttpRequestEntity entity = new HttpRequestEntity(endpoint);
        entity.setName(name);
        
        // Set method if present (default to GET)
        String method = (String) requestConfig.getOrDefault("method", "GET");
        entity.setMethod(method);
        
        // Set body if present
        if (requestConfig.containsKey("body")) {
            String body = (String) requestConfig.get("body");
            entity.setBody(body);
        }
        
        // Set expected status if present
        if (requestConfig.containsKey("expectedStatus")) {
            int expectedStatus = (Integer) requestConfig.get("expectedStatus");
            entity.setExpectedStatus(expectedStatus);
        }
        
        // Set content type if present
        if (requestConfig.containsKey("contentType")) {
            String contentType = (String) requestConfig.get("contentType");
            entity.setContentType(contentType);
        }
        
        // Set headers if present
        if (requestConfig.containsKey("headers")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) requestConfig.get("headers");
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                entity.addHeader(header.getKey(), header.getValue().toString());
            }
        }
        
        logger.debug("Created HTTP entity: {}", entity.getUrl());
        return entity;
    }
    
    /**
     * Creates a simple HTTP request entity with minimal configuration.
     * 
     * @param url The URL to request
     * @param method The HTTP method (GET, POST, PUT, DELETE, etc.)
     * @return A configured HttpRequestEntity
     */
    public static HttpRequestEntity createHttpEntity(String url, String method) {
        HttpRequestEntity entity = new HttpRequestEntity(url);
        entity.setMethod(method);
        entity.setName(method + " " + url);
        
        logger.debug("Created HTTP entity: {} {}", method, url);
        return entity;
    }
    
    /**
     * Creates a simple HTTP request entity with minimal configuration.
     * Default to GET method.
     * 
     * @param url The URL to request
     * @return A configured HttpRequestEntity
     */
    public static HttpRequestEntity createHttpEntity(String url) {
        return createHttpEntity(url, "GET");
    }
    
    /**
     * Creates an HTTP request entity with body.
     * 
     * @param url The URL to request
     * @param method The HTTP method (GET, POST, PUT, DELETE, etc.)
     * @param body The request body
     * @return A configured HttpRequestEntity
     */
    public static HttpRequestEntity createHttpEntity(String url, String method, String body) {
        HttpRequestEntity entity = createHttpEntity(url, method);
        entity.setBody(body);
        return entity;
    }
    
    /**
     * Creates a GraphQL request entity from a configuration map.
     * 
     * @param config The configuration map containing request parameters
     * @return A configured GraphQLRequestEntity
     */
    public static GraphQLRequestEntity createGraphQLEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = getRequestConfig(config);
        
        String endpoint = (String) requestConfig.getOrDefault("endpoint", "https://example.com/graphql");
        String query = (String) requestConfig.getOrDefault("query", "query { }");
        
        GraphQLRequestEntity entity = new GraphQLRequestEntity();
        entity.setUrl(endpoint);
        entity.setQuery(query);
        
        // Set name if present
        String name = (String) requestConfig.getOrDefault("name", "GraphQL Request");
        entity.setProperty("name", name);
        
        // Set variables if present
        if (requestConfig.containsKey("variables")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) requestConfig.get("variables");
            for (Map.Entry<String, Object> variable : variables.entrySet()) {
                entity.addVariable(variable.getKey(), variable.getValue().toString());
            }
        }
        
        // Set headers if present
        if (requestConfig.containsKey("headers")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) requestConfig.get("headers");
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                entity.addHeader(header.getKey(), header.getValue().toString());
            }
        }
        
        logger.debug("Created GraphQL entity for endpoint: {}", endpoint);
        return entity;
    }
    
    /**
     * Creates a GraphQL request entity.
     * 
     * @param endpoint The GraphQL endpoint
     * @param query The GraphQL query
     * @return A configured GraphQLRequestEntity
     */
    public static GraphQLRequestEntity createGraphQLEntity(String endpoint, String query) {
        GraphQLRequestEntity entity = new GraphQLRequestEntity();
        entity.setUrl(endpoint);
        entity.setQuery(query);
        entity.setProperty("name", "GraphQL to " + endpoint);
        
        logger.debug("Created GraphQL entity for endpoint: {}", endpoint);
        return entity;
    }
    
    /**
     * Creates a JDBC request entity from a configuration map.
     * 
     * @param config The configuration map containing request parameters
     * @return A configured JdbcRequestEntity
     */
    public static JdbcRequestEntity createJdbcEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = getRequestConfig(config);
        
        String query = (String) requestConfig.getOrDefault("query", "SELECT 1");
        String jdbcUrl = (String) requestConfig.getOrDefault("jdbcUrl", "");
        String username = (String) requestConfig.getOrDefault("username", "");
        String password = (String) requestConfig.getOrDefault("password", "");
        String driverClass = (String) requestConfig.getOrDefault("driverClass", "");
        
        JdbcRequestEntity entity = new JdbcRequestEntity(jdbcUrl, username, password, driverClass);
        entity.setQuery(query);
        
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
        JdbcRequestEntity entity = new JdbcRequestEntity(jdbcUrl, username, password, "org.postgresql.Driver");
        entity.setQuery(query);
        entity.setProperty("name", "JDBC Query");
        
        logger.debug("Created JDBC entity with query: {}", query);
        return entity;
    }
    
    /**
     * Creates a SOAP request entity from a configuration map.
     * 
     * @param config The configuration map containing request parameters
     * @return A configured SoapRequestEntity
     */
    public static SoapRequestEntity createSoapEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = getRequestConfig(config);
        
        String endpoint = (String) requestConfig.getOrDefault("endpoint", "https://example.com/soap");
        String soapAction = (String) requestConfig.getOrDefault("soapAction", "");
        String payload = (String) requestConfig.getOrDefault("payload", "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body></Body></Envelope>");
        
        SoapRequestEntity entity = new SoapRequestEntity();
        entity.setUrl(endpoint);
        entity.setSoapAction(soapAction);
        entity.setPayload(payload);
        
        // Set name if present
        String name = (String) requestConfig.getOrDefault("name", "SOAP Request");
        entity.setProperty("name", name);
        
        // Set headers if present
        if (requestConfig.containsKey("headers")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) requestConfig.get("headers");
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                entity.addHeader(header.getKey(), header.getValue().toString());
            }
        }
        
        logger.debug("Created SOAP entity for endpoint: {}", endpoint);
        return entity;
    }
    
    /**
     * Creates an XML request entity from a configuration map.
     * 
     * @param config The configuration map containing request parameters
     * @return A configured XmlRequestEntity
     */
    public static XmlRequestEntity createXmlEntity(Map<String, Object> config) {
        Map<String, Object> requestConfig = getRequestConfig(config);
        
        String endpoint = (String) requestConfig.getOrDefault("endpoint", "https://example.com/xml");
        String body = (String) requestConfig.getOrDefault("body", "<root></root>");
        
        XmlRequestEntity entity = new XmlRequestEntity();
        entity.setUrl(endpoint);
        entity.setBody(body);
        
        // Set name if present
        String name = (String) requestConfig.getOrDefault("name", "XML Request");
        entity.setProperty("name", name);
        
        // Set method if present
        String method = (String) requestConfig.getOrDefault("method", "POST");
        entity.setMethod(method);
        
        // Set headers if present
        if (requestConfig.containsKey("headers")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) requestConfig.get("headers");
            for (Map.Entry<String, Object> header : headers.entrySet()) {
                entity.addHeader(header.getKey(), header.getValue().toString());
            }
        }
        
        logger.debug("Created XML entity for endpoint: {}", endpoint);
        return entity;
    }
    
    /**
     * Helper method to extract the request configuration from a full configuration map.
     * 
     * @param config The full configuration map
     * @return The request configuration map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getRequestConfig(Map<String, Object> config) {
        if (config == null) {
            return Map.of();
        }
        
        if (config.containsKey("request")) {
            return (Map<String, Object>) config.get("request");
        }
        
        return config;
    }
}
