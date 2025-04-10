package io.perftest.protocol;

import io.perftest.model.Response;
import io.perftest.util.TemplateProcessor;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * JMS Protocol implementation for messaging with variable support
 */
public class JmsProtocol implements Protocol {
    
    private final TemplateProcessor templateProcessor;
    private Map<String, String> globalVariables;
    
    public JmsProtocol() {
        templateProcessor = new TemplateProcessor();
        globalVariables = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "jms";
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
        Connection connection = null;
        
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
            
            // Extract connection properties from processed headers
            String initialContextFactory = processedHeaders.get("initialContextFactory");
            String providerUrl = processedHeaders.get("providerUrl");
            String connectionFactory = processedHeaders.get("connectionFactory");
            String username = processedHeaders.get("username");
            String password = processedHeaders.get("password");
            String replyQueue = processedHeaders.get("replyQueue");
            
            // Use variables as fallback for connection properties if not in headers
            if (initialContextFactory == null) {
                initialContextFactory = mergedVariables.get("jmsInitialContextFactory");
            }
            if (providerUrl == null) {
                providerUrl = mergedVariables.get("jmsProviderUrl");
            }
            if (connectionFactory == null) {
                connectionFactory = mergedVariables.get("jmsConnectionFactory");
            }
            if (username == null) {
                username = mergedVariables.get("jmsUsername");
            }
            if (password == null) {
                password = mergedVariables.get("jmsPassword");
            }
            if (replyQueue == null) {
                replyQueue = mergedVariables.get("jmsReplyQueue");
            }
            
            // Set up JNDI context
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            props.setProperty(Context.PROVIDER_URL, providerUrl);
            if (username != null && password != null) {
                props.setProperty(Context.SECURITY_PRINCIPAL, username);
                props.setProperty(Context.SECURITY_CREDENTIALS, password);
            }
            
            Context ctx = new InitialContext(props);
            
            // Get ConnectionFactory and create Connection
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup(connectionFactory);
            if (username != null && password != null) {
                connection = factory.createConnection(username, password);
            } else {
                connection = factory.createConnection();
            }
            
            // Create session
            boolean transacted = Boolean.parseBoolean(mergedVariables.getOrDefault("jmsTransacted", "false"));
            int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
            if (mergedVariables.containsKey("jmsAcknowledgeMode")) {
                String ackMode = mergedVariables.get("jmsAcknowledgeMode");
                if ("CLIENT_ACKNOWLEDGE".equalsIgnoreCase(ackMode)) {
                    acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
                } else if ("DUPS_OK_ACKNOWLEDGE".equalsIgnoreCase(ackMode)) {
                    acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;
                }
            }
            Session session = connection.createSession(transacted, acknowledgeMode);
            
            // Create destination (either queue or topic)
            Destination destination;
            if ("queue".equalsIgnoreCase(method)) {
                destination = session.createQueue(endpoint);
            } else if ("topic".equalsIgnoreCase(method)) {
                destination = session.createTopic(endpoint);
            } else {
                throw new IllegalArgumentException("Unsupported JMS method: " + method);
            }
            
            // Create producer
            MessageProducer producer = session.createProducer(destination);
            
            // Set delivery mode from variables
            if (mergedVariables.containsKey("jmsDeliveryMode")) {
                String deliveryMode = mergedVariables.get("jmsDeliveryMode");
                if ("NON_PERSISTENT".equalsIgnoreCase(deliveryMode)) {
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                } else if ("PERSISTENT".equalsIgnoreCase(deliveryMode)) {
                    producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                }
            }
            
            // Create message
            TextMessage message = session.createTextMessage(body);
            
            // Process params with variables
            Map<String, String> processedParams = new HashMap<>();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String value = entry.getValue();
                    if (mergedVariables != null && !mergedVariables.isEmpty()) {
                        value = templateProcessor.processTemplate(value, mergedVariables);
                    }
                    processedParams.put(entry.getKey(), value);
                }
            }
            
            // Set message properties from processed params
            for (Map.Entry<String, String> entry : processedParams.entrySet()) {
                message.setStringProperty(entry.getKey(), entry.getValue());
            }
            
            // Generate correlation ID for response correlation
            String correlationId = UUID.randomUUID().toString();
            message.setJMSCorrelationID(correlationId);
            
            // Set reply queue if specified
            if (replyQueue != null) {
                Destination replyDest = session.createQueue(replyQueue);
                message.setJMSReplyTo(replyDest);
            }
            
            // Start connection
            connection.start();
            
            // Send message
            producer.send(message);
            
            // Wait for response if reply queue is specified
            if (replyQueue != null) {
                Destination replyDest = session.createQueue(replyQueue);
                MessageConsumer consumer = session.createConsumer(replyDest, "JMSCorrelationID = '" + correlationId + "'");
                
                // Wait for response with timeout from variables or default to 5 seconds
                long timeout = 5000; // Default: 5 seconds
                if (mergedVariables.containsKey("jmsTimeout")) {
                    try {
                        timeout = Long.parseLong(mergedVariables.get("jmsTimeout"));
                    } catch (NumberFormatException e) {
                        // Ignore if not a valid number
                    }
                }
                
                Message responseMessage = consumer.receive(timeout);
                
                if (responseMessage instanceof TextMessage) {
                    response.setBody(((TextMessage) responseMessage).getText());
                    response.setStatusCode(200);
                } else if (responseMessage != null) {
                    response.setBody("Received non-text response");
                    response.setStatusCode(200);
                } else {
                    response.setBody("No response received within timeout");
                    response.setStatusCode(408); // Request Timeout
                }
                
                consumer.close();
            } else {
                // No response expected
                response.setBody("Message sent successfully");
                response.setStatusCode(200);
            }
            
            // Commit transaction if transacted
            if (transacted) {
                session.commit();
            }
            
            producer.close();
            session.close();
            
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("JMS Error: " + e.getMessage());
            throw new Exception("JMS Protocol Error: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        return response;
    }
}
