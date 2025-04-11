package io.ecs.protocols;

import io.ecs.engine.Protocol;
import io.ecs.model.Response;
import io.ecs.util.TemplateProcessor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified HTTP/HTTPS protocol implementation with variable support
 */
public class HttpProtocol implements Protocol {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpProtocol.class);
    private Map<String, String> globalVariables = new HashMap<>();
    private final TemplateProcessor templateProcessor;
    
    public HttpProtocol() {
        templateProcessor = new TemplateProcessor();
    }
    
    @Override
    public String getName() {
        return "http";
    }
    
    @Override
    public Map<String, String> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }
    
    @Override
    public void setGlobalVariables(Map<String, String> variables) {
        this.globalVariables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }
    
    @Override
    public Response execute(String endpoint, String method, String body,
                         Map<String, String> headers, Map<String, String> params,
                         Map<String, String> requestVariables) throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Merge global and request variables
            Map<String, String> mergedVariables = new HashMap<>(globalVariables);
            if (requestVariables != null) {
                mergedVariables.putAll(requestVariables);
            }
            
            // Process variables in endpoint, body, headers, and params
            String processedEndpoint = processVariables(endpoint, mergedVariables);
            String processedBody = processVariables(body, mergedVariables);
            Map<String, String> processedHeaders = processMapValues(headers, mergedVariables);
            Map<String, String> processedParams = processMapValues(params, mergedVariables);
            
            // Add base URL if not an absolute URL
            if (!processedEndpoint.toLowerCase().startsWith("http")) {
                String baseUrl = mergedVariables.getOrDefault("baseUrl", "https://jsonplaceholder.typicode.com");
                if (!processedEndpoint.startsWith("/")) {
                    processedEndpoint = baseUrl + "/" + processedEndpoint;
                } else {
                    processedEndpoint = baseUrl + processedEndpoint;
                }
            }
            
            // Create and execute the request
            HttpClient httpClient = HttpClients.createDefault();
            HttpUriRequest request = createRequest(processedEndpoint, method, processedBody, processedParams);
            
            // Add headers
            if (processedHeaders != null) {
                for (Map.Entry<String, String> entry : processedHeaders.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }
            
            // Set content type from variables if present and not already set
            if (processedBody != null && !request.containsHeader("Content-Type")) {
                String contentType = mergedVariables.getOrDefault("contentType", "application/json");
                request.addHeader("Content-Type", contentType);
            }
            
            // Configure timeouts if specified in variables
            // Note: This would require custom HttpClient configuration
            
            // Execute request
            HttpResponse httpResponse = httpClient.execute(request);
            HttpEntity entity = httpResponse.getEntity();
            
            // Create response
            Response response = new Response();
            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            
            // Get headers
            for (org.apache.http.Header header : httpResponse.getAllHeaders()) {
                response.addHeader(header.getName(), header.getValue());
            }
            
            // Get body
            if (entity != null) {
                String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                response.setBody(responseBody);
                
                // Set received bytes
                if (responseBody != null) {
                    response.setReceivedBytes(responseBody.getBytes(StandardCharsets.UTF_8).length);
                }
                
                // Ensure the entity content is fully consumed
                EntityUtils.consume(entity);
            }
            
            // Set response time
            long endTime = System.currentTimeMillis();
            response.setResponseTime(endTime - startTime);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error executing HTTP request: {}", e.getMessage());
            
            // Create error response
            Response errorResponse = new Response();
            errorResponse.setStatusCode(500);
            errorResponse.setBody("Error: " + e.getMessage());
            
            long endTime = System.currentTimeMillis();
            errorResponse.setResponseTime(endTime - startTime);
            
            return errorResponse;
        }
    }
    
    /**
     * Process a string with template variables
     */
    private String processVariables(String input, Map<String, String> variables) {
        if (input == null || input.isEmpty() || variables == null || variables.isEmpty()) {
            return input;
        }
        return templateProcessor.processTemplate(input, variables);
    }
    
    /**
     * Process all values in a map with template variables
     */
    private Map<String, String> processMapValues(Map<String, String> map, Map<String, String> variables) {
        if (map == null || map.isEmpty() || variables == null || variables.isEmpty()) {
            return map;
        }
        
        Map<String, String> processedMap = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            processedMap.put(entry.getKey(), processVariables(entry.getValue(), variables));
        }
        return processedMap;
    }
    
    /**
     * Create an HTTP request based on method and parameters
     */
    private HttpUriRequest createRequest(String endpoint, String method, String body, 
                                      Map<String, String> params) throws URISyntaxException, IOException {
        // Build URI with query parameters
        URIBuilder builder = new URIBuilder(endpoint);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        URI uri = builder.build();
        
        // Create request based on method
        HttpUriRequest request;
        
        switch (method.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uri);
                if (body != null && !body.isEmpty()) {
                    httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                }
                request = httpPost;
                break;
                
            case "PUT":
                HttpPut httpPut = new HttpPut(uri);
                if (body != null && !body.isEmpty()) {
                    httpPut.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                }
                request = httpPut;
                break;
                
            case "DELETE":
                request = new HttpDelete(uri);
                break;
                
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uri);
                if (body != null && !body.isEmpty()) {
                    httpPatch.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
                }
                request = httpPatch;
                break;
                
            case "HEAD":
                request = new HttpHead(uri);
                break;
                
            case "OPTIONS":
                request = new HttpOptions(uri);
                break;
                
            default:
                // Default to GET
                request = new HttpGet(uri);
                break;
        }
        
        // Add default headers
        request.addHeader("User-Agent", "PerfTest-Framework/1.0");
        request.addHeader("Accept", "*/*");
        
        if (body != null && !body.isEmpty() && 
                !request.containsHeader("Content-Type")) {
            request.addHeader("Content-Type", "application/json");
        }
        
        return request;
    }
}