package io.perftest.protocol;

import io.perftest.model.Request;
import io.perftest.model.Response;
import io.perftest.model.TestResult;
import io.perftest.util.TemplateProcessor;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Protocol implementation with variable support
 */
public class HttpProtocol implements Protocol {
    
    private final TemplateProcessor templateProcessor;
    private Map<String, String> globalVariables;
    
    public HttpProtocol() {
        templateProcessor = new TemplateProcessor();
        globalVariables = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "http";
    }
    
    public void setGlobalVariables(Map<String, String> variables) {
        if (variables != null) {
            this.globalVariables = new HashMap<>(variables);
        }
    }
    
    public Map<String, String> getGlobalVariables() {
        return globalVariables;
    }
    
    @Override
    public Response execute(String endpoint, String method, String body, 
                         Map<String, String> headers, Map<String, String> params) throws Exception {
        
        return execute(endpoint, method, body, headers, params, null);
    }
    
    public Response execute(String endpoint, String method, String body, 
                         Map<String, String> headers, Map<String, String> params, 
                         Map<String, String> requestVariables) throws Exception {
        
        Response response = new Response();
        
        try {
            // Merge global and request variables
            Map<String, String> mergedVariables = new HashMap<>(globalVariables);
            if (requestVariables != null) {
                mergedVariables.putAll(requestVariables);
            }
            
            // Process endpoint with variables
            if (mergedVariables != null && !mergedVariables.isEmpty()) {
                endpoint = templateProcessor.processTemplate(endpoint, mergedVariables);
                if (body != null) {
                    body = templateProcessor.processTemplate(body, mergedVariables);
                }
            }
            
            // Build URL with parameters
            StringBuilder urlBuilder = new StringBuilder();
            if (!endpoint.toLowerCase().startsWith("http")) {
                // Use baseUrl from variables if available, otherwise use JSONPlaceholder
                String baseUrl = mergedVariables.getOrDefault("baseUrl", "https://jsonplaceholder.typicode.com");
                urlBuilder.append(baseUrl);
                if (!endpoint.startsWith("/")) {
                    urlBuilder.append("/");
                }
            }
            urlBuilder.append(endpoint);
            
            if (params != null && !params.isEmpty()) {
                urlBuilder.append(endpoint.contains("?") ? "&" : "?");
                boolean first = true;
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (!first) {
                        urlBuilder.append("&");
                    }
                    
                    // Process param values with variables
                    String paramValue = param.getValue();
                    if (mergedVariables != null && !mergedVariables.isEmpty()) {
                        paramValue = templateProcessor.processTemplate(paramValue, mergedVariables);
                    }
                    
                    urlBuilder.append(param.getKey()).append("=").append(paramValue);
                    first = false;
                }
            }
            
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            
            // Configure connection timeout if available in variables
            if (mergedVariables.containsKey("connectionTimeout")) {
                try {
                    int timeout = Integer.parseInt(mergedVariables.get("connectionTimeout"));
                    connection.setConnectTimeout(timeout);
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            // Configure read timeout if available in variables
            if (mergedVariables.containsKey("socketTimeout")) {
                try {
                    int timeout = Integer.parseInt(mergedVariables.get("socketTimeout"));
                    connection.setReadTimeout(timeout);
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            // Set headers with variable substitution
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    String headerValue = header.getValue();
                    if (mergedVariables != null && !mergedVariables.isEmpty()) {
                        headerValue = templateProcessor.processTemplate(headerValue, mergedVariables);
                    }
                    connection.setRequestProperty(header.getKey(), headerValue);
                }
            }
            
            // Set content type from variables if present and not already set
            if (body != null && connection.getRequestProperty("Content-Type") == null) {
                String contentType = mergedVariables.getOrDefault("contentType", "application/json");
                connection.setRequestProperty("Content-Type", contentType);
            }
            
            // Set body for POST, PUT, etc.
            if (body != null && !method.equals("GET") && !method.equals("DELETE")) {
                connection.setDoOutput(true);
                connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                connection.getOutputStream().flush();
                connection.getOutputStream().close();
            }
            
            // Get response
            int statusCode = connection.getResponseCode();
            response.setStatusCode(statusCode);
            
            // Read response body
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            
            try {
                while ((length = connection.getInputStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                response.setBody(result.toString(StandardCharsets.UTF_8.name()));
            } catch (Exception e) {
                // Handle error stream if available
                if (connection.getErrorStream() != null) {
                    result = new ByteArrayOutputStream();
                    while ((length = connection.getErrorStream().read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    response.setBody(result.toString(StandardCharsets.UTF_8.name()));
                } else {
                    response.setBody("Error: " + e.getMessage());
                }
            }
            
            // Get response headers
            for (String key : connection.getHeaderFields().keySet()) {
                if (key != null) {
                    response.addHeader(key, connection.getHeaderField(key));
                }
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            throw new Exception("HTTP Protocol Error: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    @Override
    public TestResult execute(Request request, Map<String, String> variables) throws Exception {
        // Call the existing execute method with parameters from the request
        Response response = execute(
            request.getEndpoint(),
            request.getMethod(),
            request.getBody(),
            request.getHeaders(),
            request.getParams(),
            variables
        );
        
        // Convert response to TestResult
        TestResult result = new TestResult();
        if (response != null) {
            result.setSuccess(response.isSuccess());
            result.setStatusCode(response.getStatusCode());
            result.setResponseBody(response.getBody());
            result.setResponseTime(response.getResponseTime());
            result.setHeaders(response.getHeaders());
            result.setTestName(request.getName());
            
            // Store received data in the result
            if (response.getBody() != null) {
                result.setReceivedBytes(response.getBody().getBytes(StandardCharsets.UTF_8).length);
            }
        } else {
            result.setSuccess(false);
            result.setStatusCode(500);
            result.setError("Null response returned");
            result.setTestName(request.getName());
        }
        
        return result;
    }
}
