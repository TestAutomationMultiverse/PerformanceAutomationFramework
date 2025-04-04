package io.perftest.systems;

import io.perftest.entities.core.Entity;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.util.Map;

/**
 * Base system interface in the ECS architecture.
 * Systems provide behavior that operates on entities with specific components.
 */
public interface System {
    
    /**
     * Initialize the system with default settings
     */
    void init();
    
    /**
     * Process an entity
     * 
     * @param entity The entity to process
     */
    void process(Entity entity);
    
    /**
     * Process the results of a test execution
     * 
     * @param stats The test statistics
     * @param context Additional context information
     */
    void processResults(TestPlanStats stats, Map<String, Object> context);
    
    /**
     * Clean up any resources used by this system
     */
    void cleanup();
}
