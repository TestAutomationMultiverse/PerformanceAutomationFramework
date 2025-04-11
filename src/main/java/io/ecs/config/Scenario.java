package io.ecs.config;

import io.ecs.model.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a test scenario in the YAML configuration
 */
public class Scenario {
    private String name;
    private int threads = 1;
    private int iterations = 1;
    private int rampUp = 0;
    private int hold = 0;
    private String engine;
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
        this.variables = variables;
    }

    public Map<String, String> getDataFiles() {
        return dataFiles;
    }

    public void setDataFiles(Map<String, String> dataFiles) {
        this.dataFiles = dataFiles;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
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