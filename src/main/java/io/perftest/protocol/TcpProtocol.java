package io.perftest.protocol;

import io.perftest.model.Response;
import io.perftest.util.TemplateProcessor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * TCP Protocol implementation with variable support
 */
public class TcpProtocol implements Protocol {
    
    private final TemplateProcessor templateProcessor;
    private Map<String, String> globalVariables;
    
    public TcpProtocol() {
        templateProcessor = new TemplateProcessor();
        globalVariables = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "tcp";
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
        Socket socket = null;
        
        try {
            // Merge global and request variables
            Map<String, String> mergedVariables = new HashMap<>(globalVariables);
            if (requestVariables != null) {
                mergedVariables.putAll(requestVariables);
            }
            
            // Process endpoint and body with variables
            if (mergedVariables != null && !mergedVariables.isEmpty()) {
                endpoint = templateProcessor.processTemplate(endpoint, mergedVariables);
                if (body != null) {
                    body = templateProcessor.processTemplate(body, mergedVariables);
                }
            }
            
            // Parse host and port from endpoint
            String[] parts = endpoint.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            // Configure socket timeout if available in variables
            int timeout = 30000; // Default 30 seconds
            if (mergedVariables.containsKey("socketTimeout")) {
                try {
                    timeout = Integer.parseInt(mergedVariables.get("socketTimeout"));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            // Connect to the server
            socket = new Socket(host, port);
            socket.setSoTimeout(timeout);
            
            // Send data
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(body);
            
            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            
            response.setStatusCode(200); // Using 200 as a default for TCP
            response.setBody(responseBuilder.toString());
            
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("TCP Error: " + e.getMessage());
            throw new Exception("TCP Protocol Error: " + e.getMessage(), e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        return response;
    }
}
