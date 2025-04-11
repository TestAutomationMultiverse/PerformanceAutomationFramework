package io.ecs.component;

import io.ecs.model.Request;
import io.ecs.model.Response;
import io.ecs.model.TestResult;
import io.ecs.engine.Protocol;
import io.ecs.engine.ProtocolFactory;
import io.ecs.protocols.HttpProtocol;
import io.ecs.util.DynamicVariableResolver;
import io.ecs.util.EcsLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * ECS Component for handling protocol operations
 * 
 * This component follows the Entity-Component-System pattern by providing
 * protocol capabilities that can be applied to request entities.
 */
public class ProtocolComponent {
    private static final EcsLogger logger = EcsLogger.getLogger(ProtocolComponent.class);
    
    private final Map<String, String> variables;
    private Protocol currentProtocol;
    
    /**
     * Create a new ProtocolComponent with default HTTP protocol
     */
    public ProtocolComponent() {
        this.variables = new HashMap<>();
        this.currentProtocol = new HttpProtocol();
    }
    
    /**
     * Create a new ProtocolComponent with a specific protocol
     * 
     * @param protocol The protocol implementation
     */
    public ProtocolComponent(Protocol protocol) {
        this.variables = new HashMap<>();
        this.currentProtocol = protocol;
    }
    
    /**
     * Create a new ProtocolComponent with a specific protocol type
     * 
     * @param protocolType Protocol type identifier
     */
    public ProtocolComponent(String protocolType) {
        this.variables = new HashMap<>();
        this.currentProtocol = ProtocolFactory.createProtocol(protocolType);
    }
    
    /**
     * Create a new ProtocolComponent with the given variables
     * 
     * @param variables The variables to use in this component
     */
    public ProtocolComponent(Map<String, String> variables) {
        this.variables = new HashMap<>();
        if (variables != null) {
            this.variables.putAll(variables);
        }
        this.currentProtocol = new HttpProtocol();
    }
    
    /**
     * Set the current protocol
     * 
     * @param protocol The protocol implementation
     * @return This component for chaining
     */
    public ProtocolComponent setProtocol(Protocol protocol) {
        this.currentProtocol = protocol;
        return this;
    }
    
    /**
     * Set the current protocol type
     * 
     * @param protocolType Protocol type identifier
     * @return This component for chaining
     */
    public ProtocolComponent setProtocolType(String protocolType) {
        this.currentProtocol = ProtocolFactory.createProtocol(protocolType);
        return this;
    }
    
    /**
     * Get the current protocol
     * 
     * @return Current protocol implementation
     */
    public Protocol getProtocol() {
        return currentProtocol;
    }
    
    /**
     * Execute a request using the current protocol
     * 
     * @param request Request to execute
     * @return Response from the protocol
     */
    public Response executeRequest(Request request) {
        try {
            // Resolve any variables in the request
            Request resolvedRequest = resolveVariables(request);
            
            // Execute the request with the current protocol
            TestResult testResult = currentProtocol.execute(resolvedRequest);
            
            // Convert TestResult to Response
            Response response = new Response();
            response.setStatusCode(testResult.getStatusCode());
            response.setSuccess(testResult.isSuccess());
            response.setResponseTime(testResult.getResponseTime());
            response.setBody(testResult.getResponseBody());
            response.setError(testResult.getError());
            
            // Process any variables from the response
            processResponseVariables(response);
            
            return response;
        } catch (Exception e) {
            logger.error("Error executing request: {}", request.getName(), e);
            Response errorResponse = new Response();
            errorResponse.setSuccess(false);
            errorResponse.setStatusCode(0);
            errorResponse.setResponseTime(0);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Get a variable from the component
     * 
     * @param name Variable name
     * @return Variable value or null if not found
     */
    public String getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Set a variable in the component
     * 
     * @param name Variable name
     * @param value Variable value
     * @return This component for chaining
     */
    public ProtocolComponent setVariable(String name, String value) {
        variables.put(name, value);
        return this;
    }
    
    /**
     * Get all variables
     * 
     * @return Map of all variables
     */
    public Map<String, String> getVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * Clear all variables
     * 
     * @return This component for chaining
     */
    public ProtocolComponent clearVariables() {
        variables.clear();
        return this;
    }
    
    /**
     * Resolve variables in a request
     * 
     * @param request Request with variables
     * @return New request with resolved variables
     */
    private Request resolveVariables(Request request) {
        // Create a copy of the request
        Request resolvedRequest = new Request();
        
        // Copy properties from original request
        resolvedRequest.setName(request.getName());
        resolvedRequest.setProtocol(request.getProtocol());
        resolvedRequest.setEndpoint(request.getEndpoint());
        resolvedRequest.setMethod(request.getMethod());
        resolvedRequest.setBody(request.getBody());
        resolvedRequest.setBodyTemplate(request.getBodyTemplate());
        resolvedRequest.setHeadersTemplate(request.getHeadersTemplate());
        resolvedRequest.setParamsTemplate(request.getParamsTemplate());
        resolvedRequest.setDataSource(request.getDataSource());
        
        // Copy maps
        if (request.getHeaders() != null) {
            resolvedRequest.setHeaders(new HashMap<>(request.getHeaders()));
        }
        if (request.getParams() != null) {
            resolvedRequest.setParams(new HashMap<>(request.getParams()));
        }
        if (request.getVariables() != null) {
            resolvedRequest.setVariables(new HashMap<>(request.getVariables()));
        }
        if (request.getAssertions() != null) {
            resolvedRequest.setAssertions(new HashMap<>(request.getAssertions()));
        }
        if (request.getResponseValidators() != null) {
            resolvedRequest.setResponseValidators(new HashMap<>(request.getResponseValidators()));
        }
        
        // Resolve URL variables
        String url = resolvedRequest.getUrl();
        if (url != null) {
            String resolvedUrl = DynamicVariableResolver.resolveVariables(url, variables);
            // We can't directly set URL as it's derived from endpoint, so we set the endpoint
            if (resolvedUrl != null && !resolvedUrl.equals(url)) {
                // Check if it has protocol prefix
                if (resolvedUrl.startsWith("http://") || resolvedUrl.startsWith("https://")) {
                    String protocol = resolvedUrl.startsWith("https://") ? "HTTPS" : "HTTP";
                    resolvedRequest.setProtocol(protocol);
                    // Remove protocol from URL to get endpoint
                    String endpoint = resolvedUrl.substring(resolvedUrl.indexOf("://") + 3);
                    resolvedRequest.setEndpoint(endpoint);
                } else {
                    resolvedRequest.setEndpoint(resolvedUrl);
                }
            }
        }
        
        // Resolve header variables
        Map<String, String> resolvedHeaders = new HashMap<>();
        for (Map.Entry<String, String> header : resolvedRequest.getHeaders().entrySet()) {
            String resolvedName = DynamicVariableResolver.resolveVariables(header.getKey(), variables);
            String resolvedValue = DynamicVariableResolver.resolveVariables(header.getValue(), variables);
            resolvedHeaders.put(resolvedName, resolvedValue);
        }
        resolvedRequest.setHeaders(resolvedHeaders);
        
        // Resolve body variables if it's a string body
        if (resolvedRequest.getBody() instanceof String) {
            String body = (String) resolvedRequest.getBody();
            String resolvedBody = DynamicVariableResolver.resolveVariables(body, variables);
            resolvedRequest.setBody(resolvedBody);
        }
        
        return resolvedRequest;
    }
    
    /**
     * Process variables from a response
     * 
     * @param response Response containing potential variables
     */
    private void processResponseVariables(Response response) {
        // Add custom variable processing from response data
        // This could extract values from JSON/XML responses, headers, etc.
    }
    
    /**
     * Add a test result to track protocol execution
     * 
     * @param testResult Test result to update
     * @param request Request that was executed
     * @param response Response that was received
     * @return Updated test result
     */
    public TestResult addToTestResult(TestResult testResult, Request request, Response response) {
        testResult.addRequestResult(request.getName(), 
                                   response.isSuccess(), 
                                   response.getStatusCode(), 
                                   response.getResponseTime());
        
        // Add response body sample if needed
        // testResult.addResponseSample(request.getName(), response.getBody().toString());
        
        return testResult;
    }
}