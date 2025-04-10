package io.perftest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a test scenario
 */
public class Scenario {
    private String name;
    private List<Request> requests;
    private Map<String, String> variables;
    private Map<String, String> dataFiles;
    private int threads = 1;
    private int iterations = 1;
    private int rampUp = 0;
    private int hold = 0;
    private String engine;
    
    public Scenario() {
        requests = new ArrayList<>();
        variables = new HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
