package io.ecs.model;

import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

/**
 * Represents a result from a test execution
 * Tracks both the processed request and response details
 */
public class TestResult {
    private boolean success;
    private int statusCode;
    private long responseTime;
    private String responseBody;
    private String error;
    private String testName;
    private Map<String, String> headers;
    private long receivedBytes;
    private long timestamp;  // Timestamp when the test was executed
    private long startTime;   // Start time of the test execution
    private long endTime;     // End time of the test execution
    
    // Additional fields to track request processing
    private String processedBody;       // Body with resolved variables
    private String processedEndpoint;   // Endpoint with resolved variables
    private Map<String, String> processedHeaders; // Headers with resolved variables
    
    // Fields for ECS pattern implementation
    private String scenarioId;          // ID of the scenario this result belongs to
    private String scenarioName;        // Name of the scenario this result belongs to
    
    public TestResult() {
        this.headers = new HashMap<>();
        this.processedHeaders = new HashMap<>();
        this.receivedBytes = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getTestName() {
        return testName;
    }
    
    public void setTestName(String testName) {
        this.testName = testName;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public long getReceivedBytes() {
        return receivedBytes;
    }
    
    public void setReceivedBytes(long receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getProcessedBody() {
        return processedBody;
    }

    public void setProcessedBody(String processedBody) {
        this.processedBody = processedBody;
    }

    public String getProcessedEndpoint() {
        return processedEndpoint;
    }

    public void setProcessedEndpoint(String processedEndpoint) {
        this.processedEndpoint = processedEndpoint;
    }

    public Map<String, String> getProcessedHeaders() {
        return processedHeaders;
    }

    public void setProcessedHeaders(Map<String, String> processedHeaders) {
        this.processedHeaders = processedHeaders;
    }
    
    public String getScenarioId() {
        return scenarioId;
    }
    
    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }
    
    public String getScenarioName() {
        return scenarioName;
    }
    
    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }
    
    /**
     * Add a request result to this test result
     * 
     * @param requestName Name of the request
     * @param success Whether the request was successful
     * @param statusCode HTTP status code
     * @param responseTime Response time in milliseconds
     */
    public void addRequestResult(String requestName, boolean success, int statusCode, long responseTime) {
        this.testName = requestName;
        this.success = success;
        this.statusCode = statusCode;
        this.responseTime = responseTime;
    }
    
    @Override
    public String toString() {
        return "TestResult{" +
                "success=" + success +
                ", statusCode=" + statusCode +
                ", responseTime=" + responseTime +
                ", testName='" + testName + '\'' +
                ", body='" + (responseBody != null ? (responseBody.length() > 50 ? 
                              responseBody.substring(0, 47) + "..." : responseBody) : "null") + '\'' +
                (error != null ? ", error='" + error + '\'' : "") +
                ", processedEndpoint='" + processedEndpoint + '\'' +
                ", receivedBytes=" + receivedBytes +
                ", timestamp=" + timestamp +
                '}';
    }
}