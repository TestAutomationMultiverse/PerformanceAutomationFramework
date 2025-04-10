package io.perftest.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a response from a protocol
 */
public class Response {
    private int statusCode;
    private String body;
    private Map<String, String> headers = new HashMap<>();
    private long responseTime;
    private boolean success;
    private String error;
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.success = statusCode >= 200 && statusCode < 400;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public void addHeader(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }
    
    public long getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
        if (error != null && !error.isEmpty()) {
            this.success = false;
        }
    }
    
    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", body='" + (body != null ? (body.length() > 50 ? body.substring(0, 47) + "..." : body) : "null") + '\'' +
                ", responseTime=" + responseTime +
                ", success=" + success +
                '}';
    }
}