package io.perftest.engine;

import io.perftest.model.Response;

import java.util.Map;

/**
 * Interface for all protocol implementations
 */
public interface Protocol {
    
    /**
     * Set global variables for this protocol
     */
    void setGlobalVariables(Map<String, String> variables);
    
    /**
     * Execute a request with this protocol
     * 
     * @param endpoint Endpoint URL or address
     * @param method HTTP method or equivalent
     * @param body Request body (optional)
     * @param headers Request headers (optional)
     * @param params Query parameters or equivalent (optional)
     * @param variables Variables for this request (optional)
     * @return Response from the execution
     */
    Response execute(String endpoint, String method, String body,
                  Map<String, String> headers, Map<String, String> params,
                  Map<String, String> variables) throws Exception;
}