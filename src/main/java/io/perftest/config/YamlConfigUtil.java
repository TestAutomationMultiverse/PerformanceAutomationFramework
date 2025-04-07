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
 * Utility class for loading and validating YAML configuration files.
 * 
 * <p>This class provides methods to load YAML configuration files from the classpath
 * and convert them to strongly-typed Java objects using Jackson's YAML parser.</p>
 * 
 * <p>It offers both exception-throwing and safe (Result-based) methods for loading
 * and validating configurations, allowing for flexible error handling strategies.</p>
 * 
 * <p>Example usage for exception-based approach:</p>
 * <pre>
 * try {
 *     HttpConfig config = YamlConfigUtil.load("http-config.yml", HttpConfig.class);
 *     YamlConfigUtil.validate(config);
 *     // Use the config object
 * } catch (ConfigException e) {
 *     // Handle configuration errors
 * } catch (IOException e) {
 *     // Handle I/O errors
 * }
 * </pre>
 * 
 * <p>Example usage for safe (Result-based) approach:</p>
 * <pre>
 * Result&lt;HttpConfig&gt; result = YamlConfigUtil.safeLoad("http-config.yml", HttpConfig.class);
 * if (result.isSuccess()) {
 *     HttpConfig config = result.getValue();
 *     Result&lt;HttpConfig&gt; validationResult = YamlConfigUtil.safeValidate(config);
 *     if (validationResult.isSuccess()) {
 *         // Use the validated config object
 *     } else {
 *         // Handle validation error
 *         ErrorCode errorCode = validationResult.getErrorCode();
 *         String errorMessage = validationResult.getErrorMessage();
 *     }
 * } else {
 *     // Handle loading error
 *     ErrorCode errorCode = result.getErrorCode();
 *     String errorMessage = result.getErrorMessage();
 * }
 * </pre>
 * 
 * @since 1.0
 */
@Slf4j
public class YamlConfigUtil {
    
    /** Jackson ObjectMapper configured with YAML factory for parsing YAML files */
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private YamlConfigUtil() {
        // Utility class should not be instantiated
    }
    
    /**
     * Loads a YAML configuration file from the classpath and maps it to the specified class.
     * 
     * <p>This method throws exceptions if any error occurs during loading or parsing,
     * making it suitable for use cases where you want immediate failure on errors.</p>
     * 
     * @param <T> The type of configuration object to return
     * @param path Path to the YAML file in the classpath
     * @param configClass Class to map the YAML content to
     * @return The parsed configuration object
     * @throws ConfigException If the configuration file cannot be found or is invalid
     * @throws IOException If an I/O error occurs while reading the file
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
        }, ErrorCode.CONFIG_FILE_NOT_FOUND, "Failed to load YAML configuration from: " + path, log);
    }
    
    /**
     * Safely loads a YAML configuration file from the classpath and maps it to the specified class.
     * 
     * <p>This method returns a Result object instead of throwing exceptions, making it
     * suitable for use cases where you want to handle errors in a more controlled way.</p>
     * 
     * @param <T> The type of configuration object to return
     * @param path Path to the YAML file in the classpath
     * @param configClass Class to map the YAML content to
     * @return A Result containing either the parsed configuration object or error information
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
     * Validates a configuration object.
     * 
     * <p>This method checks that the configuration is not null and can be extended
     * in the future to perform more complex validation rules specific to each
     * configuration type.</p>
     * 
     * <p>It throws exceptions if validation fails, making it suitable for use cases
     * where you want immediate failure on validation errors.</p>
     * 
     * @param <T> The type of configuration object to validate
     * @param config The configuration object to validate
     * @return The validated configuration object (for method chaining)
     * @throws ConfigException If validation fails
     */
    public static <T> T validate(T config) {
        ErrorHandler.validateNotNull(config, ErrorCode.CONFIG_VALIDATION_ERROR, "Configuration cannot be null");
        
        // Additional validation can be added here in the future
        // For example, checking required fields, validating values, etc.
        
        return config;
    }
    
    /**
     * Safely validates a configuration object.
     * 
     * <p>This method checks that the configuration is not null and can be extended
     * in the future to perform more complex validation rules specific to each
     * configuration type.</p>
     * 
     * <p>It returns a Result object instead of throwing exceptions, making it
     * suitable for use cases where you want to handle validation errors in a
     * more controlled way.</p>
     * 
     * @param <T> The type of configuration object to validate
     * @param config The configuration object to validate
     * @return A Result containing either the validated configuration object or error information
     */
    public static <T> Result<T> safeValidate(T config) {
        if (config == null) {
            return Result.failure(ErrorCode.CONFIG_VALIDATION_ERROR, "Configuration cannot be null");
        }
        
        // Additional validation can be added here in the future
        // For example, checking required fields, validating values, etc.
        
        return Result.success(config);
    }
    
    /**
     * Validates that a string is not null or empty.
     * 
     * <p>This is a utility method for validating string fields in configuration objects.</p>
     * 
     * @param value The string value to validate
     * @param errorCode The error code to use if validation fails
     * @param errorMessage The error message to use if validation fails
     * @throws ConfigException If the string is null or empty
     */
    public static void validateNotEmpty(String value, ErrorCode errorCode, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ConfigException(errorCode, errorMessage);
        }
    }
}
