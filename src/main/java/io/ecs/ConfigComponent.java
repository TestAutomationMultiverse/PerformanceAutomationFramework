package io.ecs;

import io.ecs.model.Scenario;
import io.ecs.model.Request;
import io.ecs.model.ExecutionConfig;
import io.ecs.config.YamlConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ECS Component for handling test configuration loading and management
 * 
 * This component follows the Entity-Component-System pattern by providing
 * configuration capabilities that can be applied to scenario entities.
 */
public class ConfigComponent {
    private static final Logger LOGGER = Logger.getLogger(ConfigComponent.class.getName());
    
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
     * Create a new ConfigComponent with specific variables
     * 
     * @param variables Initial global variables
     */
    public ConfigComponent(Map<String, String> variables) {
        this.globalVariables = new HashMap<>(variables);
        this.loadedScenarios = new ArrayList<>();
        this.yamlConfig = new YamlConfig();
    }
    
    /**
     * Load scenarios from a YAML configuration file
     * 
     * @param configFile Path to YAML configuration file
     * @return List of loaded scenarios
     */
    public List<Scenario> loadFromYaml(String configFile) {
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
                            loadedScenarios.add(scenario);
                        }
                    }
                } else if (scenariosObj instanceof Map) {
                    // Handle scenarios as map (backward compatibility)
                    Map<String, Object> scenariosMap = (Map<String, Object>) scenariosObj;
                    
                    for (Map.Entry<String, Object> entry : scenariosMap.entrySet()) {
                        String scenarioName = entry.getKey();
                        if (entry.getValue() instanceof Map) {
                            Map<String, Object> scenarioConfig = (Map<String, Object>) entry.getValue();
                            
                            Scenario scenario = processScenarioConfig(scenarioConfig);
                            if (scenario != null) {
                                scenario.setName(scenarioName); // Override with map key
                                scenarios.add(scenario);
                                loadedScenarios.add(scenario);
                            }
                        }
                    }
                }
            }
            
            LOGGER.info("Loaded " + scenarios.size() + " scenarios from " + configFile);
            return scenarios;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading configuration from " + configFile + ": " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Process a scenario configuration map into a Scenario object
     * 
     * @param scenarioConfig Scenario configuration map
     * @return Scenario object
     */
    private Scenario processScenarioConfig(Map<String, Object> scenarioConfig) {
        try {
            Scenario scenario = new Scenario();
            
            // Set name
            if (scenarioConfig.containsKey("name")) {
                scenario.setName(String.valueOf(scenarioConfig.get("name")));
            } else {
                scenario.setName("Unnamed Scenario");
            }
            
            // Set description
            if (scenarioConfig.containsKey("description")) {
                scenario.setDescription(String.valueOf(scenarioConfig.get("description")));
            }
            
            // Set execution properties
            if (scenarioConfig.containsKey("threads")) {
                scenario.setThreads(Integer.parseInt(String.valueOf(scenarioConfig.get("threads"))));
            }
            if (scenarioConfig.containsKey("iterations")) {
                scenario.setIterations(Integer.parseInt(String.valueOf(scenarioConfig.get("iterations"))));
            }
            if (scenarioConfig.containsKey("rampUp")) {
                scenario.setRampUp(Integer.parseInt(String.valueOf(scenarioConfig.get("rampUp"))));
            }
            if (scenarioConfig.containsKey("hold")) {
                scenario.setHold(Integer.parseInt(String.valueOf(scenarioConfig.get("hold"))));
            }
            if (scenarioConfig.containsKey("successThreshold")) {
                scenario.setSuccessThreshold(Double.parseDouble(String.valueOf(scenarioConfig.get("successThreshold"))));
            }
            
            // Set engine
            if (scenarioConfig.containsKey("engine")) {
                scenario.setEngine(String.valueOf(scenarioConfig.get("engine")));
            }
            
            // Process variables
            if (scenarioConfig.containsKey("variables") && scenarioConfig.get("variables") instanceof Map) {
                Map<String, Object> varMap = (Map<String, Object>) scenarioConfig.get("variables");
                Map<String, String> variables = new HashMap<>();
                for (Map.Entry<String, Object> entry : varMap.entrySet()) {
                    variables.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                scenario.setVariables(variables);
            }
            
            // Process requests
            if (scenarioConfig.containsKey("requests") && scenarioConfig.get("requests") instanceof List) {
                List<Map<String, Object>> requestsList = (List<Map<String, Object>>) scenarioConfig.get("requests");
                for (Map<String, Object> requestMap : requestsList) {
                    Request request = processRequestConfig(requestMap);
                    if (request != null) {
                        scenario.addRequest(request);
                    }
                }
            }
            
            return scenario;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing scenario config: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Process a request configuration map into a Request object
     * 
     * @param requestMap Request configuration map
     * @return Request object
     */
    private Request processRequestConfig(Map<String, Object> requestMap) {
        try {
            Request request = new Request();
            
            // Set basic properties
            if (requestMap.containsKey("name")) {
                request.setName(String.valueOf(requestMap.get("name")));
            } else {
                request.setName("Unnamed Request");
            }
            
            if (requestMap.containsKey("endpoint")) {
                request.setEndpoint(String.valueOf(requestMap.get("endpoint")));
            }
            
            if (requestMap.containsKey("method")) {
                request.setMethod(String.valueOf(requestMap.get("method")));
            } else {
                request.setMethod("GET");
            }
            
            if (requestMap.containsKey("protocol")) {
                request.setProtocol(String.valueOf(requestMap.get("protocol")));
            }
            
            if (requestMap.containsKey("body")) {
                request.setBody(String.valueOf(requestMap.get("body")));
            }
            
            // Process headers
            if (requestMap.containsKey("headers") && requestMap.get("headers") instanceof Map) {
                Map<String, Object> headersMap = (Map<String, Object>) requestMap.get("headers");
                Map<String, String> headers = new HashMap<>();
                for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
                    headers.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                request.setHeaders(headers);
            }
            
            // Process parameters
            if (requestMap.containsKey("params") && requestMap.get("params") instanceof Map) {
                Map<String, Object> paramsMap = (Map<String, Object>) requestMap.get("params");
                Map<String, String> params = new HashMap<>();
                for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                    params.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                request.setParams(params);
            }
            
            // Process variables
            if (requestMap.containsKey("variables") && requestMap.get("variables") instanceof Map) {
                Map<String, Object> varMap = (Map<String, Object>) requestMap.get("variables");
                Map<String, String> variables = new HashMap<>();
                for (Map.Entry<String, Object> entry : varMap.entrySet()) {
                    variables.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                request.setVariables(variables);
            }
            
            return request;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing request config: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Set global variables for all scenarios
     * 
     * @param variables Variables to set
     * @return This component for chaining
     */
    public ConfigComponent setGlobalVariables(Map<String, String> variables) {
        if (variables != null) {
            globalVariables.putAll(variables);
        }
        return this;
    }
    
    /**
     * Add a single global variable
     * 
     * @param name Variable name
     * @param value Variable value
     * @return This component for chaining
     */
    public ConfigComponent addGlobalVariable(String name, String value) {
        globalVariables.put(name, value);
        return this;
    }
    
    /**
     * Get the global variables
     * 
     * @return Map of global variables
     */
    public Map<String, String> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }
    
    /**
     * Get loaded scenarios
     * 
     * @return List of loaded scenarios
     */
    public List<Scenario> getLoadedScenarios() {
        return new ArrayList<>(loadedScenarios);
    }
    
    /**
     * Create execution config from a scenario
     * 
     * @param scenario The scenario
     * @param reportDirectory Directory for test reports
     * @return Execution configuration
     */
    public ExecutionConfig createExecutionConfig(Scenario scenario, String reportDirectory) {
        ExecutionConfig config = new ExecutionConfig();
        config.setThreads(scenario.getThreads());
        config.setIterations(scenario.getIterations());
        config.setRampUpSeconds(scenario.getRampUp());
        config.setHoldSeconds(scenario.getHold());
        config.setReportDirectory(reportDirectory);
        
        // Combine global variables with scenario variables
        Map<String, String> combinedVars = new HashMap<>(globalVariables);
        if (scenario.getVariables() != null) {
            combinedVars.putAll(scenario.getVariables());
        }
        config.setVariables(combinedVars);
        
        // Set success threshold from scenario if available
        if (scenario.getSuccessThreshold() > 0) {
            config.setSuccessThreshold(scenario.getSuccessThreshold());
        }
        
        return config;
    }
    
    /**
     * Add a new scenario to the loaded list
     * 
     * @param scenario Scenario to add
     */
    public void addScenario(Scenario scenario) {
        loadedScenarios.add(scenario);
    }
    
    /**
     * Get a specific scenario by name
     * 
     * @param name Scenario name
     * @return Scenario if found, null otherwise
     */
    public Scenario getScenarioByName(String name) {
        return loadedScenarios.stream()
            .filter(s -> s.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}