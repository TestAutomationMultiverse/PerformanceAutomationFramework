package io.perftest.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a request in the YAML configuration
 */
public class Request {
    private String name;
    private String protocol;
    private String endpoint;
    private String method;
    private String body;
    private String bodyTemplate;
    private Map<String, String> headers = new HashMap<>();
    private String headersTemplate;
    private Map<String, String> params = new HashMap<>();
    private String paramsTemplate;
    private Map<String, String> variables = new HashMap<>();
    private Map<String, Object> assertions = new HashMap<>();
    private Map<String, String> responseValidators = new HashMap<>();
    private String dataSource;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getAssertions() {
        return assertions;
    }

    public void setAssertions(Map<String, Object> assertions) {
        this.assertions = assertions;
    }
    
    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public String getHeadersTemplate() {
        return headersTemplate;
    }

    public void setHeadersTemplate(String headersTemplate) {
        this.headersTemplate = headersTemplate;
    }

    public String getParamsTemplate() {
        return paramsTemplate;
    }

    public void setParamsTemplate(String paramsTemplate) {
        this.paramsTemplate = paramsTemplate;
    }
    
    public Map<String, String> getResponseValidators() {
        return responseValidators;
    }

    public void setResponseValidators(Map<String, String> responseValidators) {
        this.responseValidators = responseValidators;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String toString() {
        return "Request{" +
                "name='" + name + '\'' +
                ", protocol='" + protocol + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}