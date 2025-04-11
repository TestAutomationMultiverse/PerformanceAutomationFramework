package io.ecs.component;

import io.ecs.model.Request;
import io.ecs.model.Response;
import io.ecs.model.TestResult;
import io.ecs.engine.Protocol;
import io.ecs.engine.ProtocolFactory;
import io.ecs.protocols.HttpProtocol;
import io.ecs.util.DynamicVariableResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ECS Component for handling protocol operations
 * 
 * This component follows the Entity-Component-System pattern by providing
 * protocol capabilities that can be applied to request entities.
 */
public class ProtocolComponent {
    private static final Logger LOGGER = Logger.getLogger(ProtocolComponent.class.getName());
    
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
            Response response = currentProtocol.executeRequest(resolvedRequest);
            
            // Process any variables from the response
            processResponseVariables(response);
            
            return response;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing request: " + request.getName(), e);
            Response errorResponse = new Response();
            errorResponse.setSuccess(false);
            errorResponse.setStatusCode(0);
            errorResponse.setResponseTime(0);
            errorResponse.setErrorMessage(e.getMessage());
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
        Request resolvedRequest = new Request(request);
        
        // Resolve URL variables
        String resolvedUrl = DynamicVariableResolver.resolveVariables(resolvedRequest.getUrl(), variables);
        resolvedRequest.setUrl(resolvedUrl);
        
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