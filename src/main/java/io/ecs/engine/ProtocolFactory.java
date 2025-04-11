package io.ecs.engine;

import io.ecs.protocols.HttpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating protocol implementations
 * This is a unified ProtocolFactory that creates instances of our consolidated Protocol implementations
 */
public class ProtocolFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ProtocolFactory.class);
    private static final Map<String, Protocol> protocolCache = new HashMap<>();
    
    /**
     * Get a protocol implementation by name
     * 
     * @param protocolName Name of the protocol (HTTP, HTTPS, etc.)
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
     * Currently only HTTP/HTTPS protocols are supported
     */
    public static Protocol createProtocol(String protocolName) {
        switch (protocolName) {
            case "HTTP":
            case "HTTPS":
                return new HttpProtocol();
            default:
                logger.warn("Unsupported protocol: {}. Using HTTP as default.", protocolName);
                return new HttpProtocol(); // Return HTTP by default for now
        }
    }
}