package io.ecs;

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
        this.currentProtocol = ProtocolFactory.getProtocol("HTTP");
        if (this.currentProtocol != null) {
            this.currentProtocol.initialize(variables);
        }
    }
    
    /**
     * Create a new ProtocolComponent with specific variables
     * 
     * @param variables Variables to use for processing
     */
    public ProtocolComponent(Map<String, String> variables) {
        this.variables = new HashMap<>(variables);
        this.currentProtocol = ProtocolFactory.getProtocol("HTTP");
        if (this.currentProtocol != null) {
            this.currentProtocol.initialize(variables);
        }
    }
    
    /**
     * Create a new ProtocolComponent with specific variables and protocol
     * 
     * @param variables Variables to use for processing
     * @param protocolName Name of the protocol to use
     */
    public ProtocolComponent(Map<String, String> variables, String protocolName) {
        this.variables = new HashMap<>(variables);
        this.currentProtocol = ProtocolFactory.getProtocol(protocolName);
        if (this.currentProtocol != null) {
            this.currentProtocol.initialize(variables);
        } else {
            // Fall back to HTTP if the requested protocol is not available
            LOGGER.warning("Protocol not found: " + protocolName + ", falling back to HTTP");
            this.currentProtocol = ProtocolFactory.getProtocol("HTTP");
            if (this.currentProtocol != null) {
                this.currentProtocol.initialize(variables);
            }
        }
    }
    
    /**
     * Set the current protocol
     * 
     * @param protocolName Protocol name (HTTP, HTTPS, etc.)
     * @return This component for chaining
     */
    public ProtocolComponent setProtocol(String protocolName) {
        this.currentProtocol = ProtocolFactory.getProtocol(protocolName);
        if (this.currentProtocol != null) {
            this.currentProtocol.initialize(variables);
        }
        return this;
    }
    
    /**
     * Execute a request using the current protocol
     * 
     * @param request Request to execute
     * @return Test result from the execution
     */
    public TestResult executeRequest(Request request) {
        if (currentProtocol == null) {
            // Default to HTTP if not specified
            setProtocol("HTTP");
        }
        
        // Auto-select protocol based on URL if needed
        if (request.getUrl() != null && request.getUrl().startsWith("https://") && !(currentProtocol instanceof HttpProtocol)) {
            setProtocol("HTTPS");
        }
        
        try {
            // Combine global variables with request-specific variables
            Map<String, String> combinedVars = new HashMap<>(variables);
            if (request.getVariables() != null && !request.getVariables().isEmpty()) {
                combinedVars.putAll(request.getVariables());
            }
            
            // Execute the request with the combined variables
            return currentProtocol.execute(request, combinedVars);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing request: " + e.getMessage(), e);
            
            // Create an error result
            TestResult errorResult = new TestResult();
            errorResult.setSuccess(false);
            errorResult.setStatusCode(500);
            errorResult.setTestName(request.getName());
            errorResult.setError("Protocol error: " + e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * Process variables in a template string
     * 
     * @param template Template string with ${var} placeholders
     * @return Processed string with variables substituted
     */
    public String processVariables(String template) {
        if (template == null) {
            return null;
        }
        
        if (currentProtocol == null) {
            // Default to HTTP if not specified
            setProtocol("HTTP");
        }
        
        try {
            return currentProtocol.processVariables(template);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing variables in template: " + e.getMessage(), e);
            
            // Fall back to basic variable resolution if protocol-based one fails
            return DynamicVariableResolver.processTemplate(template, variables, createDynamicContext());
        }
    }
    
    /**
     * Create a dynamic context with runtime variables
     * 
     * @return Map of dynamic context variables
     */
    private Map<String, Object> createDynamicContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("timestamp", System.currentTimeMillis());
        context.put("threadNum", Thread.currentThread().getId());
        context.put("now", System.currentTimeMillis());
        return context;
    }
    
    /**
     * Get the current protocol
     * 
     * @return Current protocol implementation
     */
    public Protocol getCurrentProtocol() {
        return currentProtocol;
    }
    
    /**
     * Set variables for the protocol
     * 
     * @param newVariables Variables to set
     */
    public void setVariables(Map<String, String> newVariables) {
        if (newVariables != null) {
            variables.putAll(newVariables);
            
            // Update protocol with new variables
            if (currentProtocol != null) {
                currentProtocol.initialize(variables);
            }
        }
    }
    
    /**
     * Add a single variable
     * 
     * @param name Variable name
     * @param value Variable value
     */
    public void addVariable(String name, String value) {
        variables.put(name, value);
        
        // Update protocol with new variable
        if (currentProtocol != null) {
            currentProtocol.initialize(variables);
        }
    }
}