package io.perftest.protocol;

/**
 * Factory class to create appropriate Protocol instances
 */
public class ProtocolFactory {
    
    /**
     * Get a protocol implementation based on protocol name
     * 
     * @param protocolName the name of the protocol
     * @return Protocol implementation
     * @throws IllegalArgumentException if protocol is not supported
     */
    public static Protocol getProtocol(String protocolName) {
        if (protocolName == null || protocolName.isEmpty()) {
            throw new IllegalArgumentException("Protocol name cannot be null or empty");
        }
        
        switch (protocolName.toLowerCase()) {
            case "http":
                return new HttpProtocol();
            case "https":
                return new HttpsProtocol();
            case "tcp":
                return new TcpProtocol();
            case "udp":
                return new UdpProtocol();
            case "jdbc":
                throw new UnsupportedOperationException("JDBC protocol is not currently supported in this build");
            case "jms":
                return new JmsProtocol();
            case "mqtt":
                return new MqttProtocol();
            default:
                throw new IllegalArgumentException("Unsupported protocol: " + protocolName);
        }
    }
}
