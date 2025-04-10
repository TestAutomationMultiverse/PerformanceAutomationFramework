package io.perftest.engine;

import io.perftest.model.ExecutionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating engine implementations
 */
public class EngineFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(EngineFactory.class);
    
    /**
     * Get an engine implementation by name
     * 
     * @param engineName Name of the engine (jmeter-dsl, jmeter-api, custom)
     * @param config Configuration for the engine
     * @return Engine implementation
     */
    public static Engine getEngine(String engineName, ExecutionConfig config) {
        if (engineName == null) {
            logger.info("No engine specified, using JMeter DSL by default");
            return new JMDSLEngine(config);
        }
        
        // Normalize engine name
        String normalizedName = engineName.trim().toLowerCase();
        
        switch (normalizedName) {
            case "jmeter-dsl":
            case "jmdsl":
                logger.info("Creating JMeter DSL engine");
                return new JMDSLEngine(config);
                
            case "jmeter-api":
            case "jmapi":
                logger.info("Creating JMeter API engine");
                return new JMAPIEngine(config);
                
            // Add more engines here as needed
                
            default:
                logger.warn("Unknown engine: {}, using JMeter DSL as default", engineName);
                return new JMDSLEngine(config);
        }
    }
}