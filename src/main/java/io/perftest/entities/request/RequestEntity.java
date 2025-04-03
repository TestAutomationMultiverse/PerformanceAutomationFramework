package io.perftest.entities.request;

import java.util.HashMap;
import java.util.Map;

/**
 * Base entity class for HTTP requests
 */
public class RequestEntity {
    private String url;
    private String method;
    private String body;
    private Map<String, String> headers;
    private int expectedStatus;
    private Map<String, String> assertions;
    private Integer connectTimeout;
    private Integer responseTimeout;
    private String name;
    
    /**
     * Creates a new request entity with the specified URL
     * @param url URL for the request
     */
    public RequestEntity(String url) {
        this.url = url;
        this.method = "GET";  // Default method
        this.headers = new HashMap<>();
        this.expectedStatus = 200;  // Default expected status
        this.assertions = new HashMap<>();
        this.name = "Unnamed Request";  // Default name
    }
    
    /**
     * Get the request URL
     * @return URL
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Set the request URL
     * @param url URL
     * @return this instance for chaining
     */
    public RequestEntity setUrl(String url) {
        this.url = url;
        return this;
    }
    
    /**
     * Get the HTTP method
     * @return HTTP method (GET, POST, etc.)
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * Set the HTTP method
     * @param method HTTP method (GET, POST, etc.)
     * @return this instance for chaining
     */
    public RequestEntity setMethod(String method) {
        this.method = method;
        return this;
    }
    
    /**
     * Get the request body
     * @return Request body
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Set the request body
     * @param body Request body
     * @return this instance for chaining
     */
    public RequestEntity setBody(String body) {
        this.body = body;
        return this;
    }
    
    /**
     * Get the request headers
     * @return Map of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    /**
     * Set all headers (will override existing headers)
     * @param headers Map of headers
     * @return this instance for chaining
     */
    public RequestEntity setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }
    
    /**
     * Add a header to the request
     * @param name Header name
     * @param value Header value
     * @return this instance for chaining
     */
    public RequestEntity addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    /**
     * Get the expected HTTP status code
     * @return Expected status code
     */
    public int getExpectedStatus() {
        return expectedStatus;
    }
    
    /**
     * Set the expected HTTP status code
     * @param expectedStatus Expected status code
     * @return this instance for chaining
     */
    public RequestEntity setExpectedStatus(int expectedStatus) {
        this.expectedStatus = expectedStatus;
        return this;
    }
    
    /**
     * Get the assertions for this request
     * @return Map of JSONPath to expected values
     */
    public Map<String, String> getAssertions() {
        return assertions;
    }
    
    /**
     * Set all assertions (will override existing assertions)
     * @param assertions Map of JSONPath to expected values
     * @return this instance for chaining
     */
    public RequestEntity setAssertions(Map<String, String> assertions) {
        this.assertions = assertions;
        return this;
    }
    
    /**
     * Add a JSONPath assertion
     * @param jsonPath JSONPath expression
     * @param expectedValue Expected value (use * for any value)
     * @return this instance for chaining
     */
    public RequestEntity addAssertion(String jsonPath, String expectedValue) {
        this.assertions.put(jsonPath, expectedValue);
        return this;
    }
    
    /**
     * Get the connection timeout in milliseconds
     * @return Connection timeout
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }
    
    /**
     * Set the connection timeout in milliseconds
     * @param connectTimeout Connection timeout
     * @return this instance for chaining
     */
    public RequestEntity setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
    
    /**
     * Get the response timeout in milliseconds
     * @return Response timeout
     */
    public Integer getResponseTimeout() {
        return responseTimeout;
    }
    
    /**
     * Set the response timeout in milliseconds
     * @param responseTimeout Response timeout
     * @return this instance for chaining
     */
    public RequestEntity setResponseTimeout(Integer responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }
    
    /**
     * Get the request name
     * @return Request name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the request name
     * @param name Request name
     * @return this instance for chaining
     */
    public RequestEntity setName(String name) {
        this.name = name;
        return this;
    }
}