package io.perftest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for loading and accessing YAML configuration files.
 */
public class YamlConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(YamlConfigLoader.class);

    /**
     * Loads a YAML configuration file from the classpath.
     *
     * @param filename Name of the YAML file to load (e.g., "http-config.yml")
     * @return Map containing the parsed YAML configuration
     * @throws IOException If the file cannot be read
     */
    public static Map<String, Object> loadConfig(String filename) throws IOException {
        logger.info("Loading YAML configuration from {}", filename);
        Yaml yaml = new Yaml();
        
        try (InputStream inputStream = YamlConfigLoader.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                logger.error("Configuration file not found: {}", filename);
                throw new IOException("Configuration file not found: " + filename);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> config = yaml.load(inputStream);
            if (config == null) {
                logger.warn("Empty configuration file: {}", filename);
                return Collections.emptyMap();
            }
            
            logger.debug("Loaded configuration: {}", config);
            return config;
        } catch (Exception e) {
            logger.error("Error loading YAML configuration: {}", e.getMessage(), e);
            throw new IOException("Error loading YAML configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a template file from the classpath.
     *
     * @param templatePath Path to the template file (e.g., "templates/http/body/create_user.json")
     * @return Content of the template file as a string
     * @throws IOException If the file cannot be read
     */
    public static String loadTemplateContent(String templatePath) throws IOException {
        logger.info("Loading template from {}", templatePath);
        
        try (InputStream inputStream = YamlConfigLoader.class.getClassLoader().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                logger.error("Template file not found: {}", templatePath);
                throw new IOException("Template file not found: " + templatePath);
            }
            
            String content = new String(inputStream.readAllBytes());
            logger.debug("Loaded template content of size: {}", content.length());
            return content;
        } catch (Exception e) {
            logger.error("Error loading template: {}", e.getMessage(), e);
            throw new IOException("Error loading template: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets template paths from the templates section of a configuration map.
     *
     * @param config The configuration map
     * @param templateType The type of template to retrieve (bodyPath, queryPath, headersPath, etc.)
     * @return The template path or null if not found
     */
    public static String getTemplatePath(Map<String, Object> config, String templateType) {
        Map<String, Object> templates = getMap(config, "templates");
        if (templates.isEmpty()) {
            logger.debug("No templates section found in configuration");
            return null;
        }
        
        return getString(templates, templateType, null);
    }
    
    /**
     * Gets a response template path from the templates section of a configuration.
     *
     * @param config The configuration map
     * @param responseType The type of response template (success, error, fault, etc.)
     * @return The response template path or null if not found
     */
    public static String getResponseTemplatePath(Map<String, Object> config, String responseType) {
        Map<String, Object> templates = getMap(config, "templates");
        if (templates.isEmpty()) {
            logger.debug("No templates section found in configuration");
            return null;
        }
        
        Map<String, Object> responsePaths = getMap(templates, "responsePath");
        if (responsePaths.isEmpty()) {
            logger.debug("No responsePath section found in templates configuration");
            return null;
        }
        
        return getString(responsePaths, responseType, null);
    }

    /**
     * Gets a nested map from a configuration map.
     *
     * @param config The configuration map
     * @param key The key for the nested map
     * @return The nested map, or an empty map if not found
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    /**
     * Gets a list from a configuration map.
     *
     * @param config The configuration map
     * @param key The key for the list
     * @return The list, or an empty list if not found
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getList(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return Collections.emptyList();
    }

    /**
     * Gets a string value from a configuration map with a default value.
     *
     * @param config The configuration map
     * @param key The key for the value
     * @param defaultValue The default value to return if the key is not found
     * @return The string value or the default value
     */
    public static String getString(Map<String, Object> config, String key, String defaultValue) {
        return Optional.ofNullable(config.get(key))
                .map(Object::toString)
                .orElse(defaultValue);
    }

    /**
     * Gets an integer value from a configuration map with a default value.
     *
     * @param config The configuration map
     * @param key The key for the value
     * @param defaultValue The default value to return if the key is not found
     * @return The integer value or the default value
     */
    public static int getInt(Map<String, Object> config, String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for key {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    /**
     * Gets a boolean value from a configuration map with a default value.
     *
     * @param config The configuration map
     * @param key The key for the value
     * @param defaultValue The default value to return if the key is not found
     * @return The boolean value or the default value
     */
    public static boolean getBoolean(Map<String, Object> config, String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
}
