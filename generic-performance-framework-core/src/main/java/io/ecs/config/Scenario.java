package io.ecs.config;

import io.ecs.model.Request;
import io.ecs.util.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a test scenario in the YAML configuration
 */
public class Scenario {
    private String id;
    private String name;
    private String description;
    private int threads = 1;
    private int iterations = 1;
    private int rampUp = 0;
    private int hold = 0;
    private String engine;
    private double successThreshold = 100.0;
    private Map<String, String> variables = new HashMap<>();
    private Map<String, String> dataFiles = new HashMap<>();
    private List<Request> requests = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Map<String, String> getDataFiles() {
        return dataFiles;
    }

    public void setDataFiles(Map<String, String> dataFiles) {
        if (dataFiles != null) {
            Map<String, String> resolvedDataFiles = new HashMap<>();
            for (Map.Entry<String, String> entry : dataFiles.entrySet()) {
                String filePath = entry.getValue();
                // Resolve the file path if it's a file reference
                if (filePath != null) {
                    resolvedDataFiles.put(entry.getKey(), FileUtils.resolveFilePath(filePath));
                } else {
                    resolvedDataFiles.put(entry.getKey(), null);
                }
            }
            this.dataFiles = resolvedDataFiles;
        } else {
            this.dataFiles = dataFiles;
        }
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getSuccessThreshold() {
        return successThreshold;
    }
    
    public void setSuccessThreshold(double successThreshold) {
        this.successThreshold = successThreshold;
    }
    
    /**
     * Add a request to this scenario
     * 
     * @param request Request to add
     */
    public void addRequest(Request request) {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        requests.add(request);
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "name='" + name + '\'' +
                ", threads=" + threads +
                ", iterations=" + iterations +
                ", rampUp=" + rampUp +
                ", hold=" + hold +
                ", engine='" + engine + '\'' +
                ", requests=" + requests.size() +
                '}';
    }
}