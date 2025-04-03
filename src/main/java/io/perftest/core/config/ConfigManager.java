package io.perftest.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages loading and accessing configuration settings from YAML files
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private final ObjectMapper yamlMapper;
    private Map<String, Object> configMap = new HashMap<>();
    private Map<String, Map<String, Object>> configMapByProtocol = new HashMap<>();

    public ConfigManager() {
        // Using factory method to ensure compatibility
        this.yamlMapper = createYamlMapper();
    }
    
    /**
     * Create a YAML ObjectMapper that is compatible with different Jackson versions
     * @return Configured ObjectMapper for YAML
     */
    private ObjectMapper createYamlMapper() {
        return new ObjectMapper(new YAMLFactory());
    }

    /**
     * Loads configuration from a YAML file
     * @param configFile File object pointing to YAML configuration file
     * @throws IOException if file cannot be read or parsed
     */
    public void loadConfig(File configFile) throws IOException {
        logger.info("Loading configuration from file: {}", configFile.getAbsolutePath());
        
        try {
            // More compatible approach for YAML parsing
            String yamlContent = new String(java.nio.file.Files.readAllBytes(configFile.toPath()));
            configMap = yamlMapper.readValue(yamlContent, Map.class);
            
            logger.info("Configuration loaded successfully with {} keys", configMap.size());
            
            // Store by protocol type based on filename
            String protocol = getProtocolFromFileName(configFile.getName());
            if (protocol != null) {
                configMapByProtocol.put(protocol, configMap);
            }
        } catch (IOException e) {
            logger.error("Failed to load configuration from file: {}", configFile.getAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * Loads configuration from a resource in the classpath
     * @param resourcePath Path to YAML configuration resource
     * @throws IOException if resource cannot be read or parsed
     */
    public void loadConfigFromResource(String resourcePath) throws IOException {
        logger.info("Loading configuration from resource: {}", resourcePath);
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            
            // More compatible approach for YAML parsing
            String yamlContent = new String(inputStream.readAllBytes());
            configMap = yamlMapper.readValue(yamlContent, Map.class);
            
            logger.info("Configuration loaded successfully with {} keys", configMap.size());
            
            // Store by protocol type based on filename
            String protocol = getProtocolFromFileName(resourcePath);
            if (protocol != null) {
                configMapByProtocol.put(protocol, configMap);
            }
        } catch (IOException e) {
            logger.error("Failed to load configuration from resource: {}", resourcePath, e);
            throw e;
        }
    }
    
    /**
     * Loads multiple configuration files from resources
     * @param resourcePaths List of paths to YAML configuration resources
     * @throws IOException if any resource cannot be read or parsed
     */
    public void loadConfigsFromResources(String... resourcePaths) throws IOException {
        for (String path : resourcePaths) {
            loadConfigFromResource(path);
        }
    }
    
    /**
     * Gets the configuration for a specific protocol
     * @param protocol Protocol name (http, graphql, soap)
     * @return Configuration map for the specified protocol
     */
    public Map<String, Object> getConfigForProtocol(String protocol) {
        return configMapByProtocol.getOrDefault(protocol, new HashMap<>());
    }

    /**
     * Extracts protocol type from configuration filename
     * @param filename Name of the configuration file
     * @return Protocol name or null if not recognized
     */
    private String getProtocolFromFileName(String filename) {
        List<String> protocols = Arrays.asList("http", "graphql", "soap", "jdbc");
        
        for (String protocol : protocols) {
            if (filename.contains(protocol)) {
                return protocol;
            }
        }
        
        return null;
    }

    /**
     * Gets a configuration value by path (dot notation)
     * @param path Configuration path (e.g., "http.timeouts.connect")
     * @param defaultValue Default value if path is not found
     * @param <T> Expected return type
     * @return Configuration value or default value if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path, T defaultValue) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = configMap;
        
        for (int i = 0; i < parts.length - 1; i++) {
            Object value = current.get(parts[i]);
            if (!(value instanceof Map)) {
                return defaultValue;
            }
            current = (Map<String, Object>) value;
        }
        
        Object value = current.get(parts[parts.length - 1]);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return (T) value;
        } catch (ClassCastException e) {
            logger.warn("Type mismatch for configuration key: {}", path, e);
            return defaultValue;
        }
    }

    /**
     * Gets a configuration value by path
     * @param path Configuration path (e.g., "http.timeouts.connect")
     * @param <T> Expected return type
     * @return Configuration value or null if not found
     */
    public <T> T get(String path) {
        return get(path, null);
    }

    /**
     * Gets the entire configuration map for the active configuration
     * @return Map of all configuration values
     */
    public Map<String, Object> getConfigMap() {
        return configMap;
    }
    
    /**
     * Gets all loaded configuration maps by protocol
     * @return Map of protocol -> config maps
     */
    public Map<String, Map<String, Object>> getAllConfigs() {
        return configMapByProtocol;
    }
}