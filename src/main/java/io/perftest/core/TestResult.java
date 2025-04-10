package io.perftest.core;

import io.perftest.model.Response;

/**
 * Stores the result of a single test execution
 */
public class TestResult {
    private String requestName;
    private boolean success;
    private String status;
    private long startTime;
    private long endTime;
    private long responseTime;
    private Response response;
    private String errorMessage;
    
    public String getRequestName() {
        return requestName;
    }
    
    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
    
    public Response getResponse() {
        return response;
    }
    
    public void setResponse(Response response) {
        this.response = response;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "TestResult{" +
                "requestName='" + requestName + '\'' +
                ", success=" + success +
                ", status='" + status + '\'' +
                ", responseTime=" + responseTime + "ms" +
                ", statusCode=" + (response != null ? response.getStatusCode() : "N/A") +
                (errorMessage != null ? ", error='" + errorMessage + '\'' : "") +
                '}';
    }
}
