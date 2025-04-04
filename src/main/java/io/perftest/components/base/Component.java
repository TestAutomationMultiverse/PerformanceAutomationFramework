package io.perftest.components.base;

import io.perftest.entities.base.Entity;

/**
 * Interface for components in the Entity-Component-System architecture
 * 
 * @param <E> The entity type this component processes
 * @param <T> The result type of processing
 */
public interface Component<E extends Entity, T> {
    
    /**
     * Process an entity and return the result
     * 
     * @param entity The entity to process
     * @return The result of processing
     */
    T process(E entity);
}
