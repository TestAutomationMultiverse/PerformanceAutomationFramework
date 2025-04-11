package io.ecs.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper component for building requests in the ECS framework
 * Provides a fluent interface for creating complex requests
 */
public class RequestBuilder {
    
    private final Request request;
    
    /**
     * Create a new request builder
     * 
     * @param name Name of the request
     * @param protocol Protocol to use (HTTP, HTTPS, etc.)
     */
    public RequestBuilder(String name, String protocol) {
        request = new Request();
        request.setName(name);
        request.setProtocol(protocol);
        
        // Initialize collections
        request.setHeaders(new HashMap<>());
        request.setParams(new HashMap<>());
    }
    
    /**
     * Static factory method to create a new request builder
     * 
     * @param name Name of the request
     * @param protocol Protocol to use
     * @return A new request builder
     */
    public static RequestBuilder create(String name, String protocol) {
        return new RequestBuilder(name, protocol);
    }
    
    /**
     * Set the HTTP method
     * 
     * @param method HTTP method (GET, POST, PUT, etc.)
     * @return This builder instance
     */
    public RequestBuilder method(String method) {
        request.setMethod(method);
        return this;
    }
    
    /**
     * Set the endpoint URL
     * 
     * @param endpoint Endpoint URL, can include variables
     * @return This builder instance
     */
    public RequestBuilder endpoint(String endpoint) {
        request.setEndpoint(endpoint);
        return this;
    }
    
    /**
     * Set the request body
     * 
     * @param body Request body, can include variables
     * @return This builder instance
     */
    public RequestBuilder body(String body) {
        request.setBody(body);
        return this;
    }
    
    /**
     * Add a header to the request
     * 
     * @param name Header name
     * @param value Header value, can include variables
     * @return This builder instance
     */
    public RequestBuilder header(String name, String value) {
        request.getHeaders().put(name, value);
        return this;
    }
    
    /**
     * Add multiple headers at once
     * 
     * @param headers Map of header name to value
     * @return This builder instance
     */
    public RequestBuilder headers(Map<String, String> headers) {
        if (headers != null) {
            request.getHeaders().putAll(headers);
        }
        return this;
    }
    
    /**
     * Add a query parameter
     * 
     * @param name Parameter name
     * @param value Parameter value, can include variables
     * @return This builder instance
     */
    public RequestBuilder param(String name, String value) {
        request.getParams().put(name, value);
        return this;
    }
    
    /**
     * Add multiple query parameters at once
     * 
     * @param params Map of parameter name to value
     * @return This builder instance
     */
    public RequestBuilder params(Map<String, String> params) {
        if (params != null) {
            request.getParams().putAll(params);
        }
        return this;
    }
    
    /**
     * Add standard JSON content type header
     * 
     * @return This builder instance
     */
    public RequestBuilder jsonContent() {
        request.getHeaders().put("Content-Type", "application/json");
        return this;
    }
    
    /**
     * Add a dynamic request ID header
     * 
     * @return This builder instance
     */
    public RequestBuilder withRequestId() {
        request.getHeaders().put("X-Request-ID", "${uuid}");
        return this;
    }
    
    /**
     * Add a timestamp header
     * 
     * @return This builder instance
     */
    public RequestBuilder withTimestamp() {
        request.getHeaders().put("X-Timestamp", "${timestamp}");
        return this;
    }
    
    /**
     * Set the data source for this request
     * 
     * @param dataSource Name of the data source
     * @return This builder instance
     */
    public RequestBuilder dataSource(String dataSource) {
        request.setDataSource(dataSource);
        return this;
    }
    
    /**
     * Build the final request
     * 
     * @return The configured request
     */
    public Request build() {
        return request;
    }
}