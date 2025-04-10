package io.perftest.protocol;

import io.perftest.model.Request;
import io.perftest.model.Response;
import io.perftest.model.TestResult;

import java.util.Map;

/**
 * Interface for protocol implementations with variable support
 */
public interface Protocol {
    
    /**
     * Get the protocol name
     * 
     * @return protocol name
     */
    String getName();
    
    /**
     * Set global variables for this protocol
     * 
     * @param variables the global variables
     */
    default void setGlobalVariables(Map<String, String> variables) {
        // Default implementation does nothing
    }
    
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
    Response execute(String endpoint, String method, String body, 
                  Map<String, String> headers, Map<String, String> params) throws Exception;
    
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
    default Response execute(String endpoint, String method, String body, 
                         Map<String, String> headers, Map<String, String> params, 
                         Map<String, String> requestVariables) throws Exception {
        // Default implementation ignores request variables
        return execute(endpoint, method, body, headers, params);
    }
    
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
            
            // Copy individual headers instead of using setHeaders
            if (response.getHeaders() != null) {
                for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                    result.getHeaders().put(header.getKey(), header.getValue());
                }
            }
        } else {
            result.setSuccess(false);
            result.setStatusCode(500);
            result.setError("Null response returned");
        }
        
        return result;
    }
}
