package io.perftest.entities.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base entity class in the ECS architecture.
 * Entities are containers for components and data, but have no behavior themselves.
 */
public abstract class Entity {
    private final String id;
    private final Map<Class<?>, Object> components = new HashMap<>();
    private final Map<String, Object> properties = new HashMap<>();
    
    protected Entity() {
        this.id = UUID.randomUUID().toString();
    }
    
    /**
     * Get the unique identifier for this entity
     * 
     * @return The entity's unique ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Add a component to this entity
     * 
     * @param componentClass The class of the component
     * @param component The component instance
     * @param <T> The component type
     * @return This entity for chaining
     */
    public <T> Entity addComponent(Class<T> componentClass, T component) {
        components.put(componentClass, component);
        return this;
    }
    
    /**
     * Get a component from this entity
     * 
     * @param componentClass The class of the component to retrieve
     * @param <T> The component type
     * @return The component instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getComponent(Class<T> componentClass) {
        return (T) components.get(componentClass);
    }
    
    /**
     * Check if this entity has a specific component
     * 
     * @param componentClass The component class to check for
     * @return True if the entity has the component, false otherwise
     */
    public boolean hasComponent(Class<?> componentClass) {
        return components.containsKey(componentClass);
    }
    
    /**
     * Remove a component from this entity
     * 
     * @param componentClass The class of the component to remove
     * @return This entity for chaining
     */
    public Entity removeComponent(Class<?> componentClass) {
        components.remove(componentClass);
        return this;
    }
    
    /**
     * Set a property on this entity
     * 
     * @param key The property key
     * @param value The property value
     * @return This entity for chaining
     */
    public Entity setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }
    
    /**
     * Get a property from this entity
     * 
     * @param key The property key
     * @return The property value, or null if not found
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * Get a property with a specific type
     * 
     * @param key The property key
     * @param defaultValue The default value to return if the property is not found
     * @param <T> The property type
     * @return The property value, or the default value if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        return properties.containsKey(key) ? (T) properties.get(key) : defaultValue;
    }
    
    /**
     * Get all properties of this entity
     * 
     * @return An unmodifiable map of all properties
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
