package io.perftest.systems;

import io.perftest.entities.core.Entity;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.util.Map;

/**
 * Core System interface for the Entity-Component-System (ECS) architecture.
 * 
 * <p>In the ECS pattern, Systems are responsible for processing entities and 
 * executing business logic on entities with specific components. Systems define
 * how entities and their components interact with each other and the external world.</p>
 * 
 * <p>Systems encapsulate the core behavior and algorithms of the performance testing
 * framework, separating logic from data (which is stored in entities) and processing
 * strategies (which are defined by components).</p>
 * 
 * <p>This design allows for high flexibility, as new functionality can be added by
 * implementing new Systems without modifying existing code.</p>
 * 
 * @since 1.0
 */
public interface System {
    
    /**
     * Initializes the system with default settings and configures any resources needed.
     * This method should be called before the system processes any entities.
     * 
     * <p>Implementations may use this method to set up connections, allocate resources,
     * or prepare the execution environment.</p>
     */
    void init();
    
    /**
     * Processes an entity according to the system's specific logic.
     * 
     * <p>This is the main method for implementing the business logic of the system.
     * It should examine the entity, extract or manipulate relevant data, and perform
     * appropriate actions based on the entity's components.</p>
     * 
     * @param entity The entity to process - must not be null
     */
    void process(Entity entity);
    
    /**
     * Processes the results after test execution is complete.
     * 
     * <p>This method allows systems to analyze test statistics, generate reports,
     * persist data, or perform other post-processing actions after test execution.</p>
     * 
     * @param stats Test execution statistics from JMeter DSL
     * @param context Additional contextual data that may be relevant for result processing
     */
    void processResults(TestPlanStats stats, Map<String, Object> context);
    
    /**
     * Cleans up any resources used by this system.
     * 
     * <p>This method should release any resources that were allocated during initialization
     * or processing. It should be called when the system is no longer needed to prevent
     * resource leaks.</p>
     * 
     * <p>Examples of cleanup actions include closing database connections, shutting down
     * thread pools, or releasing file handles.</p>
     */
    void cleanup();
}
