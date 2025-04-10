package io.perftest.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for test execution
 */
public class ExecutionConfig {
    private int threads = 1;
    private int iterations = 1;
    private int rampUpSeconds = 0;
    private int holdSeconds = 0;
    private Map<String, String> variables = new HashMap<>();
    
    public ExecutionConfig() {
    }
    
    public ExecutionConfig(int threads, int iterations) {
        this.threads = threads;
        this.iterations = iterations;
    }
    
    public ExecutionConfig(int threads, int iterations, int rampUpSeconds, int holdSeconds) {
        this.threads = threads;
        this.iterations = iterations;
        this.rampUpSeconds = rampUpSeconds;
        this.holdSeconds = holdSeconds;
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
    
    public Map<String, String> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
    
    public void addVariable(String name, String value) {
        this.variables.put(name, value);
    }
    
    @Override
    public String toString() {
        return "ExecutionConfig{" +
                "threads=" + threads +
                ", iterations=" + iterations +
                ", rampUpSeconds=" + rampUpSeconds +
                ", holdSeconds=" + holdSeconds +
                ", variables=" + variables.size() +
                '}';
    }
}