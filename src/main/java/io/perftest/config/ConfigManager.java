package io.perftest.config;

import io.perftest.exception.ConfigException;
import io.perftest.exception.ErrorCode;
import io.perftest.exception.ErrorHandler;
import io.perftest.exception.Result;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manager for configuration settings
 */
@Slf4j
public class ConfigManager {
    
    private static ConfigManager instance;
    
    @Getter
    @Setter
    private Map<String, Object> configMap = new HashMap<>();
    
    /**
     * Private constructor for singleton pattern
     */
    private ConfigManager() {
        log.info("Initializing configuration manager");
    }
    
    /**
     * Get the singleton instance
     * @return The ConfigManager instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * Set a configuration value
     * @param key Configuration key
     * @param value Configuration value
     * @throws ConfigException If the key or value is null
     */
    public void setConfig(String key, Object value) {
        ErrorHandler.validateNotNull(key, ErrorCode.CONFIG_ERROR, "Configuration key cannot be null");
        ErrorHandler.validateNotNull(value, ErrorCode.CONFIG_ERROR, "Configuration value cannot be null");
        
        log.debug("Setting configuration: {}={}", key, value);
        configMap.put(key, value);
    }
    
    /**
     * Set a configuration value with error handling
     * @param key Configuration key
     * @param value Configuration value
     * @return A Result indicating success or failure
     */
    public Result<Void> safeSetConfig(String key, Object value) {
        if (key == null) {
            return Result.failure(ErrorCode.CONFIG_ERROR, "Configuration key cannot be null");
        }
        if (value == null) {
            return Result.failure(ErrorCode.CONFIG_ERROR, "Configuration value cannot be null");
        }
        
        try {
            log.debug("Setting configuration: {}={}", key, value);
            configMap.put(key, value);
            return Result.success(null);
        } catch (Exception e) {
            log.error("Error setting configuration: {}", e.getMessage(), e);
            return Result.failure(ErrorCode.CONFIG_ERROR, "Failed to set configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get a configuration value
     * @param key Configuration key
     * @param <T> Type of the configuration value
     * @return The configuration value
     * @throws ConfigException If the key is null or not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key) {
        ErrorHandler.validateNotNull(key, ErrorCode.CONFIG_ERROR, "Configuration key cannot be null");
        
        log.debug("Getting configuration: {}", key);
        Object value = configMap.get(key);
        
        if (value == null) {
            throw new ConfigException(ErrorCode.CONFIG_ERROR, "Configuration not found for key: " + key);
        }
        
        return (T) value;
    }
    
    /**
     * Get a configuration value with error handling
     * @param key Configuration key
     * @param <T> Type of the configuration value
     * @return A Result containing the configuration value or an error
     */
    @SuppressWarnings("unchecked")
    public <T> Result<T> safeGetConfig(String key) {
        if (key == null) {
            return Result.failure(ErrorCode.CONFIG_ERROR, "Configuration key cannot be null");
        }
        
        log.debug("Getting configuration: {}", key);
        Object value = configMap.get(key);
        
        if (value == null) {
            return Result.failure(ErrorCode.CONFIG_ERROR, "Configuration not found for key: " + key);
        }
        
        try {
            return Result.success((T) value);
        } catch (ClassCastException e) {
            log.error("Error casting configuration value: {}", e.getMessage(), e);
            return Result.failure(ErrorCode.CONFIG_ERROR, 
                    "Failed to cast configuration value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get a configuration value or a default value if not found
     * @param key Configuration key
     * @param defaultValue Default value to return if the key is not found
     * @param <T> Type of the configuration value
     * @return The configuration value or the default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigOrDefault(String key, T defaultValue) {
        ErrorHandler.validateNotNull(key, ErrorCode.CONFIG_ERROR, "Configuration key cannot be null");
        
        log.debug("Getting configuration with default: {}", key);
        Object value = configMap.get(key);
        
        if (value == null) {
            log.debug("Configuration not found, returning default value: {}", defaultValue);
            return defaultValue;
        }
        
        return (T) value;
    }
    
    /**
     * Check if a configuration key exists
     * @param key Configuration key
     * @return True if the key exists, false otherwise
     * @throws ConfigException If the key is null
     */
    public boolean hasConfig(String key) {
        ErrorHandler.validateNotNull(key, ErrorCode.CONFIG_ERROR, "Configuration key cannot be null");
        
        boolean hasKey = configMap.containsKey(key);
        log.debug("Checking if configuration exists: {} = {}", key, hasKey);
        return hasKey;
    }
    
    /**
     * Get a configuration value as an Optional
     * @param key Configuration key
     * @param <T> Type of the configuration value
     * @return An Optional containing the configuration value, or empty if not found
     * @throws ConfigException If the key is null
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConfigAsOptional(String key) {
        ErrorHandler.validateNotNull(key, ErrorCode.CONFIG_ERROR, "Configuration key cannot be null");
        
        log.debug("Getting configuration as optional: {}", key);
        return Optional.ofNullable((T) configMap.get(key));
    }
    
    /**
     * Remove a configuration value
     * @param key Configuration key
     * @throws ConfigException If the key is null
     */
    public void removeConfig(String key) {
        ErrorHandler.validateNotNull(key, ErrorCode.CONFIG_ERROR, "Configuration key cannot be null");
        
        log.debug("Removing configuration: {}", key);
        configMap.remove(key);
    }
    
    /**
     * Clear all configuration values
     */
    public void clearConfig() {
        log.debug("Clearing all configurations");
        configMap.clear();
    }
    
    /**
     * Load configuration from a YAML file
     * @param path Path to the YAML file
     * @param configClass Class to map the YAML to
     * @param <T> Type of the configuration object
     * @return The configuration object
     * @throws ConfigException If an error occurs while loading the configuration
     * @throws IOException If an I/O error occurs
     */
    public <T> T loadFromYaml(String path, Class<T> configClass) throws IOException {
        log.info("Loading configuration from YAML: {}", path);
        ErrorHandler.validateNotNull(path, ErrorCode.CONFIG_FILE_NOT_FOUND, "Path to YAML file cannot be null");
        ErrorHandler.validateNotNull(configClass, ErrorCode.CONFIG_PARSE_ERROR, "Configuration class cannot be null");
        
        T config = YamlConfigUtil.load(path, configClass);
        String configKey = path.replace("/", ".").replace(".yml", "").replace(".yaml", "");
        setConfig(configKey, config);
        
        return config;
    }
    
    /**
     * Load configuration from a YAML file with error handling
     * @param path Path to the YAML file
     * @param configClass Class to map the YAML to
     * @param <T> Type of the configuration object
     * @return A Result containing the configuration object or an error
     */
    public <T> Result<T> safeLoadFromYaml(String path, Class<T> configClass) {
        log.info("Safely loading configuration from YAML: {}", path);
        
        if (path == null) {
            return Result.failure(ErrorCode.CONFIG_FILE_NOT_FOUND, "Path to YAML file cannot be null");
        }
        if (configClass == null) {
            return Result.failure(ErrorCode.CONFIG_PARSE_ERROR, "Configuration class cannot be null");
        }
        
        return YamlConfigUtil.safeLoad(path, configClass)
                .map(config -> {
                    String configKey = path.replace("/", ".").replace(".yml", "").replace(".yaml", "");
                    setConfig(configKey, config);
                    return config;
                });
    }
}
