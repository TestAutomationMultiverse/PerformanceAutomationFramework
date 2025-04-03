package io.perftest.entities.request;

import io.perftest.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity representing an HTTP request
 */
public class HttpRequestEntity extends RequestEntity {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestEntity.class);
    
    private String body;
    private Map<String, Object> variables;  // Template variables
    private static Map<String, Object> defaults;
    private static String baseUrl;
    
    // Static initializer to load default values from configuration
    static {
        try {
            ConfigManager configManager = new ConfigManager();
            configManager.loadConfigFromResource("http-config.yml");
            Map<String, Object> httpConfig = configManager.getConfigForProtocol("defaults");
            if (httpConfig != null) {
                defaults = httpConfig;
                baseUrl = (String) defaults.getOrDefault("baseUrl", "https://test-api.k6.io");
            } else {
                defaults = new HashMap<>();
                defaults.put("default_method", "GET");
                defaults.put("default_content_type", "application/json");
                defaults.put("default_charset", "UTF-8");
                defaults.put("follow_redirects", true);
                defaults.put("connect_timeout", 5000);
                defaults.put("response_timeout", 10000);
                baseUrl = "https://test-api.k6.io"; // Default if not found in config
            }
        } catch (IOException e) {
            logger.warn("Failed to load HTTP configuration defaults", e);
            defaults = new HashMap<>();
            defaults.put("default_method", "GET");
            defaults.put("default_content_type", "application/json");
            defaults.put("default_charset", "UTF-8");
            defaults.put("follow_redirects", true);
            defaults.put("connect_timeout", 5000);
            defaults.put("response_timeout", 10000);
            baseUrl = "https://test-api.k6.io"; // Default if exception occurs
        }
    }
    
    /**
     * Creates a new HTTP request entity with the specified URL
     * @param url URL for the HTTP request
     */
    public HttpRequestEntity(String url) {
        super(url);
        
        // Get default method from config
        String method = (String) defaults.getOrDefault("default_method", "GET");
        setMethod(method);
        
        // Set default content type if not provided
        String contentType = (String) defaults.getOrDefault("default_content_type", "application/json");
        String charset = (String) defaults.getOrDefault("default_charset", "UTF-8");
        addHeader("Content-Type", contentType + ";charset=" + charset);
        
        // Set default timeouts
        Integer connectTimeout = getIntegerValue(defaults.getOrDefault("connect_timeout", 5000));
        Integer responseTimeout = getIntegerValue(defaults.getOrDefault("response_timeout", 10000));
        setConnectTimeout(connectTimeout);
        setResponseTimeout(responseTimeout);
        
        // Initialize variables map
        this.variables = new HashMap<>();
    }
    
    /**
     * Creates a new HTTP request entity with an endpoint relative to the base URL
     * @param endpoint Endpoint path (e.g., "/public/crocodiles/1")
     * @param useBaseUrl Whether to prepend the base URL from configuration
     */
    public HttpRequestEntity(String endpoint, boolean useBaseUrl) {
        this(useBaseUrl ? constructFullUrl(endpoint) : endpoint);
    }
    
    /**
     * Creates a new HTTP request entity with an endpoint from configuration
     * @param configPathKey Configuration path key for the endpoint
     * @param testName Test name to locate in the configuration
     */
    public HttpRequestEntity(String configPathKey, String testName) {
        this(getEndpointFromConfig(configPathKey, testName));
    }
    
    /**
     * Helper method to construct a full URL from an endpoint
     * @param endpoint Endpoint path
     * @return Full URL with base URL and endpoint
     */
    private static String constructFullUrl(String endpoint) {
        // If the endpoint already starts with http:// or https://, return as is
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        
        // Make sure the endpoint starts with "/"
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        
        return baseUrl + endpoint;
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
     * Get the request body
     * @return Request body
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Set the request body
     * @param body Request body
     * @return this instance for chaining
     */
    public HttpRequestEntity setBody(String body) {
        this.body = body;
        return this;
    }
    
    /**
     * Get template variables
     * @return Map of template variables
     */
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    /**
     * Set all template variables
     * @param variables Map of template variables
     * @return this instance for chaining
     */
    public HttpRequestEntity setVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }
    
    /**
     * Add a template variable
     * @param name Variable name
     * @param value Variable value
     * @return this instance for chaining
     */
    public HttpRequestEntity addVariable(String name, Object value) {
        this.variables.put(name, value);
        return this;
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
    
    /**
     * Set the headers map (overriding the default)
     * @param headers Map of headers
     * @return this instance for chaining
     */
    @Override
    public HttpRequestEntity setHeaders(Map<String, String> headers) {
        super.setHeaders(headers);
        return this;
    }
    
    /**
     * Set the assertions map (overriding the default)
     * @param assertions Map of assertions
     * @return this instance for chaining
     */
    @Override
    public HttpRequestEntity setAssertions(Map<String, String> assertions) {
        super.setAssertions(assertions);
        return this;
    }
    
    /**
     * Gets the base URL from configuration
     * @return Base URL string
     */
    public static String getBaseUrl() {
        return baseUrl;
    }
    
    /**
     * Gets an endpoint URL from configuration by test name
     * @param configSection Configuration section (e.g., "tests", "auth_tests.tests")
     * @param testName Test name to look for
     * @return The full URL (base URL + endpoint) or null if not found
     */
    private static String getEndpointFromConfig(String configSection, String testName) {
        try {
            ConfigManager configManager = new ConfigManager();
            configManager.loadConfigFromResource("http-config.yml");
            
            // Handle nested sections with dot notation
            String[] sectionParts = configSection.split("\\.");
            Map<String, Object> currentSection = configManager.getConfigMap();
            
            // Navigate to the specified section
            for (String part : sectionParts) {
                if (currentSection.containsKey(part) && currentSection.get(part) instanceof Map) {
                    currentSection = (Map<String, Object>) currentSection.get(part);
                } else {
                    logger.warn("Configuration section not found: {}", part);
                    return baseUrl; // Return default base URL if section not found
                }
            }
            
            // If the section is a list (like "tests"), find the test by name
            if (currentSection instanceof List) {
                List<Map<String, Object>> testsList = (List<Map<String, Object>>) currentSection;
                for (Map<String, Object> test : testsList) {
                    if (testName.equals(test.get("name"))) {
                        String endpoint = (String) test.get("endpoint");
                        if (endpoint != null) {
                            return constructFullUrl(endpoint);
                        } else {
                            String url = (String) test.get("url");
                            if (url != null) {
                                return url; // Use full URL if available
                            }
                        }
                    }
                }
            }
            
            logger.warn("Test not found in configuration: {}", testName);
            return baseUrl; // Return default base URL if test not found
            
        } catch (IOException e) {
            logger.error("Error loading configuration for endpoint", e);
            return baseUrl; // Return default base URL on error
        }
    }
}