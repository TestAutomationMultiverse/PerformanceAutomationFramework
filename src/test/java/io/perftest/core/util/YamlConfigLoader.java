package io.perftest.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for loading YAML configuration files
 */
public class YamlConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(YamlConfigLoader.class);
    // Using an instance creation method to ensure compatibility with different Jackson versions
    private static final ObjectMapper yamlMapper = createYamlMapper();
    
    /**
     * Load a single YAML file from the file system or classpath
     * 
     * @param yamlPath Path to the YAML file
     * @return Map representing the YAML content
     * @throws IOException If the file cannot be read or parsed
     */
    public static Map<String, Object> loadYamlFile(String yamlPath) throws IOException {
        logger.info("Loading YAML file: {}", yamlPath);
        
        // Try as file path first
        File file = new File(yamlPath);
        if (file.exists() && file.isFile()) {
            return yamlMapper.readValue(file, Map.class);
        }
        
        // Try as classpath resource
        String resourcePath = yamlPath.startsWith("/") ? yamlPath : "/" + yamlPath;
        try (InputStream is = YamlConfigLoader.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return yamlMapper.readValue(is, Map.class);
            }
        }
        
        // Try as relative path to test resources
        Path relPath = Paths.get("src/test/resources", yamlPath);
        if (Files.exists(relPath)) {
            return yamlMapper.readValue(relPath.toFile(), Map.class);
        }
        
        throw new IOException("YAML file not found: " + yamlPath);
    }
    
    /**
     * Load multiple YAML documents from a single file
     * 
     * @param yamlPath Path to the multi-document YAML file
     * @return List of maps representing each YAML document
     * @throws IOException If the file cannot be read or parsed
     */
    public static List<Map<String, Object>> loadYamlDocuments(String yamlPath) throws IOException {
        logger.info("Loading multiple YAML documents from: {}", yamlPath);
        
        // Get the input stream from file or classpath
        InputStream inputStream = null;
        
        // Try as file path first
        File file = new File(yamlPath);
        if (file.exists() && file.isFile()) {
            inputStream = Files.newInputStream(file.toPath());
        } else {
            // Try as classpath resource
            String resourcePath = yamlPath.startsWith("/") ? yamlPath : "/" + yamlPath;
            inputStream = YamlConfigLoader.class.getResourceAsStream(resourcePath);
            
            // Try as relative path to test resources
            if (inputStream == null) {
                Path relPath = Paths.get("src/test/resources", yamlPath);
                if (Files.exists(relPath)) {
                    inputStream = Files.newInputStream(relPath);
                }
            }
        }
        
        if (inputStream == null) {
            throw new IOException("YAML file not found: " + yamlPath);
        }
        
        // Use a more compatible approach to read YAML documents
        List<Map<String, Object>> configs = new ArrayList<>();
        try {
            // Parse each document individually
            String content = new String(inputStream.readAllBytes());
            String[] documents = content.split("---");
            
            for (String document : documents) {
                if (document.trim().isEmpty()) {
                    continue;
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Object> config = yamlMapper.readValue(document, Map.class);
                configs.add(config);
            }
        } catch (Exception e) {
            logger.error("Error parsing YAML documents: {}", e.getMessage());
            throw new IOException("Failed to parse YAML documents: " + e.getMessage(), e);
        }
        
        inputStream.close();
        return configs;
    }
    
    /**
     * Merge multiple YAML configurations into a single map
     * 
     * @param configs List of configuration maps
     * @return Merged configuration map
     */
    public static Map<String, Object> mergeConfigs(List<Map<String, Object>> configs) {
        Map<String, Object> mergedConfig = new HashMap<>();
        
        for (Map<String, Object> config : configs) {
            // Deep merge the configurations
            mergeMapRecursive(mergedConfig, config);
        }
        
        return mergedConfig;
    }
    
    /**
     * Create a YAML ObjectMapper that works with different Jackson versions
     * @return Configured ObjectMapper for YAML
     */
    private static ObjectMapper createYamlMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
    
    /**
     * Recursively merge two maps
     * 
     * @param target Target map to merge into
     * @param source Source map to merge from
     */
    @SuppressWarnings("unchecked")
    private static void mergeMapRecursive(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // If both are maps, merge them recursively
            if (value instanceof Map && target.containsKey(key) && target.get(key) instanceof Map) {
                Map<String, Object> targetMap = (Map<String, Object>) target.get(key);
                Map<String, Object> sourceMap = (Map<String, Object>) value;
                mergeMapRecursive(targetMap, sourceMap);
            } 
            // If both are lists, combine them
            else if (value instanceof List && target.containsKey(key) && target.get(key) instanceof List) {
                List<Object> targetList = (List<Object>) target.get(key);
                List<Object> sourceList = (List<Object>) value;
                targetList.addAll(sourceList);
            } 
            // Otherwise, overwrite the value
            else {
                target.put(key, value);
            }
        }
    }
}