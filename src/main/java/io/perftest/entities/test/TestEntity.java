package io.perftest.entities.test;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified TestEntity class that represents a performance test configuration
 */
public class TestEntity {
    private String name;
    private int threads;
    private int iterations;
    private Map<String, Object> variables = new HashMap<>();

    public TestEntity() {
    }

    public TestEntity(String name, int threads, int iterations) {
        this.name = name;
        this.threads = threads;
        this.iterations = iterations;
    }

    // Getters and Setters
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

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void addVariable(String key, Object value) {
        this.variables.put(key, value);
    }
}