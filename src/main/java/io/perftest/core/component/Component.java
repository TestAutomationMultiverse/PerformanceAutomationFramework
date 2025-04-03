package io.perftest.core.component;

/**
 * Base component interface in the ECS architecture.
 * Components contain data and functionality that can be attached to entities.
 */
public interface Component {
    
    /**
     * Initialize the component with default settings
     */
    void init();
    
    /**
     * Get the unique type identifier for this component
     * 
     * @return The component type name
     */
    String getType();
    
    /**
     * Clean up any resources used by this component
     */
    void cleanup();
}
