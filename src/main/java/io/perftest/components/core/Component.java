package io.perftest.components.core;

import io.perftest.entities.request.RequestEntity;

/**
 * Core Component interface for the Entity-Component-System (ECS) architecture.
 * 
 * <p>In the ECS pattern, Components represent specific capabilities or traits that
 * can be attached to Entities. They encapsulate functionality and processing logic
 * that can be applied to entities of specific types.</p>
 * 
 * <p>Components in this framework are processing units that transform entities of type E
 * into results of type T. For performance testing, they typically convert request entities
 * into JMeter samplers or other test elements.</p>
 * 
 * <p>This design enables a modular approach to test creation where different protocols
 * (HTTP, GraphQL, JDBC, etc.) can be supported through specialized components, while
 * systems can operate on any entity regardless of its specific protocol.</p>
 * 
 * <p>The generics in this interface ensure type safety when processing entities:</p>
 * <ul>
 *   <li>E - The specific entity type this component can process</li>
 *   <li>T - The result type produced when processing an entity</li>
 * </ul>
 * 
 * @param <E> The entity type this component processes, must extend RequestEntity
 * @param <T> The result type produced by processing the entity
 * 
 * @since 1.0
 */
public interface Component<E extends RequestEntity, T> {
    
    /**
     * Processes an entity and transforms it into a result of type T.
     * 
     * <p>This is the core method of any component, responsible for applying
     * the component's logic to an entity. For performance testing components,
     * this typically involves converting request entities into protocol-specific
     * test samplers or other JMeter test elements.</p>
     * 
     * <p>Implementations should validate the entity before processing and
     * handle any protocol-specific requirements or transformations.</p>
     * 
     * @param entity The entity to process, must not be null and must be of type E
     * @return The result of processing, of type T
     * @throws io.perftest.exception.ComponentException If processing fails or validation errors occur
     */
    T process(E entity);
}
