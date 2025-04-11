package io.ecs.engine;

import io.ecs.model.Request;
import io.ecs.model.Response;
import io.ecs.model.TestResult;

import java.util.Map;
import java.util.HashMap;

/**
 * Universal interface for protocol implementations
 */
public interface Protocol {
    
    /**
     * Get the protocol name
     * 
     * @return protocol name
     */
    default String getName() {
        return "http";  // Default to HTTP protocol
    }
    
    /**
     * Set global variables for this protocol
     * 
     * @param variables the global variables
     */
    void setGlobalVariables(Map<String, String> variables);
    
    /**
     * Get global variables for this protocol
     * 
     * @return the global variables
     */
    default Map<String, String> getGlobalVariables() {
        return null;
    }
    
    /**
     * Execute a request using this protocol
     * 
     * @param endpoint the endpoint or URL
     * @param method the method (e.g., GET, POST)
     * @param body the request body
     * @param headers the request headers
     * @param params the request parameters
     * @return the response
     * @throws Exception if execution fails
     */
    default Response execute(String endpoint, String method, String body, 
                  Map<String, String> headers, Map<String, String> params) throws Exception {
        return execute(endpoint, method, body, headers, params, null);
    }
    
    /**
     * Execute a request using this protocol with request-specific variables
     * 
     * @param endpoint the endpoint or URL
     * @param method the method (e.g., GET, POST)
     * @param body the request body
     * @param headers the request headers
     * @param params the request parameters
     * @param requestVariables request-specific variables that override global variables
     * @return the response
     * @throws Exception if execution fails
     */
    Response execute(String endpoint, String method, String body, 
                  Map<String, String> headers, Map<String, String> params, 
                  Map<String, String> requestVariables) throws Exception;
    
    /**
     * Execute a request using a Request object
     * 
     * @param request the request object containing all details
     * @param variables the variables to use for this request
     * @return the test result
     * @throws Exception if execution fails
     */
    default TestResult execute(Request request, Map<String, String> variables) throws Exception {
        // Default implementation converts the Request to parameters and calls the basic execute method
        Response response = execute(
            request.getEndpoint(),
            request.getMethod(),
            request.getBody(),
            request.getHeaders(),
            request.getParams(),
            variables
        );
        
        // Convert Response to TestResult
        TestResult result = new TestResult();
        if (response != null) {
            result.setSuccess(response.isSuccess());
            result.setStatusCode(response.getStatusCode());
            result.setResponseBody(response.getBody());
            result.setResponseTime(response.getResponseTime());
            result.setTestName(request.getName());
            
            // Copy headers if available
            if (response.getHeaders() != null) {
                for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                    result.getHeaders().put(header.getKey(), header.getValue());
                }
            }
        } else {
            result.setSuccess(false);
            result.setStatusCode(500);
            result.setError("Null response returned");
            result.setTestName(request.getName());
        }
        
        return result;
    }
    
    /**
     * Process variables in a template string
     * 
     * @param template Template string with ${var} placeholders
     * @return Processed string with variables substituted
     */
    default String processVariables(String template) {
        if (template == null) {
            return null;
        }
        
        Map<String, String> variables = getGlobalVariables();
        if (variables == null) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String varPattern = "\\$\\{" + entry.getKey() + "\\}";
            result = result.replaceAll(varPattern, entry.getValue() != null ? entry.getValue() : "");
        }
        
        return result;
    }
    
    /**
     * Initialize the protocol with variables
     * 
     * @param variables variables to use for initialization
     */
    default void initialize(Map<String, String> variables) {
        setGlobalVariables(variables);
    }
    
    /**
     * Execute a request directly
     * 
     * @param request The request to execute
     * @return Test result
     */
    default TestResult execute(Request request) {
        try {
            return execute(request, request.getVariables());
        } catch (Exception e) {
            TestResult result = new TestResult();
            result.setSuccess(false);
            result.setStatusCode(500);
            result.setError(e.getMessage());
            result.setTestName(request.getName());
            return result;
        }
    }
}