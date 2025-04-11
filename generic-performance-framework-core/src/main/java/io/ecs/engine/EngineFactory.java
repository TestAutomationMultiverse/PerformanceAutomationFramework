package io.ecs.engine;

import io.ecs.model.ExecutionConfig;
import io.ecs.util.EcsLogger;

/**
 * Factory for creating engine implementations
 */
public class EngineFactory {
    
    private static final EcsLogger logger = EcsLogger.getLogger(EngineFactory.class);
    
    /**
     * Get an engine implementation by name
     * 
     * @param engineName Name of the engine (jmeter-dsl, jmeter-treebuilder, gatling, custom)
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
                
            case "jmeter-treebuilder":
            case "jmtree":
            case "JMTREE":
                logger.info("Creating JMeter TreeBuilder engine (experimental)");
                return new JMTreeBuilderEngine(config);
                
            case "gatling":
            case "GATLING":
                logger.info("Creating Gatling engine");
                // For GatlingEngine, we need to convert to engine.ExecutionConfig
                io.ecs.engine.ExecutionConfig engineConfig = new io.ecs.engine.ExecutionConfig();
                engineConfig.setThreads(config.getThreads());
                engineConfig.setIterations(config.getIterations());
                engineConfig.setRampUpSeconds(config.getRampUpSeconds());
                engineConfig.setHoldSeconds(config.getHoldSeconds());
                engineConfig.setVariables(config.getVariables());
                return new GatlingEngine(engineConfig);
                
            // Add more engines here as needed
                
            default:
                logger.warn("Unknown engine: {}, using JMeter DSL as default", engineName);
                return new JMDSLEngine(config);
        }
    }
}