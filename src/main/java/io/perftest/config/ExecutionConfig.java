package io.perftest.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration settings for test execution
 */
public class ExecutionConfig {
    private int threads = 1;
    private int iterations = 1;
    private int rampUpSeconds = 0;
    private int holdSeconds = 0;
    private int duration = 60;
    private boolean useDuration = false;
    private String reportDirectory = "target/reports";
    private Map<String, String> variables = new HashMap<>();
    
    public int getThreads() {
        return threads;
    }
    
    public void setThreads(int threads) {
        this.threads = threads > 0 ? threads : 1;
    }
    
    public int getIterations() {
        return iterations;
    }
    
    public void setIterations(int iterations) {
        this.iterations = iterations > 0 ? iterations : 1;
    }
    
    public int getRampUpSeconds() {
        return rampUpSeconds;
    }
    
    public void setRampUpSeconds(int rampUpSeconds) {
        this.rampUpSeconds = rampUpSeconds >= 0 ? rampUpSeconds : 0;
    }
    
    public int getHoldSeconds() {
        return holdSeconds;
    }
    
    public void setHoldSeconds(int holdSeconds) {
        this.holdSeconds = holdSeconds >= 0 ? holdSeconds : 0;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration > 0 ? duration : 60;
    }
    
    public boolean isUseDuration() {
        return useDuration;
    }
    
    public void setUseDuration(boolean useDuration) {
        this.useDuration = useDuration;
    }
    
    public String getReportDirectory() {
        return reportDirectory;
    }
    
    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory != null && !reportDirectory.isEmpty() 
            ? reportDirectory : "target/reports";
    }
    
    public Map<String, String> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, String> variables) {
        this.variables = variables != null ? variables : new HashMap<>();
    }
    
    @Override
    public String toString() {
        return "ExecutionConfig{" +
                "threads=" + threads +
                ", iterations=" + iterations +
                ", rampUpSeconds=" + rampUpSeconds +
                ", holdSeconds=" + holdSeconds +
                ", duration=" + duration +
                ", useDuration=" + useDuration +
                ", reportDirectory='" + reportDirectory + '\'' +
                '}';
    }
}