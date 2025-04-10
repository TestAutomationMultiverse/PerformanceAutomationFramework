package io.perftest.engine;

import io.perftest.protocols.HttpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating protocol implementations
 */
public class ProtocolFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ProtocolFactory.class);
    private static final Map<String, Protocol> protocolCache = new HashMap<>();
    
    /**
     * Get a protocol implementation by name
     * 
     * @param protocolName Name of the protocol (HTTP, HTTPS, MQTT, etc.)
     * @return Protocol implementation or null if not supported
     */
    public static Protocol getProtocol(String protocolName) {
        if (protocolName == null) {
            return null;
        }
        
        // Normalize and cache protocols
        String normalizedName = protocolName.trim().toUpperCase();
        
        // Check cache first
        if (protocolCache.containsKey(normalizedName)) {
            return protocolCache.get(normalizedName);
        }
        
        // Create new protocol instance
        Protocol protocol = createProtocol(normalizedName);
        if (protocol != null) {
            protocolCache.put(normalizedName, protocol);
        }
        
        return protocol;
    }
    
    /**
     * Create a new protocol instance
     */
    private static Protocol createProtocol(String protocolName) {
        switch (protocolName) {
            case "HTTP":
            case "HTTPS":
                return new HttpProtocol();
            // More protocols will be added here in future releases
            default:
                logger.warn("Unsupported protocol: {}", protocolName);
                return null;
        }
    }
}