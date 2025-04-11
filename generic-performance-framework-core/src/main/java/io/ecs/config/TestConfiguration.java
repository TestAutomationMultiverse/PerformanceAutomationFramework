package io.ecs.config;

import io.ecs.model.ExecutionConfig;
import io.ecs.config.Scenario;
import io.ecs.engine.Protocol;
import io.ecs.util.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the parsed test configuration
 */
public class TestConfiguration {
    private Protocol protocol;
    private String protocolName; // String representation of protocol
    private String engine;
    private String dataSource;
    private ExecutionConfig executionConfig;
    private List<Scenario> scenarios;
    private Map<String, String> variables;
    
    public TestConfiguration() {
        scenarios = new ArrayList<>();
        variables = new HashMap<>();
    }
    
    public Protocol getProtocol() {
        return protocol;
    }
    
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
        if (protocol != null) {
            this.protocolName = protocol.getName();
        }
    }
    
    public String getProtocolName() {
        return protocolName;
    }
    
    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }
    
    public String getEngine() {
        return engine;
    }
    
    public void setEngine(String engine) {
        this.engine = engine;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        // If the path is just a filename or doesn't exist, resolve it to the appropriate path
        this.dataSource = dataSource != null ? FileUtils.resolveFilePath(dataSource) : null;
    }
    
    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }
    
    public void setExecutionConfig(ExecutionConfig executionConfig) {
        this.executionConfig = executionConfig;
    }
    
    public List<Scenario> getScenarios() {
        return scenarios;
    }
    
    /**
     * Get scenarios converted to model.Scenario objects
     * 
     * @return List of model.Scenario objects
     */
    public List<io.ecs.model.Scenario> getModelScenarios() {
        List<io.ecs.model.Scenario> modelScenarios = new ArrayList<>();
        
        for (Scenario configScenario : scenarios) {
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
            
            modelScenarios.add(modelScenario);
        }
        
        return modelScenarios;
    }
    
    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }
    
    public void addScenario(Scenario scenario) {
        this.scenarios.add(scenario);
    }
    
    public Map<String, String> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, String> variables) {
        if (variables != null) {
            Map<String, String> resolvedVariables = new HashMap<>();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String value = entry.getValue();
                // Check if this variable might be a file path (like defaultHeaders)
                if (value != null && 
                   (entry.getKey().contains("Headers") || entry.getKey().contains("headers") ||
                    entry.getKey().contains("Body") || entry.getKey().contains("body") ||
                    entry.getKey().contains("File") || entry.getKey().contains("file") ||
                    entry.getKey().contains("Template") || entry.getKey().contains("template") ||
                    entry.getKey().contains("Schema") || entry.getKey().contains("schema"))) {
                    resolvedVariables.put(entry.getKey(), FileUtils.resolveFilePath(value));
                } else {
                    resolvedVariables.put(entry.getKey(), value);
                }
            }
            this.variables = resolvedVariables;
        } else {
            this.variables = variables;
        }
    }
    
    /**
     * Get a variable value
     * 
     * @param name the variable name
     * @return the variable value, or null if not found
     */
    public String getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Set a variable value
     * 
     * @param name the variable name
     * @param value the variable value
     */
    public void setVariable(String name, String value) {
        // Check if this variable might be a file path (like defaultHeaders)
        if (value != null && 
           (name.contains("Headers") || name.contains("headers") ||
            name.contains("Body") || name.contains("body") ||
            name.contains("File") || name.contains("file") ||
            name.contains("Template") || name.contains("template") ||
            name.contains("Schema") || name.contains("schema"))) {
            variables.put(name, FileUtils.resolveFilePath(value));
        } else {
            variables.put(name, value);
        }
    }
    
    @Override
    public String toString() {
        return "TestConfiguration{" +
                "protocol=" + protocol +
                ", protocolName='" + protocolName + '\'' +
                ", engine='" + engine + '\'' +
                ", dataSource='" + dataSource + '\'' +
                ", executionConfig=" + executionConfig +
                ", variables=" + variables +
                ", scenarios=" + scenarios +
                '}';
    }
}
