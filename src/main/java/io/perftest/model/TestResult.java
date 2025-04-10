package io.perftest.model;

import java.util.HashMap;
import java.util.Map;

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
    
    // Additional fields to track request processing
    private String processedBody;       // Body with resolved variables
    private String processedEndpoint;   // Endpoint with resolved variables
    private Map<String, String> processedHeaders; // Headers with resolved variables
    
    public TestResult() {
        this.headers = new HashMap<>();
        this.processedHeaders = new HashMap<>();
        this.receivedBytes = 0;
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
                '}';
    }
}