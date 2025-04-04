package io.perftest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.perftest.exception.ConfigException;
import io.perftest.exception.ErrorCode;
import io.perftest.exception.ErrorHandler;
import io.perftest.exception.ErrorReporter;
import io.perftest.exception.Result;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for YAML configuration
 */
@Slf4j
public class YamlConfigUtil {
    
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    
    /**
     * Load a YAML configuration from the classpath
     * @param path Path to the YAML file
     * @param configClass Class to map the YAML to
     * @param <T> Type of the configuration object
     * @return The configuration object
     * @throws ConfigException If an error occurs while loading the configuration
     * @throws IOException If an I/O error occurs
     */
    public static <T> T load(String path, Class<T> configClass) throws IOException {
        log.info("Loading YAML configuration from: {}", path);
        ErrorHandler.validateNotNull(path, ErrorCode.CONFIG_FILE_NOT_FOUND, "Path to YAML file cannot be null");
        ErrorHandler.validateNotNull(configClass, ErrorCode.CONFIG_PARSE_ERROR, "Configuration class cannot be null");
        
        return ErrorHandler.executeWithIOHandling(() -> {
            try (InputStream is = YamlConfigUtil.class.getClassLoader().getResourceAsStream(path)) {
                if (is == null) {
                    throw new ConfigException(ErrorCode.CONFIG_FILE_NOT_FOUND, 
                            "Could not find YAML configuration at: " + path);
                }
                
                T config = mapper.readValue(is, configClass);
                log.debug("Loaded YAML configuration: {}", config);
                return config;
            }
        }, ErrorCode.CONFIG_PARSE_ERROR, "Failed to load YAML configuration from: " + path, log);
    }
    
    /**
     * Safely load a YAML configuration from the classpath
     * @param path Path to the YAML file
     * @param configClass Class to map the YAML to
     * @param <T> Type of the configuration object
     * @return A Result containing the configuration object or an error
     */
    public static <T> Result<T> safeLoad(String path, Class<T> configClass) {
        log.info("Safely loading YAML configuration from: {}", path);
        
        if (path == null) {
            return Result.failure(ErrorCode.CONFIG_FILE_NOT_FOUND, "Path to YAML file cannot be null");
        }
        if (configClass == null) {
            return Result.failure(ErrorCode.CONFIG_PARSE_ERROR, "Configuration class cannot be null");
        }
        
        try (InputStream is = YamlConfigUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                return Result.failure(ErrorCode.CONFIG_FILE_NOT_FOUND, 
                        "Could not find YAML configuration at: " + path);
            }
            
            try {
                T config = mapper.readValue(is, configClass);
                log.debug("Loaded YAML configuration: {}", config);
                return Result.success(config);
            } catch (Exception e) {
                log.error("Error parsing YAML configuration: {}", e.getMessage(), e);
                ErrorReporter.getInstance().reportError(ErrorCode.CONFIG_PARSE_ERROR, 
                        "Failed to parse YAML configuration from: " + path, e);
                return Result.failure(ErrorCode.CONFIG_PARSE_ERROR, 
                        "Failed to parse YAML configuration: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            log.error("Error loading YAML configuration: {}", e.getMessage(), e);
            ErrorReporter.getInstance().reportError(ErrorCode.CONFIG_FILE_NOT_FOUND, 
                    "Failed to load YAML configuration from: " + path, e);
            return Result.failure(ErrorCode.CONFIG_FILE_NOT_FOUND, 
                    "Failed to load YAML configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate a configuration object
     * This is a placeholder for future validation of configuration objects
     * 
     * @param config The configuration object to validate
     * @param <T> Type of the configuration object
     * @return The validated configuration object
     * @throws ConfigException If the configuration is invalid
     */
    public static <T> T validate(T config) {
        ErrorHandler.validateNotNull(config, ErrorCode.CONFIG_VALIDATION_ERROR, "Configuration cannot be null");
        // In the future, we can add more validation logic here
        return config;
    }
    
    /**
     * Safely validate a configuration object
     * This is a placeholder for future validation of configuration objects
     * 
     * @param config The configuration object to validate
     * @param <T> Type of the configuration object
     * @return A Result containing the validated configuration object or an error
     */
    public static <T> Result<T> safeValidate(T config) {
        if (config == null) {
            return Result.failure(ErrorCode.CONFIG_VALIDATION_ERROR, "Configuration cannot be null");
        }
        // In the future, we can add more validation logic here
        return Result.success(config);
    }
}
