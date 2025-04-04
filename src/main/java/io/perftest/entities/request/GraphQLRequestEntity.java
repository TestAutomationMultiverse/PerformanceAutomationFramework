package io.perftest.entities.request;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Entity representing a GraphQL request
 */
public class GraphQLRequestEntity extends RequestEntity {
    private String query;
    private String operationName;
    private Map<String, Object> variables = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Default constructor
     */
    public GraphQLRequestEntity() {
        super();
    }
    
    /**
     * Constructor with URL
     * 
     * @param url The GraphQL endpoint URL
     */
    public GraphQLRequestEntity(String url) {
        super();
        setUrl(url);
    }
    
    /**
     * @return GraphQL query
     */
    public String getQuery() {
        return query;
    }
    
    /**
     * @param query GraphQL query
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * @return GraphQL operation name
     */
    public String getOperationName() {
        return operationName;
    }
    
    /**
     * @param operationName GraphQL operation name
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
    
    /**
     * @return GraphQL variables
     */
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    /**
     * @param variables GraphQL variables
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
    
    /**
     * Set variables from JSON string
     * 
     * @param variablesJson JSON string representing variables
     */
    public void setVariables(String variablesJson) {
        try {
            if (variablesJson == null || variablesJson.trim().isEmpty()) {
                this.variables = new HashMap<>();
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> vars = objectMapper.readValue(variablesJson, Map.class);
                this.variables = vars;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse variables JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add a GraphQL variable
     * 
     * @param name Variable name
     * @param value Variable value
     */
    public void addVariable(String name, Object value) {
        variables.put(name, value);
    }
    
    /**
     * Add a header to the request
     * 
     * @param name Header name
     * @param value Header value
     */
    public void addHeader(String name, String value) {
        getHeaders().put(name, value);
    }
    
    /**
     * Add an assertion for the response
     * 
     * @param path JsonPath or other path expression
     * @param expectedValue Expected value or "*" for existence check
     */
    public void addAssertion(String path, String expectedValue) {
        getAssertions().put(path, expectedValue);
    }
    
    /**
     * Set the name of the request
     * 
     * @param name Request name
     */
    public void setName(String name) {
        setProperty("name", name);
    }
}
