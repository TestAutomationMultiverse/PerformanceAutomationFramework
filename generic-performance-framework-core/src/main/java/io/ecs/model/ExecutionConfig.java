package io.ecs.model;

import io.ecs.util.FileUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for test execution
 */
public class ExecutionConfig {
    private int threads;
    private int iterations;
    private int rampUpSeconds;
    private int holdSeconds;
    private int duration;
    private double successThreshold = 100.0; // Default to 100% for backward compatibility
    private Map<String, String> variables;
    
    public ExecutionConfig() {
        variables = new HashMap<>();
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
    
    public int getRampUpSeconds() {
        return rampUpSeconds;
    }
    
    public void setRampUpSeconds(int rampUpSeconds) {
        this.rampUpSeconds = rampUpSeconds;
    }
    
    public int getHoldSeconds() {
        return holdSeconds;
    }
    
    public void setHoldSeconds(int holdSeconds) {
        this.holdSeconds = holdSeconds;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public double getSuccessThreshold() {
        return successThreshold;
    }
    
    public void setSuccessThreshold(double successThreshold) {
        this.successThreshold = successThreshold;
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
    
    // Report directory for JMeter reports
    private String reportDirectory = "target/reports";
    
    public String getReportDirectory() {
        return reportDirectory;
    }
    
    public void setReportDirectory(String reportDirectory) {
        // Use the file path resolver to handle relative paths
        this.reportDirectory = reportDirectory != null ? FileUtils.resolveFilePath(reportDirectory) : reportDirectory;
    }
    
    @Override
    public String toString() {
        return "ExecutionConfig{" +
                "threads=" + threads +
                ", iterations=" + iterations +
                ", rampUpSeconds=" + rampUpSeconds +
                ", holdSeconds=" + holdSeconds +
                ", duration=" + duration +
                ", successThreshold=" + successThreshold +
                ", variables=" + variables +
                '}';
    }
}
