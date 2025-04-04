package io.perftest.examples;

import io.perftest.util.YamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Example class demonstrating how to use the YamlConfigLoader to load templates
 */
public class TemplateLoaderExample {
    private static final Logger logger = LoggerFactory.getLogger(TemplateLoaderExample.class);
    
    /**
     * Loads the GraphQL query template from the configuration.
     * 
     * @param config The YAML configuration map
     * @return The GraphQL query as a string, or null if not found
     */
    public static String loadGraphQLQueryTemplate(Map<String, Object> config) {
        String queryTemplatePath = YamlConfigLoader.getTemplatePath(config, "queryPath");
        if (queryTemplatePath == null) {
            logger.warn("No GraphQL query template path specified in configuration");
            return null;
        }
        
        try {
            return YamlConfigLoader.loadTemplateContent(queryTemplatePath);
        } catch (IOException e) {
            logger.error("Failed to load GraphQL query template: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Loads the HTTP request body template from the configuration.
     * 
     * @param config The YAML configuration map
     * @return The HTTP request body as a string, or null if not found
     */
    public static String loadHttpBodyTemplate(Map<String, Object> config) {
        String bodyTemplatePath = YamlConfigLoader.getTemplatePath(config, "bodyPath");
        if (bodyTemplatePath == null) {
            logger.warn("No HTTP body template path specified in configuration");
            return null;
        }
        
        try {
            return YamlConfigLoader.loadTemplateContent(bodyTemplatePath);
        } catch (IOException e) {
            logger.error("Failed to load HTTP body template: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Loads the HTTP headers template from the configuration.
     * 
     * @param config The YAML configuration map
     * @return The HTTP headers as a string, or null if not found
     */
    public static String loadHeadersTemplate(Map<String, Object> config) {
        String headersTemplatePath = YamlConfigLoader.getTemplatePath(config, "headersPath");
        if (headersTemplatePath == null) {
            logger.warn("No headers template path specified in configuration");
            return null;
        }
        
        try {
            return YamlConfigLoader.loadTemplateContent(headersTemplatePath);
        } catch (IOException e) {
            logger.error("Failed to load headers template: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Loads the success response template from the configuration.
     * 
     * @param config The YAML configuration map
     * @return The success response schema as a string, or null if not found
     */
    public static String loadSuccessResponseTemplate(Map<String, Object> config) {
        String responseTemplatePath = YamlConfigLoader.getResponseTemplatePath(config, "success");
        if (responseTemplatePath == null) {
            logger.warn("No success response template path specified in configuration");
            return null;
        }
        
        try {
            return YamlConfigLoader.loadTemplateContent(responseTemplatePath);
        } catch (IOException e) {
            logger.error("Failed to load success response template: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Loads the error response template from the configuration.
     * 
     * @param config The YAML configuration map
     * @return The error response schema as a string, or null if not found
     */
    public static String loadErrorResponseTemplate(Map<String, Object> config) {
        String responseTemplatePath = YamlConfigLoader.getResponseTemplatePath(config, "error");
        if (responseTemplatePath == null) {
            logger.warn("No error response template path specified in configuration");
            return null;
        }
        
        try {
            return YamlConfigLoader.loadTemplateContent(responseTemplatePath);
        } catch (IOException e) {
            logger.error("Failed to load error response template: {}", e.getMessage());
            return null;
        }
    }
}
