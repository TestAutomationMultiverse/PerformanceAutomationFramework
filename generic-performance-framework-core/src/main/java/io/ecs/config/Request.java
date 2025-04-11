package io.ecs.config;

import io.ecs.util.FileUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a request in the YAML configuration
 * 
 * This is the config version of Request, which is later converted to model.Request
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

    public void setBody(Object body) {
        if (body instanceof String) {
            this.body = (String) body;
        } else {
            this.body = String.valueOf(body);
        }
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
    
    /**
     * Alias for setParams for backward compatibility
     * 
     * @param parameters Map of parameters to set
     */
    public void setParameters(Map<String, String> parameters) {
        this.params = parameters;
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
        // If the path is just a filename or doesn't exist, resolve it to the appropriate path
        this.bodyTemplate = bodyTemplate != null ? FileUtils.resolveFilePath(bodyTemplate) : null;
    }

    public String getHeadersTemplate() {
        return headersTemplate;
    }

    public void setHeadersTemplate(String headersTemplate) {
        // If the path is just a filename or doesn't exist, resolve it to the appropriate path
        this.headersTemplate = headersTemplate != null ? FileUtils.resolveFilePath(headersTemplate) : null;
    }

    public String getParamsTemplate() {
        return paramsTemplate;
    }

    public void setParamsTemplate(String paramsTemplate) {
        // If the path is just a filename or doesn't exist, resolve it to the appropriate path
        this.paramsTemplate = paramsTemplate != null ? FileUtils.resolveFilePath(paramsTemplate) : null;
    }
    
    public Map<String, String> getResponseValidators() {
        return responseValidators;
    }

    public void setResponseValidators(Map<String, String> responseValidators) {
        // For each response validator, resolve the path if it's a file reference
        if (responseValidators != null) {
            Map<String, String> resolvedValidators = new HashMap<>();
            for (Map.Entry<String, String> entry : responseValidators.entrySet()) {
                String value = entry.getValue();
                if (value != null && (value.endsWith(".json") || value.endsWith(".schema.json"))) {
                    resolvedValidators.put(entry.getKey(), FileUtils.resolveFilePath(value));
                } else {
                    resolvedValidators.put(entry.getKey(), value);
                }
            }
            this.responseValidators = resolvedValidators;
        } else {
            this.responseValidators = responseValidators;
        }
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        // If the path is just a filename or doesn't exist, resolve it to the appropriate path
        this.dataSource = dataSource != null ? FileUtils.resolveFilePath(dataSource) : null;
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