package io.perftest.protocol;

import io.perftest.model.Response;
import io.perftest.util.TemplateProcessor;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * MQTT Protocol implementation for IoT messaging with variable support
 */
public class MqttProtocol implements Protocol {
    
    private final TemplateProcessor templateProcessor;
    private Map<String, String> globalVariables;
    
    public MqttProtocol() {
        templateProcessor = new TemplateProcessor();
        globalVariables = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "mqtt";
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
        MqttClient client = null;
        
        try {
            // Merge global and request variables
            Map<String, String> mergedVariables = new HashMap<>(globalVariables);
            if (requestVariables != null) {
                mergedVariables.putAll(requestVariables);
            }
            
            // Process endpoint, method, and body with variables
            if (mergedVariables != null && !mergedVariables.isEmpty()) {
                endpoint = templateProcessor.processTemplate(endpoint, mergedVariables);
                method = templateProcessor.processTemplate(method, mergedVariables);
                if (body != null) {
                    body = templateProcessor.processTemplate(body, mergedVariables);
                }
            }
            
            // Process headers with variables
            Map<String, String> processedHeaders = new HashMap<>();
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    String value = entry.getValue();
                    if (mergedVariables != null && !mergedVariables.isEmpty()) {
                        value = templateProcessor.processTemplate(value, mergedVariables);
                    }
                    processedHeaders.put(entry.getKey(), value);
                }
            }
            
            // Determine protocol from variables or default to tcp
            String protocol = mergedVariables.getOrDefault("mqttProtocol", "tcp");
            
            // Extract MQTT parameters
            String brokerUrl = protocol + "://" + endpoint;
            
            // Use client ID from variables or generate one
            String clientId = mergedVariables.containsKey("mqttClientId") ? 
                    mergedVariables.get("mqttClientId") : 
                    "PerfTest-" + UUID.randomUUID().toString();
            
            // Using method field as the topic, but can override with variable
            String topic = mergedVariables.containsKey("mqttTopic") ? 
                    mergedVariables.get("mqttTopic") : method;
            
            // Get response topic from headers or variables
            String responseTopic = processedHeaders.get("responseTopic");
            if (responseTopic == null && mergedVariables.containsKey("mqttResponseTopic")) {
                responseTopic = mergedVariables.get("mqttResponseTopic");
            }
            
            // Get QoS from headers or variables
            int qos = 0; // Default QoS level
            if (processedHeaders.containsKey("qos")) {
                try {
                    qos = Integer.parseInt(processedHeaders.get("qos"));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            } else if (mergedVariables.containsKey("mqttQos")) {
                try {
                    qos = Integer.parseInt(mergedVariables.get("mqttQos"));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            // Get timeout from headers or variables
            int timeout = 5000; // Default timeout
            if (processedHeaders.containsKey("timeout")) {
                try {
                    timeout = Integer.parseInt(processedHeaders.get("timeout"));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            } else if (mergedVariables.containsKey("mqttTimeout")) {
                try {
                    timeout = Integer.parseInt(mergedVariables.get("mqttTimeout"));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            // Set up MQTT client
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(brokerUrl, clientId, persistence);
            
            MqttConnectOptions options = new MqttConnectOptions();
            
            // Configure connection options from variables
            options.setCleanSession(Boolean.parseBoolean(
                    mergedVariables.getOrDefault("mqttCleanSession", "true")));
            
            if (mergedVariables.containsKey("mqttKeepAlive")) {
                try {
                    options.setKeepAliveInterval(Integer.parseInt(mergedVariables.get("mqttKeepAlive")));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            if (mergedVariables.containsKey("mqttConnectionTimeout")) {
                try {
                    options.setConnectionTimeout(Integer.parseInt(mergedVariables.get("mqttConnectionTimeout")));
                } catch (NumberFormatException e) {
                    // Ignore if not a valid number
                }
            }
            
            // Set username/password from headers or variables
            String username = processedHeaders.get("username");
            String password = processedHeaders.get("password");
            
            if (username == null && mergedVariables.containsKey("mqttUsername")) {
                username = mergedVariables.get("mqttUsername");
            }
            
            if (password == null && mergedVariables.containsKey("mqttPassword")) {
                password = mergedVariables.get("mqttPassword");
            }
            
            if (username != null && password != null) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }
            
            // Connect to broker
            client.connect(options);
            
            // If expecting a response, set up a callback
            final CountDownLatch latch = new CountDownLatch(1);
            final StringBuilder responseBody = new StringBuilder();
            
            if (responseTopic != null) {
                client.subscribe(responseTopic, qos, (topic1, message) -> {
                    responseBody.append(new String(message.getPayload()));
                    latch.countDown();
                });
            }
            
            // Create message
            MqttMessage message = new MqttMessage(body.getBytes());
            message.setQos(qos);
            
            // Set retained flag from variables
            if (mergedVariables.containsKey("mqttRetained")) {
                message.setRetained(Boolean.parseBoolean(mergedVariables.get("mqttRetained")));
            }
            
            // Publish message
            client.publish(topic, message);
            
            // Wait for response if responseTopic is set
            if (responseTopic != null) {
                boolean received = latch.await(timeout, TimeUnit.MILLISECONDS);
                
                if (received) {
                    response.setBody(responseBody.toString());
                    response.setStatusCode(200);
                } else {
                    response.setBody("No response received within timeout");
                    response.setStatusCode(408); // Request Timeout
                }
                
                client.unsubscribe(responseTopic);
            } else {
                // No response expected
                response.setBody("Message published successfully");
                response.setStatusCode(200);
            }
            
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("MQTT Error: " + e.getMessage());
            throw new Exception("MQTT Protocol Error: " + e.getMessage(), e);
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                    client.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        return response;
    }
}
