package io.ecs.component;

import io.ecs.config.Scenario;
import io.ecs.model.Request;
import io.ecs.model.ExecutionConfig;
import io.ecs.config.YamlConfig;
import io.ecs.model.Response;
import io.ecs.util.EcsLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ECS Component for handling test configuration loading and management
 * 
 * This component follows the Entity-Component-System pattern by providing
 * configuration capabilities that can be applied to scenario entities.
 */
public class ConfigComponent {
    private static final EcsLogger logger = EcsLogger.getLogger(ConfigComponent.class);
    
    private final Map<String, String> globalVariables;
    private final List<Scenario> loadedScenarios;
    private final YamlConfig yamlConfig;
    
    /**
     * Create a new ConfigComponent with default values
     */
    public ConfigComponent() {
        this.globalVariables = new HashMap<>();
        this.loadedScenarios = new ArrayList<>();
        this.yamlConfig = new YamlConfig();
    }
    
    /**
     * Create a new ConfigComponent with custom global variables
     * 
     * @param globalVariables Global variables map
     */
    public ConfigComponent(Map<String, String> globalVariables) {
        this.globalVariables = new HashMap<>(globalVariables);
        this.loadedScenarios = new ArrayList<>();
        this.yamlConfig = new YamlConfig();
    }
    
    /**
     * Load scenarios from a YAML configuration file
     * 
     * @param configFile Path to the YAML config file
     * @return This component for chaining
     * @throws IOException If the file cannot be read
     */
    public ConfigComponent loadConfig(String configFile) throws IOException {
        logger.info("Loading configuration from: {}", configFile);
        List<Scenario> scenarios = loadScenariosFromYaml(configFile, globalVariables);
        loadedScenarios.clear();
        loadedScenarios.addAll(scenarios);
        return this;
    }
    
    /**
     * Load scenarios from a YAML configuration file
     * 
     * @param configFile Path to the YAML config file
     * @param variables Variables for substitution
     * @return List of loaded scenarios
     * @throws IOException If the file cannot be read
     */
    public List<Scenario> loadScenariosFromYaml(String configFile, Map<String, String> variables) throws IOException {
        logger.info("Loading scenarios from YAML: {}", configFile);
        
        try {
            // Parse the YAML configuration
            Map<String, Object> configMap = yamlConfig.parseConfig(configFile);
            List<Scenario> scenarios = new ArrayList<>();
            
            // Extract global variables if present
            Map<String, String> configVars = yamlConfig.getGlobalVariables();
            if (configVars != null && !configVars.isEmpty()) {
                globalVariables.putAll(configVars);
            }
            
            // Process scenarios from the configuration
            if (configMap.containsKey("scenarios")) {
                Object scenariosObj = configMap.get("scenarios");
                
                if (scenariosObj instanceof List) {
                    // Handle scenarios as list
                    List<Map<String, Object>> scenariosList = (List<Map<String, Object>>) scenariosObj;
                    
                    for (Map<String, Object> scenarioConfig : scenariosList) {
                        Scenario scenario = processScenarioConfig(scenarioConfig);
                        if (scenario != null) {
                            scenarios.add(scenario);
                        }
                    }
                } else if (scenariosObj instanceof Map) {
                    // Handle scenarios as map (backward compatibility)
                    Map<String, Object> scenariosMap = (Map<String, Object>) scenariosObj;
                    
                    for (Map.Entry<String, Object> entry : scenariosMap.entrySet()) {
                        String scenarioName = entry.getKey();
                        Map<String, Object> scenarioConfig = (Map<String, Object>) entry.getValue();
                        
                        Scenario scenario = new Scenario();
                        scenario.setName(scenarioName);
                        
                        // Set configuration values
                        if (scenarioConfig.containsKey("threads")) {
                            scenario.setThreads(getIntValue(scenarioConfig, "threads", 1));
                        }
                        
                        if (scenarioConfig.containsKey("iterations")) {
                            scenario.setIterations(getIntValue(scenarioConfig, "iterations", 1));
                        }
                        
                        if (scenarioConfig.containsKey("rampUp")) {
                            scenario.setRampUp(getIntValue(scenarioConfig, "rampUp", 0));
                        }
                        
                        if (scenarioConfig.containsKey("successThreshold")) {
                            scenario.setSuccessThreshold(getDoubleValue(scenarioConfig, "successThreshold", 100.0));
                        }
                        
                        if (scenarioConfig.containsKey("engine")) {
                            scenario.setEngine(getStringValue(scenarioConfig, "engine", "jmdsl"));
                        }
                        
                        // Process requests
                        if (scenarioConfig.containsKey("requests")) {
                            List<Map<String, Object>> requestConfigs = 
                                (List<Map<String, Object>>) scenarioConfig.get("requests");
                            
                            List<Request> requests = new ArrayList<>();
                            for (Map<String, Object> requestConfig : requestConfigs) {
                                Request request = processRequestConfig(requestConfig);
                                if (request != null) {
                                    requests.add(request);
                                }
                            }
                            
                            scenario.setRequests(requests);
                        }
                        
                        // Process variables
                        if (scenarioConfig.containsKey("variables")) {
                            Map<String, String> scenarioVars = 
                                (Map<String, String>) scenarioConfig.get("variables");
                            scenario.setVariables(scenarioVars);
                        }
                        
                        // Generate ID for scenario if it doesn't have one
                        if (scenario.getId() == null) {
                            scenario.setId(UUID.randomUUID().toString());
                        }
                        
                        scenarios.add(scenario);
                    }
                }
            }
            
            return scenarios;
        } catch (Exception e) {
            logger.error("Error loading scenarios from YAML: {}", e.getMessage(), e);
            throw new IOException("Error parsing YAML configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process a scenario configuration map into a Scenario object
     * 
     * @param scenarioConfig Map containing scenario configuration
     * @return Scenario object
     */
    private Scenario processScenarioConfig(Map<String, Object> scenarioConfig) {
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
        
        if (scenarioConfig.containsKey("successThreshold")) {
            scenario.setSuccessThreshold(getDoubleValue(scenarioConfig, "successThreshold", 100.0));
        }
        
        if (scenarioConfig.containsKey("engine")) {
            scenario.setEngine(getStringValue(scenarioConfig, "engine", "jmdsl"));
        }
        
        // Process requests
        if (scenarioConfig.containsKey("requests")) {
            Object requestsObj = scenarioConfig.get("requests");
            List<Request> requests = new ArrayList<>();
            
            if (requestsObj instanceof List) {
                List<Map<String, Object>> requestConfigs = (List<Map<String, Object>>) requestsObj;
                
                for (Map<String, Object> requestConfig : requestConfigs) {
                    Request request = processRequestConfig(requestConfig);
                    if (request != null) {
                        requests.add(request);
                    }
                }
            }
            
            scenario.setRequests(requests);
        }
        
        // Process variables
        if (scenarioConfig.containsKey("variables")) {
            Object variablesObj = scenarioConfig.get("variables");
            
            if (variablesObj instanceof Map) {
                Map<String, String> variables = new HashMap<>();
                Map<String, Object> varsMap = (Map<String, Object>) variablesObj;
                
                for (Map.Entry<String, Object> entry : varsMap.entrySet()) {
                    variables.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                
                scenario.setVariables(variables);
            }
        }
        
        // Generate ID for scenario if it doesn't have one
        if (scenario.getId() == null) {
            scenario.setId(UUID.randomUUID().toString());
        }
        
        return scenario;
    }
    
    /**
     * Process a request configuration map into a Request object
     * 
     * @param requestConfig Map containing request configuration
     * @return Request object
     */
    private Request processRequestConfig(Map<String, Object> requestConfig) {
        String name = getStringValue(requestConfig, "name", "Unnamed Request");
        String url = getStringValue(requestConfig, "url", "");
        String method = getStringValue(requestConfig, "method", "GET");
        
        Request request = new Request();
        request.setName(name);
        // Parse URL to determine protocol and endpoint
        if (url != null) {
            if (url.startsWith("https://")) {
                request.setProtocol("HTTPS");
                request.setEndpoint(url.substring(8)); // Remove https://
            } else if (url.startsWith("http://")) {
                request.setProtocol("HTTP");
                request.setEndpoint(url.substring(7)); // Remove http://
            } else {
                request.setProtocol("HTTP"); // Default protocol
                request.setEndpoint(url);
            }
        }
        request.setMethod(method);
        
        // Add headers if present
        if (requestConfig.containsKey("headers")) {
            Object headersObj = requestConfig.get("headers");
            
            if (headersObj instanceof Map) {
                Map<String, String> headers = new HashMap<>();
                Map<String, Object> headersMap = (Map<String, Object>) headersObj;
                
                for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
                    headers.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                
                request.setHeaders(headers);
            }
        }
        
        // Add body if present
        if (requestConfig.containsKey("body")) {
            request.setBody(requestConfig.get("body"));
        }
        
        // Add request parameters
        if (requestConfig.containsKey("parameters")) {
            Object paramsObj = requestConfig.get("parameters");
            
            if (paramsObj instanceof Map) {
                Map<String, String> params = new HashMap<>();
                Map<String, Object> paramsMap = (Map<String, Object>) paramsObj;
                
                for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                    params.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                
                request.setParameters(params);
            }
        }
        
        return request;
    }
    
    /**
     * Get a string value from a configuration map
     * 
     * @param map The configuration map
     * @param key The key to look up
     * @param defaultValue Default value if key not found
     * @return The string value or default
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            return value != null ? String.valueOf(value) : defaultValue;
        }
        return defaultValue;
    }
    
    /**
     * Get an integer value from a configuration map
     * 
     * @param map The configuration map
     * @param key The key to look up
     * @param defaultValue Default value if key not found
     * @return The integer value or default
     */
    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }
    
    /**
     * Get a double value from a configuration map
     * 
     * @param map The configuration map
     * @param key The key to look up
     * @param defaultValue Default value if key not found
     * @return The double value or default
     */
    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }
    
    /**
     * Get the loaded scenarios
     * 
     * @return List of loaded scenarios
     */
    public List<Scenario> getScenarios() {
        return new ArrayList<>(loadedScenarios);
    }
    
    /**
     * Convert between config.Scenario and model.Scenario
     * 
     * @param configScenario The source scenario from config package
     * @return A new model.Scenario with the same properties
     */
    public io.ecs.model.Scenario convertToModelScenario(io.ecs.config.Scenario configScenario) {
        io.ecs.model.Scenario modelScenario = new io.ecs.model.Scenario();
        
        // Copy basic properties
        modelScenario.setId(configScenario.getId());
        modelScenario.setName(configScenario.getName());
        modelScenario.setDescription(configScenario.getDescription());
        modelScenario.setThreads(configScenario.getThreads());
        modelScenario.setIterations(configScenario.getIterations());
        modelScenario.setRampUp(configScenario.getRampUp());
        modelScenario.setHold(configScenario.getHold());
        modelScenario.setEngine(configScenario.getEngine());
        modelScenario.setSuccessThreshold(configScenario.getSuccessThreshold());
        
        // Copy collections
        modelScenario.setRequests(new ArrayList<>(configScenario.getRequests()));
        modelScenario.setVariables(new HashMap<>(configScenario.getVariables()));
        modelScenario.setDataFiles(new HashMap<>(configScenario.getDataFiles()));
        
        return modelScenario;
    }
    
    /**
     * Convert a list of config.Scenario objects to model.Scenario objects
     * 
     * @param configScenarios List of config.Scenario objects
     * @return List of model.Scenario objects
     */
    public List<io.ecs.model.Scenario> convertToModelScenarios(List<io.ecs.config.Scenario> configScenarios) {
        List<io.ecs.model.Scenario> modelScenarios = new ArrayList<>();
        for (io.ecs.config.Scenario configScenario : configScenarios) {
            modelScenarios.add(convertToModelScenario(configScenario));
        }
        return modelScenarios;
    }
    
    /**
     * Get a specific scenario by name
     * 
     * @param name Scenario name
     * @return The scenario or null if not found
     */
    public Scenario getScenario(String name) {
        return loadedScenarios.stream()
            .filter(s -> s.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Add a scenario to this component
     * 
     * @param scenario Scenario to add
     * @return This component for chaining
     */
    public ConfigComponent addScenario(Scenario scenario) {
        if (scenario != null) {
            // Generate ID for scenario if it doesn't have one
            if (scenario.getId() == null) {
                scenario.setId(UUID.randomUUID().toString());
            }
            
            // Replace existing scenario with same name if present
            for (int i = 0; i < loadedScenarios.size(); i++) {
                if (loadedScenarios.get(i).getName().equals(scenario.getName())) {
                    loadedScenarios.set(i, scenario);
                    return this;
                }
            }
            
            // Add new scenario
            loadedScenarios.add(scenario);
        }
        return this;
    }
    
    /**
     * Remove a scenario by name
     * 
     * @param name Name of the scenario to remove
     * @return True if removed, false if not found
     */
    public boolean removeScenario(String name) {
        return loadedScenarios.removeIf(s -> s.getName().equals(name));
    }
    
    /**
     * Get the global variables map
     * 
     * @return Global variables map
     */
    public Map<String, String> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }
    
    /**
     * Add a global variable
     * 
     * @param key Variable key
     * @param value Variable value
     * @return This component for chaining
     */
    public ConfigComponent addGlobalVariable(String key, String value) {
        globalVariables.put(key, value);
        return this;
    }
    
    /**
     * Add multiple global variables
     * 
     * @param variables Map of variables to add
     * @return This component for chaining
     */
    public ConfigComponent addGlobalVariables(Map<String, String> variables) {
        globalVariables.putAll(variables);
        return this;
    }
    
    /**
     * Clear all global variables
     * 
     * @return This component for chaining
     */
    public ConfigComponent clearGlobalVariables() {
        globalVariables.clear();
        return this;
    }
    
    /**
     * Get the YAML configuration utility
     * 
     * @return YAML config utility
     */
    public YamlConfig getYamlConfig() {
        return yamlConfig;
    }
    
    /**
     * Parse a YAML configuration into a map
     * 
     * @param configFile Path to the config file
     * @return Map of configuration values
     * @throws IOException If parsing fails
     */
    public Map<String, Object> parseConfig(String configFile) throws IOException {
        return yamlConfig.parseConfig(configFile);
    }
}