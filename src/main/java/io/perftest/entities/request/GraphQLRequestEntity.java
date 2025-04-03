package io.perftest.entities.request;

import io.perftest.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a GraphQL request
 */
public class GraphQLRequestEntity extends RequestEntity {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLRequestEntity.class);
    
    private String query;
    private String operationName;
    private Map<String, Object> variables; // GraphQL request variables
    private Map<String, Object> templateVars; // Variables for template processing
    private static Map<String, Object> defaults;
    
    // Static initializer to load default values from configuration
    static {
        try {
            ConfigManager configManager = new ConfigManager();
            configManager.loadConfigFromResource("graphql-config.yml");
            Map<String, Object> graphqlConfig = configManager.getConfigForProtocol("defaults");
            if (graphqlConfig != null) {
                defaults = graphqlConfig;
            } else {
                defaults = new HashMap<>();
                defaults.put("default_method", "POST");
                defaults.put("default_content_type", "application/json");
                defaults.put("default_charset", "UTF-8");
                defaults.put("follow_redirects", true);
                defaults.put("connect_timeout", 5000);
                defaults.put("response_timeout", 10000);
                defaults.put("operation_name_field", "operationName");
                defaults.put("query_field", "query");
                defaults.put("variables_field", "variables");
            }
        } catch (IOException e) {
            logger.warn("Failed to load GraphQL configuration defaults", e);
            defaults = new HashMap<>();
            defaults.put("default_method", "POST");
            defaults.put("default_content_type", "application/json");
            defaults.put("default_charset", "UTF-8");
            defaults.put("follow_redirects", true);
            defaults.put("connect_timeout", 5000);
            defaults.put("response_timeout", 10000);
            defaults.put("operation_name_field", "operationName");
            defaults.put("query_field", "query");
            defaults.put("variables_field", "variables");
        }
    }
    
    /**
     * Creates a new GraphQL request entity with the specified URL
     * @param url URL for the GraphQL endpoint
     */
    public GraphQLRequestEntity(String url) {
        super(url);
        
        // Get method from config (GraphQL requests are typically POST)
        String method = (String) defaults.getOrDefault("default_method", "POST");
        setMethod(method);
        
        // Set default content type
        String contentType = (String) defaults.getOrDefault("default_content_type", "application/json");
        String charset = (String) defaults.getOrDefault("default_charset", "UTF-8");
        addHeader("Content-Type", contentType + ";charset=" + charset);
        
        // Set default timeouts
        Integer connectTimeout = getIntegerValue(defaults.getOrDefault("connect_timeout", 5000));
        Integer responseTimeout = getIntegerValue(defaults.getOrDefault("response_timeout", 10000));
        setConnectTimeout(connectTimeout);
        setResponseTimeout(responseTimeout);
        
        // Initialize variables maps
        this.variables = new HashMap<>();
        this.templateVars = new HashMap<>();
    }
    
    /**
     * Helper to safely convert Object to Integer
     */
    private Integer getIntegerValue(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 5000; // Default
            }
        }
        return 5000; // Default
    }
    
    /**
     * Get the GraphQL query
     * @return GraphQL query
     */
    public String getQuery() {
        return query;
    }
    
    /**
     * Set the GraphQL query
     * @param query GraphQL query
     * @return this instance for chaining
     */
    public GraphQLRequestEntity setQuery(String query) {
        this.query = query;
        return this;
    }
    
    /**
     * Get the GraphQL operation name
     * @return Operation name
     */
    public String getOperationName() {
        return operationName;
    }
    
    /**
     * Set the GraphQL operation name
     * @param operationName Operation name
     * @return this instance for chaining
     */
    public GraphQLRequestEntity setOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }
    
    /**
     * Get GraphQL variables
     * @return Map of GraphQL variables
     */
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    /**
     * Set all GraphQL variables
     * @param variables Map of GraphQL variables
     * @return this instance for chaining
     */
    public GraphQLRequestEntity setVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }
    
    /**
     * Add a GraphQL variable
     * @param name Variable name
     * @param value Variable value
     * @return this instance for chaining
     */
    public GraphQLRequestEntity addVariable(String name, Object value) {
        this.variables.put(name, value);
        return this;
    }
    
    /**
     * Add a GraphQL variable (alias for addVariable)
     * @param name Variable name
     * @param value Variable value
     * @return this instance for chaining
     */
    public GraphQLRequestEntity addGraphQLVariable(String name, Object value) {
        return addVariable(name, value);
    }
    
    /**
     * Get template variables
     * @return Map of template variables
     */
    public Map<String, Object> getTemplateVars() {
        return templateVars;
    }
    
    /**
     * Set all template variables
     * @param templateVars Map of template variables
     * @return this instance for chaining
     */
    public GraphQLRequestEntity setTemplateVars(Map<String, Object> templateVars) {
        this.templateVars = templateVars;
        return this;
    }
    
    /**
     * Add a template variable
     * @param name Variable name
     * @param value Variable value
     * @return this instance for chaining
     */
    public GraphQLRequestEntity addTemplateVar(String name, Object value) {
        this.templateVars.put(name, value);
        return this;
    }
    
    /**
     * Get the field name used for the operation name in the request
     * @return Operation name field
     */
    public String getOperationNameField() {
        return (String) defaults.getOrDefault("operation_name_field", "operationName");
    }
    
    /**
     * Get the field name used for the query in the request
     * @return Query field
     */
    public String getQueryField() {
        return (String) defaults.getOrDefault("query_field", "query");
    }
    
    /**
     * Get the field name used for variables in the request
     * @return Variables field
     */
    public String getVariablesField() {
        return (String) defaults.getOrDefault("variables_field", "variables");
    }
    
    /**
     * Get the follow redirects setting from configuration
     * @return true if redirects should be followed, false otherwise
     */
    public boolean getFollowRedirects() {
        return Boolean.TRUE.equals(defaults.getOrDefault("follow_redirects", Boolean.TRUE));
    }
    
    /**
     * Get all defaults from configuration
     * @return Map of default configuration values
     */
    public static Map<String, Object> getDefaults() {
        return defaults;
    }
}