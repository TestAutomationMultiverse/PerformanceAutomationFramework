package io.perftest.entities.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The core Entity class in the Entity-Component-System (ECS) architecture.
 * 
 * <p>In the ECS pattern, Entities are lightweight containers for data and components.
 * They have identity (through a unique ID) but no behavior of their own. Instead,
 * behaviors are implemented in Systems that operate on Entities with specific Components.</p>
 * 
 * <p>Entities store both components (which define capabilities and behaviors) and 
 * properties (which store arbitrary data). This separation allows for a flexible 
 * and extensible design where new capabilities can be added to entities without
 * modifying existing code.</p>
 * 
 * <p>This implementation uses a type-safe component registry where components are
 * indexed by their class, and a string-based property map for storing additional
 * data.</p>
 * 
 * @since 1.0
 */
public abstract class Entity {
    private final String id;
    private final Map<Class<?>, Object> components = new HashMap<>();
    private final Map<String, Object> properties = new HashMap<>();
    
    /**
     * Creates a new Entity with a randomly generated UUID.
     * Each entity has a guaranteed unique identifier that can be used
     * to distinguish it from other entities in the system.
     */
    protected Entity() {
        this.id = UUID.randomUUID().toString();
    }
    
    /**
     * Retrieves the unique identifier for this entity.
     * 
     * <p>This ID is guaranteed to be unique across all entities
     * and remains constant for the lifetime of the entity.</p>
     * 
     * @return The entity's unique ID as a String representation of a UUID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Adds a component to this entity, indexing it by its class type.
     * 
     * <p>Components define the capabilities and characteristics of an entity.
     * Adding a component to an entity indicates that the entity has the capability
     * represented by that component.</p>
     * 
     * <p>If a component of the same class already exists, it will be replaced.</p>
     * 
     * @param componentClass The class of the component to add, used as a key
     * @param component The component instance to attach to this entity
     * @param <T> The type of the component
     * @return This entity for method chaining
     */
    public <T> Entity addComponent(Class<T> componentClass, T component) {
        components.put(componentClass, component);
        return this;
    }
    
    /**
     * Retrieves a component of the specified type from this entity.
     * 
     * <p>This method performs a type-safe cast of the component to the
     * requested type, assuming the component was added with the correct type.</p>
     * 
     * @param componentClass The class of the component to retrieve
     * @param <T> The expected type of the component
     * @return The component instance, or null if no component of that type exists
     */
    @SuppressWarnings("unchecked")
    public <T> T getComponent(Class<T> componentClass) {
        return (T) components.get(componentClass);
    }
    
    /**
     * Checks if this entity has a component of the specified type.
     * 
     * <p>This method is useful for determining whether a specific system
     * should process this entity, based on the presence of required components.</p>
     * 
     * @param componentClass The component class to check for
     * @return True if the entity has a component of the specified type, false otherwise
     */
    public boolean hasComponent(Class<?> componentClass) {
        return components.containsKey(componentClass);
    }
    
    /**
     * Removes a component of the specified type from this entity.
     * 
     * <p>This method removes the capability represented by the component
     * from the entity. Systems that operate on that component type will
     * no longer process this entity.</p>
     * 
     * @param componentClass The class of the component to remove
     * @return This entity for method chaining
     */
    public Entity removeComponent(Class<?> componentClass) {
        components.remove(componentClass);
        return this;
    }
    
    /**
     * Sets a named property on this entity with the specified value.
     * 
     * <p>Properties allow storing arbitrary data that doesn't warrant
     * a full component. They can be used for simple flags, counters,
     * or other lightweight data.</p>
     * 
     * <p>If a property with the same key already exists, it will be replaced.</p>
     * 
     * @param key The property key (name)
     * @param value The property value to store
     * @return This entity for method chaining
     */
    public Entity setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }
    
    /**
     * Retrieves a property value by its key.
     * 
     * @param key The property key to look up
     * @return The property value, or null if no property with that key exists
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * Retrieves a property value with type information, returning a default
     * value if the property doesn't exist.
     * 
     * <p>This method performs an unchecked cast to the expected type.
     * Care should be taken to ensure that the stored property is of
     * the expected type to avoid ClassCastExceptions.</p>
     * 
     * @param key The property key to look up
     * @param defaultValue The default value to return if the property doesn't exist
     * @param <T> The expected type of the property value
     * @return The property value cast to type T, or the default value if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        return properties.containsKey(key) ? (T) properties.get(key) : defaultValue;
    }
}
