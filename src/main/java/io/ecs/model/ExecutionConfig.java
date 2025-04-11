package io.ecs.model;

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
    
    // Report directory for JMeter reports
    private String reportDirectory = "target/reports";
    
    public String getReportDirectory() {
        return reportDirectory;
    }
    
    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory;
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
