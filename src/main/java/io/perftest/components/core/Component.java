package io.perftest.components.core;

import io.perftest.entities.request.RequestEntity;

/**
 * Interface for components in the Entity-Component-System architecture
 * 
 * @param <E> The entity type this component processes
 * @param <T> The result type of processing
 */
public interface Component<E extends RequestEntity, T> {
    
    /**
     * Process an entity and return the result
     * 
     * @param entity The entity to process
     * @return The result of processing
     */
    T process(E entity);
}
