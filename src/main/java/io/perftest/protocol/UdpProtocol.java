package io.perftest.protocol;

import io.perftest.model.Response;
import io.perftest.util.TemplateProcessor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * UDP Protocol implementation with variable support
 */
public class UdpProtocol implements Protocol {
    
    private final TemplateProcessor templateProcessor;
    private Map<String, String> globalVariables;
    
    public UdpProtocol() {
        templateProcessor = new TemplateProcessor();
        globalVariables = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "udp";
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
        DatagramSocket socket = null;
        
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
            
            // Create and configure socket
            socket = new DatagramSocket();
            
            // Configure socket timeout if available in variables
            int timeout = 5000; // Default 5 seconds
            if (mergedVariables.containsKey("udpTimeout")) {
                try {
                    timeout = Integer.parseInt(mergedVariables.get("udpTimeout"));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            socket.setSoTimeout(timeout);
            
            // Configure buffer size if available in variables
            int bufferSize = 1024; // Default 1KB
            if (mergedVariables.containsKey("udpBufferSize")) {
                try {
                    bufferSize = Integer.parseInt(mergedVariables.get("udpBufferSize"));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            // Convert message to bytes
            byte[] sendData = body.getBytes();
            
            // Create and send packet
            InetAddress address = InetAddress.getByName(host);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(sendPacket);
            
            // Prepare to receive response
            byte[] receiveData = new byte[bufferSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            
            // Receive response
            socket.receive(receivePacket);
            String responseBody = new String(receivePacket.getData(), 0, receivePacket.getLength());
            
            response.setStatusCode(200); // Using 200 as a default for UDP
            response.setBody(responseBody);
            
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("UDP Error: " + e.getMessage());
            throw new Exception("UDP Protocol Error: " + e.getMessage(), e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        
        return response;
    }
}
