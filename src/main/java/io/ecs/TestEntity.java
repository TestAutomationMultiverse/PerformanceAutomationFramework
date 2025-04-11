package io.ecs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a test entity in the ECS framework
 * 
 * In the Entity-Component-System pattern, entities are just identifiers
 * with components attached to them. This class provides the core entity
 * functionality for the performance testing framework.
 */
public class TestEntity {
    private final String id;
    private final Map<Class<?>, Object> components;
    private String name;
    
    /**
     * Create a new test entity with a random ID
     */
    public TestEntity() {
        this(UUID.randomUUID().toString());
    }
    
    /**
     * Create a test entity with a specific ID
     * 
     * @param id Entity identifier
     */
    public TestEntity(String id) {
        this.id = id;
        this.components = new HashMap<>();
        this.name = "Entity-" + id;
    }
    
    /**
     * Get the entity ID
     * 
     * @return Entity ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the entity name
     * 
     * @return Entity name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the entity name
     * 
     * @param name New entity name
     * @return This entity for chaining
     */
    public TestEntity setName(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Add a component to this entity
     * 
     * @param <T> Component type
     * @param componentClass Class of the component
     * @param component Component instance
     * @return This entity for chaining
     */
    public <T> TestEntity addComponent(Class<T> componentClass, T component) {
        components.put(componentClass, component);
        return this;
    }
    
    /**
     * Get a component from this entity
     * 
     * @param <T> Component type
     * @param componentClass Class of the component to retrieve
     * @return The component or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getComponent(Class<T> componentClass) {
        return (T) components.get(componentClass);
    }
    
    /**
     * Check if this entity has a specific component
     * 
     * @param <T> Component type
     * @param componentClass Class of the component to check
     * @return True if the entity has the component, false otherwise
     */
    public <T> boolean hasComponent(Class<T> componentClass) {
        return components.containsKey(componentClass);
    }
    
    /**
     * Remove a component from this entity
     * 
     * @param <T> Component type
     * @param componentClass Class of the component to remove
     * @return This entity for chaining
     */
    public <T> TestEntity removeComponent(Class<T> componentClass) {
        components.remove(componentClass);
        return this;
    }
}