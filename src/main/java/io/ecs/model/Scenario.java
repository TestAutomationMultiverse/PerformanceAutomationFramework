package io.ecs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a test scenario
 */
public class Scenario {
    private String id;
    private String name;
    private String description;
    private List<Request> requests;
    private Map<String, String> variables;
    private Map<String, String> dataFiles;
    private int threads = 1;
    private int iterations = 1;
    private int rampUp = 0;
    private int hold = 0;
    private String engine;
    private double successThreshold = 100.0; // Default success threshold is 100%
    
    public Scenario() {
        this.id = UUID.randomUUID().toString();
        this.requests = new ArrayList<>();
        this.variables = new HashMap<>();
        this.dataFiles = new HashMap<>();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<Request> getRequests() {
        return requests;
    }
    
    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }
    
    public void addRequest(Request request) {
        this.requests.add(request);
    }
    
    public Map<String, String> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
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
        variables.put(name, value);
    }

    public Map<String, String> getDataFiles() {
        if (dataFiles == null) {
            dataFiles = new HashMap<>();
        }
        return dataFiles;
    }

    public void setDataFiles(Map<String, String> dataFiles) {
        this.dataFiles = dataFiles;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getRampUp() {
        return rampUp;
    }

    public void setRampUp(int rampUp) {
        this.rampUp = rampUp;
    }

    public int getHold() {
        return hold;
    }

    public void setHold(int hold) {
        this.hold = hold;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }
    
    /**
     * Get the success threshold percentage for this scenario
     * 
     * @return Success threshold as a percentage (0-100)
     */
    public double getSuccessThreshold() {
        return successThreshold;
    }
    
    /**
     * Set the success threshold percentage for this scenario
     * 
     * @param successThreshold Success threshold as a percentage (0-100)
     */
    public void setSuccessThreshold(double successThreshold) {
        this.successThreshold = successThreshold;
    }
    
    @Override
    public String toString() {
        return "Scenario{" +
                "name='" + name + '\'' +
                ", threads=" + threads +
                ", iterations=" + iterations +
                ", rampUp=" + rampUp +
                ", engine=" + engine +
                ", variables=" + variables +
                ", requests=" + requests.size() +
                '}';
    }
}
