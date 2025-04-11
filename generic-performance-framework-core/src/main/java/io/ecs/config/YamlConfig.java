package io.ecs.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.ecs.model.ExecutionConfig;
import io.ecs.model.Request;
import io.ecs.config.Scenario;
import io.ecs.engine.ProtocolFactory;
import io.ecs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Handles parsing of YAML configuration files
 */
public class YamlConfig {
    
    private final ObjectMapper mapper;
    
    public YamlConfig() {
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules(); // Enable full processing of YAML features
    }
    
    // Global variables from the configuration
    private Map<String, String> globalVariables = new HashMap<>();
    
    /**
     * Parse YAML configuration file into TestConfiguration object
     * 
     * @param configFile the path to the YAML configuration file
     * @return TestConfiguration object
     * @throws IOException if parsing fails
     */
    public TestConfiguration parseFile(String configFile) throws IOException {
        String yamlContent = FileUtils.readFileAsString(configFile);
        return parse(yamlContent);
    }
    
    /**
     * Parse YAML configuration string into TestConfiguration object
     * 
     * @param yamlContent the YAML content as string
     * @return TestConfiguration object
     * @throws IOException if parsing fails
     */
    /**
     * Parse YAML configuration string for ECS components
     * 
     * @param configFile Path to YAML configuration file
     * @return Configuration map
     * @throws IOException if parsing fails
     */
    public Map<String, Object> parseConfig(String configFile) throws IOException {
        String yamlContent = FileUtils.readFileAsString(configFile);
        JsonNode rootNode = mapper.readTree(yamlContent);
        Map<String, Object> configMap = mapper.convertValue(rootNode, HashMap.class);
        
        // Extract global variables
        if (configMap.containsKey("variables")) {
            Map<String, Object> variablesMap = (Map<String, Object>) configMap.get("variables");
            for (Map.Entry<String, Object> entry : variablesMap.entrySet()) {
                globalVariables.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        
        return configMap;
    }
    
    /**
     * Get global variables from the configuration
     * 
     * @return Map of global variables
     */
    public Map<String, String> getGlobalVariables() {
        return globalVariables;
    }
    
    public TestConfiguration parse(String yamlContent) throws IOException {
        JsonNode rootNode = mapper.readTree(yamlContent);
        Map<String, Object> configMap = mapper.convertValue(rootNode, HashMap.class);
        
        TestConfiguration config = new TestConfiguration();
        
        // Set protocol
        String protocolName = getStringValue(configMap, "protocol", "http");
        config.setProtocolName(protocolName);
        config.setProtocol(ProtocolFactory.getProtocol(protocolName));
        
        // Set engine
        config.setEngine(getStringValue(configMap, "engine", "JMDSL"));
        
        // Set data source
        config.setDataSource(getStringValue(configMap, "data", null));
        
        // Set global variables
        if (configMap.containsKey("variables")) {
            Map<String, Object> variablesMap = (Map<String, Object>) configMap.get("variables");
            Map<String, String> variables = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : variablesMap.entrySet()) {
                variables.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            
            config.setVariables(variables);
        }
        
        // Parse execution configuration
        ExecutionConfig executionConfig = new ExecutionConfig();
        if (configMap.containsKey("execution")) {
            Map<String, Object> executionMap = (Map<String, Object>) configMap.get("execution");
            
            executionConfig.setThreads(getIntValue(executionMap, "threads", 1));
            executionConfig.setIterations(getIntValue(executionMap, "iterations", 1));
            executionConfig.setRampUpSeconds(getIntValue(executionMap, "rampUpSeconds", 0));
            executionConfig.setHoldSeconds(getIntValue(executionMap, "holdSeconds", 0));
            executionConfig.setDuration(getIntValue(executionMap, "duration", 60));
            executionConfig.setSuccessThreshold(getDoubleValue(executionMap, "successThreshold", 100.0));
        } else if (configMap.containsKey("executionConfig")) {
            Map<String, Object> executionMap = (Map<String, Object>) configMap.get("executionConfig");
            
            executionConfig.setThreads(getIntValue(executionMap, "threads", 1));
            executionConfig.setIterations(getIntValue(executionMap, "iterations", 1));
            executionConfig.setRampUpSeconds(getIntValue(executionMap, "rampUpSeconds", 0));
            executionConfig.setHoldSeconds(getIntValue(executionMap, "holdSeconds", 0));
            executionConfig.setDuration(getIntValue(executionMap, "duration", 60));
            executionConfig.setSuccessThreshold(getDoubleValue(executionMap, "successThreshold", 100.0));
            
            if (executionMap.containsKey("variables")) {
                Map<String, Object> variablesMap = (Map<String, Object>) executionMap.get("variables");
                Map<String, String> variables = new HashMap<>();
                
                for (Map.Entry<String, Object> entry : variablesMap.entrySet()) {
                    variables.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                
                executionConfig.setVariables(variables);
            }
        } else {
            // Set default values
            executionConfig.setThreads(1);
            executionConfig.setIterations(1);
            executionConfig.setRampUpSeconds(0);
            executionConfig.setHoldSeconds(0);
            executionConfig.setDuration(60);
        }
        
        config.setExecutionConfig(executionConfig);
        
        // Parse scenarios
        if (configMap.containsKey("scenarios")) {
            Object scenariosObj = configMap.get("scenarios");
            
            if (scenariosObj instanceof List) {
                // Handle scenarios as list
                List<Map<String, Object>> scenariosList = (List<Map<String, Object>>) scenariosObj;
                
                for (Map<String, Object> scenarioConfig : scenariosList) {
                    String scenarioName = getStringValue(scenarioConfig, "name", "Unnamed Scenario");
                    
                    Scenario scenario = new Scenario();
                    scenario.setName(scenarioName);
                    
                    // Add description if available
                    if (scenarioConfig.containsKey("description")) {
                        scenario.setDescription(getStringValue(scenarioConfig, "description", ""));
                    }
                    
                    // Set execution config properties if available
                    if (scenarioConfig.containsKey("threads")) {
                        scenario.setThreads(getIntValue(scenarioConfig, "threads", 1));
                    }
                    if (scenarioConfig.containsKey("iterations")) {
                        scenario.setIterations(getIntValue(scenarioConfig, "iterations", 1));
                    }
                    if (scenarioConfig.containsKey("rampUp")) {
                        scenario.setRampUp(getIntValue(scenarioConfig, "rampUp", 0));
                    }
                    if (scenarioConfig.containsKey("hold")) {
                        scenario.setHold(getIntValue(scenarioConfig, "hold", 0));
                    }
                    
                    // Set engine if specified
                    if (scenarioConfig.containsKey("engine")) {
                        scenario.setEngine(getStringValue(scenarioConfig, "engine", "jmdsl"));
                    }
                    
                    // Process scenario variables
                    if (scenarioConfig.containsKey("variables")) {
                        Map<String, Object> variablesMap = (Map<String, Object>) scenarioConfig.get("variables");
                        Map<String, String> variables = new HashMap<>();
                        
                        for (Map.Entry<String, Object> varEntry : variablesMap.entrySet()) {
                            variables.put(varEntry.getKey(), String.valueOf(varEntry.getValue()));
                        }
                        
                        scenario.setVariables(variables);
                    }
                    
                    // Process requests
                    if (scenarioConfig.containsKey("requests")) {
                        List<Map<String, Object>> requestsList = (List<Map<String, Object>>) scenarioConfig.get("requests");
                        
                        for (Map<String, Object> requestMap : requestsList) {
                            Request request = new Request();
                            request.setName(getStringValue(requestMap, "name", "Unnamed Request"));
                            request.setEndpoint(getStringValue(requestMap, "endpoint", "/"));
                            request.setMethod(getStringValue(requestMap, "method", "GET"));
                            request.setBodyTemplate(getStringValue(requestMap, "body", null));
                            request.setHeadersTemplate(getStringValue(requestMap, "headers", null));
                            request.setParamsTemplate(getStringValue(requestMap, "params", null));
                            
                            // Parse protocol-specific settings
                            if (requestMap.containsKey("protocol")) {
                                request.setProtocol(getStringValue(requestMap, "protocol", null));
                            } else {
                                request.setProtocol(protocolName); // Use global protocol if not specified
                            }
                            
                            // Parse request variables
                            if (requestMap.containsKey("variables")) {
                                Map<String, Object> variablesMap = (Map<String, Object>) requestMap.get("variables");
                                Map<String, String> variables = new HashMap<>();
                                
                                for (Map.Entry<String, Object> varEntry : variablesMap.entrySet()) {
                                    variables.put(varEntry.getKey(), String.valueOf(varEntry.getValue()));
                                }
                                
                                request.setVariables(variables);
                            }
                            
                            // Parse response validators
                            if (requestMap.containsKey("Responses") || requestMap.containsKey("responses")) {
                                Object responseObj = requestMap.containsKey("Responses") ? 
                                                  requestMap.get("Responses") : requestMap.get("responses");
                                
                                if (responseObj instanceof Map) {
                                    Map<String, Object> responsesMap = (Map<String, Object>) responseObj;
                                    Map<String, String> responseValidators = new HashMap<>();
                                    
                                    for (Map.Entry<String, Object> respEntry : responsesMap.entrySet()) {
                                        responseValidators.put(respEntry.getKey(), String.valueOf(respEntry.getValue()));
                                    }
                                    
                                    request.setResponseValidators(responseValidators);
                                }
                            }
                            
                            scenario.addRequest(request);
                        }
                    }
                    
                    config.addScenario(scenario);
                }
            } else if (scenariosObj instanceof Map) {
                // Handle scenarios as map (backward compatibility)
                Map<String, Object> scenariosMap = (Map<String, Object>) scenariosObj;
                
                for (Map.Entry<String, Object> entry : scenariosMap.entrySet()) {
                    String scenarioName = entry.getKey();
                    Map<String, Object> scenarioConfig = (Map<String, Object>) entry.getValue();
                    
                    Scenario scenario = new Scenario();
                    scenario.setName(scenarioName);
                    
                    // Parse scenario variables
                    if (scenarioConfig.containsKey("variables")) {
                        Map<String, Object> variablesMap = (Map<String, Object>) scenarioConfig.get("variables");
                        Map<String, String> variables = new HashMap<>();
                        
                        for (Map.Entry<String, Object> varEntry : variablesMap.entrySet()) {
                            variables.put(varEntry.getKey(), String.valueOf(varEntry.getValue()));
                        }
                        
                        scenario.setVariables(variables);
                    }
                    
                    // Parse requests
                    if (scenarioConfig.containsKey("requests")) {
                        List<Map<String, Object>> requestsList = (List<Map<String, Object>>) scenarioConfig.get("requests");
                        
                        for (Map<String, Object> requestMap : requestsList) {
                            Request request = new Request();
                            request.setName(getStringValue(requestMap, "name", "Unnamed Request"));
                            request.setEndpoint(getStringValue(requestMap, "endpoint", "/"));
                            request.setMethod(getStringValue(requestMap, "method", "GET"));
                            request.setBodyTemplate(getStringValue(requestMap, "body", null));
                            request.setHeadersTemplate(getStringValue(requestMap, "headers", null));
                            request.setParamsTemplate(getStringValue(requestMap, "params", null));
                            
                            // Parse protocol-specific settings
                            if (requestMap.containsKey("protocol")) {
                                request.setProtocol(getStringValue(requestMap, "protocol", null));
                            } else {
                                request.setProtocol(protocolName); // Use global protocol if not specified
                            }
                            
                            // Parse request variables
                            if (requestMap.containsKey("variables")) {
                                Map<String, Object> variablesMap = (Map<String, Object>) requestMap.get("variables");
                                Map<String, String> variables = new HashMap<>();
                                
                                for (Map.Entry<String, Object> varEntry : variablesMap.entrySet()) {
                                    variables.put(varEntry.getKey(), String.valueOf(varEntry.getValue()));
                                }
                                
                                request.setVariables(variables);
                            }
                            
                            // Parse response validators
                            if (requestMap.containsKey("Responses") || requestMap.containsKey("responses")) {
                                Object responseObj = requestMap.containsKey("Responses") ? 
                                                  requestMap.get("Responses") : requestMap.get("responses");
                                
                                if (responseObj instanceof Map) {
                                    Map<String, Object> responsesMap = (Map<String, Object>) responseObj;
                                    Map<String, String> responseValidators = new HashMap<>();
                                    
                                    for (Map.Entry<String, Object> respEntry : responsesMap.entrySet()) {
                                        responseValidators.put(respEntry.getKey(), String.valueOf(respEntry.getValue()));
                                    }
                                    
                                    request.setResponseValidators(responseValidators);
                                }
                            }
                            
                            scenario.addRequest(request);
                        }
                    }
                    
                    config.addScenario(scenario);
                }
            }
        }
        
        return config;
    }
    
    /**
     * Get a string value from a map, with default value if not found
     * 
     * @param map the map to get value from
     * @param key the key to get
     * @param defaultValue the default value if key not found
     * @return the string value
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map.containsKey(key) && map.get(key) != null) {
            return String.valueOf(map.get(key));
        }
        return defaultValue;
    }
    
    /**
     * Get an integer value from a map, with default value if not found
     * 
     * @param map the map to get value from
     * @param key the key to get
     * @param defaultValue the default value if key not found
     * @return the integer value
     */
    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        if (map.containsKey(key) && map.get(key) != null) {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    // Ignore and return default
                }
            }
        }
        return defaultValue;
    }
    
    /**
     * Get a double value from a map, with default value if not found
     * 
     * @param map the map to get value from
     * @param key the key to get
     * @param defaultValue the default value if key not found
     * @return the double value
     */
    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        if (map.containsKey(key) && map.get(key) != null) {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    // Ignore and return default
                }
            }
        }
        return defaultValue;
    }
}
