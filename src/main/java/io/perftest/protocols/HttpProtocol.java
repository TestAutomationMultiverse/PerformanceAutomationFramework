package io.perftest.protocols;

import io.perftest.engine.Protocol;
import io.perftest.model.Response;
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
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP/HTTPS protocol implementation
 */
public class HttpProtocol implements Protocol {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpProtocol.class);
    private Map<String, String> globalVariables = new HashMap<>();
    
    @Override
    public void setGlobalVariables(Map<String, String> variables) {
        this.globalVariables = variables != null ? variables : new HashMap<>();
    }
    
    @Override
    public Response execute(String endpoint, String method, String body,
                         Map<String, String> headers, Map<String, String> params,
                         Map<String, String> variables) throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpUriRequest request = createRequest(endpoint, method, body, params);
            
            // Add headers
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }
            
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
                response.setBody(EntityUtils.toString(entity));
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
                    httpPost.setEntity(new StringEntity(body));
                }
                request = httpPost;
                break;
                
            case "PUT":
                HttpPut httpPut = new HttpPut(uri);
                if (body != null && !body.isEmpty()) {
                    httpPut.setEntity(new StringEntity(body));
                }
                request = httpPut;
                break;
                
            case "DELETE":
                request = new HttpDelete(uri);
                break;
                
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uri);
                if (body != null && !body.isEmpty()) {
                    httpPatch.setEntity(new StringEntity(body));
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