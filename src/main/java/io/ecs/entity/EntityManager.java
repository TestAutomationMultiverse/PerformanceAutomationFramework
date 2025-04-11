package io.ecs.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manager for test entities in the ECS framework
 * 
 * This class provides centralized management of entities, allowing systems
 * to query and process entities with specific components.
 */
public class EntityManager {
    private final Map<String, TestEntity> entities;
    
    /**
     * Create a new entity manager
     */
    public EntityManager() {
        this.entities = new HashMap<>();
    }
    
    /**
     * Create a new entity
     * 
     * @return The created entity
     */
    public TestEntity createEntity() {
        TestEntity entity = new TestEntity();
        entities.put(entity.getId(), entity);
        return entity;
    }
    
    /**
     * Create a new entity with a specific name
     * 
     * @param name Entity name
     * @return The created entity
     */
    public TestEntity createEntity(String name) {
        TestEntity entity = createEntity();
        entity.setName(name);
        return entity;
    }
    
    /**
     * Add an existing entity to the manager
     * 
     * @param entity Entity to add
     * @return The added entity
     */
    public TestEntity addEntity(TestEntity entity) {
        entities.put(entity.getId(), entity);
        return entity;
    }
    
    /**
     * Get an entity by ID
     * 
     * @param id Entity ID
     * @return The entity or null if not found
     */
    public TestEntity getEntity(String id) {
        return entities.get(id);
    }
    
    /**
     * Get all entities
     * 
     * @return List of all entities
     */
    public List<TestEntity> getAllEntities() {
        return new ArrayList<>(entities.values());
    }
    
    /**
     * Get entities with a specific component
     * 
     * @param <T> Component type
     * @param componentClass Component class to filter by
     * @return List of entities with the component
     */
    public <T> List<TestEntity> getEntitiesWithComponent(Class<T> componentClass) {
        return entities.values().stream()
            .filter(e -> e.hasComponent(componentClass))
            .collect(Collectors.toList());
    }
    
    /**
     * Get entities with multiple components
     * 
     * @param componentClasses Component classes to filter by
     * @return List of entities with all specified components
     */
    public List<TestEntity> getEntitiesWithComponents(Class<?>... componentClasses) {
        return entities.values().stream()
            .filter(e -> {
                for (Class<?> componentClass : componentClasses) {
                    if (!e.hasComponent(componentClass)) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Remove an entity by ID
     * 
     * @param id Entity ID
     * @return True if entity was removed, false if not found
     */
    public boolean removeEntity(String id) {
        return entities.remove(id) != null;
    }
    
    /**
     * Remove an entity
     * 
     * @param entity Entity to remove
     * @return True if entity was removed, false if not found
     */
    public boolean removeEntity(TestEntity entity) {
        return entity != null && removeEntity(entity.getId());
    }
    
    /**
     * Process all entities with a specific component
     * 
     * @param <T> Component type
     * @param componentClass Component class to filter by
     * @param processor Function to process each entity
     */
    public <T> void processEntitiesWithComponent(Class<T> componentClass, Consumer<TestEntity> processor) {
        getEntitiesWithComponent(componentClass).forEach(processor);
    }
    
    /**
     * Clear all entities
     */
    public void clear() {
        entities.clear();
    }
    
    /**
     * Get the number of entities
     * 
     * @return Number of entities
     */
    public int getEntityCount() {
        return entities.size();
    }
}