package io.perftest.entities.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import io.perftest.entities.base.Entity;

/**
 * Base class for all request entities
 */
public class RequestEntity extends Entity {
    private String url;
    private String method = "GET";
    private Map<String, String> headers = new HashMap<>();
    private int connectTimeout = 30000;
    private int responseTimeout = 60000;
    private Map<String, Object> assertions = new HashMap<>();

    /**
     * Default constructor
     */
    public RequestEntity() {
        super();
    }

    /**
     * Constructor with URL
     * 
     * @param url The URL for the request
     */
    public RequestEntity(String url) {
        super();
        this.url = url;
    }

    /**
     * @return URL for the request
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url URL for the request
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return HTTP headers as a map
     */
    public Map<String, String> getHeaders() {
        if (headers == null)
            return null;
        return Collections.unmodifiableMap(headers);
    }

    /**
     * @param headers HTTP headers as a map
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Add a header to the request
     * 
     * @param name Header name
     * @param value Header value
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * @return Connection timeout in milliseconds
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * @param connectTimeout Connection timeout in milliseconds
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * @return Response timeout in milliseconds
     */
    public int getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * @param responseTimeout Response timeout in milliseconds
     */
    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    /**
     * @return Assertions as a map
     */
    public Map<String, Object> getAssertions() {
        return assertions;
    }

    /**
     * @param assertions Assertions as a map
     */
    public void setAssertions(Map<String, Object> assertions) {
        this.assertions = assertions;
    }

    /**
     * Add an assertion for the response
     * 
     * @param path JsonPath or other path expression
     * @param expectedValue Expected value or "*" for existence check
     */
    public void addAssertion(String path, String expectedValue) {
        assertions.put(path, expectedValue);
    }

    /**
     * Set the name of the request
     * 
     * @param name Request name
     */
    public void setName(String name) {
        setProperty("name", name);
    }
}
