package io.perftest.systems;

import io.perftest.components.core.Component;
import io.perftest.entities.request.RequestEntity;
import io.perftest.exception.ErrorCode;
import io.perftest.exception.ErrorHandler;
import io.perftest.exception.PerfTestException;
import io.perftest.exception.Result;
import io.perftest.exception.TestExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * The TestSystem is the core orchestrator in the Entity-Component-System (ECS) architecture.
 * 
 * <p>This system manages the relationship between different entity types and their 
 * corresponding processing components. It's responsible for routing entities to the 
 * appropriate component and handling the results of that processing.</p>
 * 
 * <p>In the context of performance testing, TestSystem acts as the coordinator between
 * request entities (which hold test data) and protocol-specific components (which generate
 * JMeter test elements). It enables the framework to support multiple protocols while
 * maintaining a consistent interface for test execution.</p>
 * 
 * <p>TestSystem implements robust error handling with both exception-throwing and safe
 * result-returning variants of processing methods, allowing clients to choose the
 * error handling strategy that best fits their needs.</p>
 * 
 * @since 1.0
 */
public class TestSystem {
    private static final Logger logger = LoggerFactory.getLogger(TestSystem.class);
    
    private final Map<Class<?>, Component<?, ?>> components = new HashMap<>();
    
    /**
     * Gets the count of registered components.
     * 
     * @return The number of components registered with this TestSystem
     */
    public int getComponentCount() {
        return components.size();
    }
    
    /**
     * Registers a component to process a specific type of entity.
     * 
     * <p>This method establishes the mapping between entity types and their
     * processing components. When an entity is processed, the system uses
     * this mapping to find the appropriate component.</p>
     * 
     * <p>The type parameters enforce that the component is capable of
     * processing the specified entity type.</p>
     * 
     * @param <E> The entity type the component can process
     * @param <T> The result type produced by the component
     * @param entityClass The class of entities this component can process
     * @param component The component instance to register
     * @throws PerfTestException If entityClass or component is null
     */
    public <E extends RequestEntity, T> void addComponent(Class<E> entityClass, Component<E, T> component) {
        logger.info("Adding component for entity class: {}", entityClass.getSimpleName());
        ErrorHandler.validateNotNull(entityClass, ErrorCode.GENERAL_ERROR, "Entity class cannot be null");
        ErrorHandler.validateNotNull(component, ErrorCode.GENERAL_ERROR, "Component cannot be null");
        
        components.put(entityClass, component);
        logger.debug("Component added successfully: {}", component.getClass().getSimpleName());
    }
    
    /**
     * Processes an entity using a component of a specific type.
     * 
     * <p>This method retrieves the component registered for the entity's type,
     * verifies that it matches the expected component type, and then uses it
     * to process the entity.</p>
     * 
     * <p>This variant throws exceptions if errors occur during processing.</p>
     * 
     * @param <E> The entity type
     * @param <T> The result type expected from the component
     * @param entity The entity to process
     * @param componentClass The expected class of the component
     * @return The result of processing the entity
     * @throws TestExecutionException If validation fails or processing errors occur
     */
    @SuppressWarnings("unchecked")
    public <E extends RequestEntity, T> T processEntity(E entity, Class<? extends Component<E, T>> componentClass) {
        logger.info("Processing entity with component: {}", componentClass.getSimpleName());
        ErrorHandler.validateNotNull(entity, ErrorCode.TEST_EXECUTION_ERROR, "Entity cannot be null");
        ErrorHandler.validateNotNull(componentClass, ErrorCode.TEST_EXECUTION_ERROR, "Component class cannot be null");
        
        return ErrorHandler.executeWithErrorHandling(() -> {
            Component<E, T> component = (Component<E, T>) components.get(entity.getClass());
            if (component == null) {
                throw new TestExecutionException(ErrorCode.TEST_EXECUTION_ERROR, 
                        "No component registered for entity type: " + entity.getClass().getSimpleName());
            }
            
            if (!componentClass.isAssignableFrom(component.getClass())) {
                throw new TestExecutionException(ErrorCode.TEST_EXECUTION_ERROR, 
                        "Component is not of expected type: " + componentClass.getSimpleName());
            }
            
            logger.debug("Processing entity with component: {}", component.getClass().getSimpleName());
            return component.process(entity);
        }, ErrorCode.TEST_EXECUTION_ERROR, "Error processing entity with component: " + componentClass.getSimpleName(), logger);
    }
    
    /**
     * Processes a request entity and returns a JMeter ThreadGroupChild (sampler).
     * 
     * <p>This is a specialized method for performance testing that converts
     * request entities into JMeter test elements that can be added to a test plan.</p>
     * 
     * <p>This method validates that the component registered for the entity's type
     * produces a result that is compatible with JMeter's ThreadGroupChild interface.</p>
     * 
     * <p>This variant throws exceptions if errors occur during processing.</p>
     * 
     * @param <E> The request entity type
     * @param request The request entity to process
     * @return A JMeter ThreadGroupChild (sampler) for the request
     * @throws TestExecutionException If validation fails or processing errors occur
     */
    @SuppressWarnings("unchecked")
    public <E extends RequestEntity> BaseThreadGroup.ThreadGroupChild processRequest(E request) {
        logger.info("Processing request entity: {}", request.getClass().getSimpleName());
        ErrorHandler.validateNotNull(request, ErrorCode.TEST_EXECUTION_ERROR, "Request entity cannot be null");
        
        return ErrorHandler.executeWithErrorHandling(() -> {
            Component<E, ?> component = (Component<E, ?>) components.get(request.getClass());
            if (component == null) {
                throw new TestExecutionException(ErrorCode.TEST_EXECUTION_ERROR, 
                        "No component registered for entity type: " + request.getClass().getSimpleName());
            }
            
            Object result = component.process(request);
            if (!(result instanceof BaseThreadGroup.ThreadGroupChild)) {
                throw new TestExecutionException(ErrorCode.TEST_EXECUTION_ERROR, 
                        "Component did not return a ThreadGroupChild: " + component.getClass().getSimpleName());
            }
            
            logger.debug("Request processed successfully: {}", request.getClass().getSimpleName());
            return (BaseThreadGroup.ThreadGroupChild) result;
        }, ErrorCode.TEST_EXECUTION_ERROR, "Error processing request entity: " + request.getClass().getSimpleName(), logger);
    }
    
    /**
     * Processes an entity with the component registered for its type.
     * 
     * <p>This is a simpler version of the processEntity method that does not
     * require specifying the expected component type. It uses the component
     * registered for the entity's class.</p>
     * 
     * <p>This variant throws exceptions if errors occur during processing.</p>
     * 
     * @param <E> The entity type
     * @param <T> The result type expected from the component
     * @param entity The entity to process
     * @return The result of processing the entity
     * @throws TestExecutionException If validation fails or processing errors occur
     */
    @SuppressWarnings("unchecked")
    public <E extends RequestEntity, T> T processEntity(E entity) {
        logger.info("Processing entity: {}", entity.getClass().getSimpleName());
        ErrorHandler.validateNotNull(entity, ErrorCode.TEST_EXECUTION_ERROR, "Entity cannot be null");
        
        return ErrorHandler.executeWithErrorHandling(() -> {
            Component<E, T> component = (Component<E, T>) components.get(entity.getClass());
            if (component == null) {
                throw new TestExecutionException(ErrorCode.TEST_EXECUTION_ERROR, 
                        "No component registered for entity type: " + entity.getClass().getSimpleName());
            }
            
            logger.debug("Processing entity with component: {}", component.getClass().getSimpleName());
            return component.process(entity);
        }, ErrorCode.TEST_EXECUTION_ERROR, "Error processing entity: " + entity.getClass().getSimpleName(), logger);
    }
    
    /**
     * Safely processes a request entity and returns a Result containing either a JMeter sampler or an error.
     * 
     * <p>This is a "safe" variant of processRequest that returns a Result object
     * instead of throwing exceptions. This allows callers to handle errors in a
     * more functional style.</p>
     * 
     * <p>If processing succeeds, the Result will contain a ThreadGroupChild.
     * If processing fails, the Result will contain error information.</p>
     * 
     * @param <E> The request entity type
     * @param request The request entity to process
     * @return A Result containing either the JMeter sampler or error information
     */
    @SuppressWarnings("unchecked")
    public <E extends RequestEntity> Result<BaseThreadGroup.ThreadGroupChild> safeProcessRequest(E request) {
        logger.info("Safely processing request entity: {}", request != null ? request.getClass().getSimpleName() : "null");
        
        if (request == null) {
            return Result.failure(ErrorCode.TEST_EXECUTION_ERROR, "Request entity cannot be null");
        }
        
        try {
            Component<E, ?> component = (Component<E, ?>) components.get(request.getClass());
            if (component == null) {
                return Result.failure(ErrorCode.TEST_EXECUTION_ERROR, 
                        "No component registered for entity type: " + request.getClass().getSimpleName());
            }
            
            Object result = component.process(request);
            if (!(result instanceof BaseThreadGroup.ThreadGroupChild)) {
                return Result.failure(ErrorCode.TEST_EXECUTION_ERROR, 
                        "Component did not return a ThreadGroupChild: " + component.getClass().getSimpleName());
            }
            
            logger.debug("Request processed successfully: {}", request.getClass().getSimpleName());
            return Result.success((BaseThreadGroup.ThreadGroupChild) result);
        } catch (Exception e) {
            logger.error("Error processing request entity: {}", e.getMessage(), e);
            if (e instanceof PerfTestException) {
                return Result.failure((PerfTestException) e);
            } else {
                return Result.failure(ErrorCode.TEST_EXECUTION_ERROR, 
                        "Error processing request entity: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Safely processes an entity and returns a Result containing either the processing output or an error.
     * 
     * <p>This is a "safe" variant of processEntity that returns a Result object
     * instead of throwing exceptions. This allows callers to handle errors in a
     * more functional style.</p>
     * 
     * <p>If processing succeeds, the Result will contain the output from the component.
     * If processing fails, the Result will contain error information.</p>
     * 
     * @param <E> The entity type
     * @param <T> The expected result type from the component
     * @param entity The entity to process
     * @return A Result containing either the processing output or error information
     */
    @SuppressWarnings("unchecked")
    public <E extends RequestEntity, T> Result<T> safeProcessEntity(E entity) {
        logger.info("Safely processing entity: {}", entity != null ? entity.getClass().getSimpleName() : "null");
        
        if (entity == null) {
            return Result.failure(ErrorCode.TEST_EXECUTION_ERROR, "Entity cannot be null");
        }
        
        try {
            Component<E, T> component = (Component<E, T>) components.get(entity.getClass());
            if (component == null) {
                return Result.failure(ErrorCode.TEST_EXECUTION_ERROR, 
                        "No component registered for entity type: " + entity.getClass().getSimpleName());
            }
            
            logger.debug("Processing entity with component: {}", component.getClass().getSimpleName());
            return Result.success(component.process(entity));
        } catch (Exception e) {
            logger.error("Error processing entity: {}", e.getMessage(), e);
            if (e instanceof PerfTestException) {
                return Result.failure((PerfTestException) e);
            } else {
                return Result.failure(ErrorCode.TEST_EXECUTION_ERROR, 
                        "Error processing entity: " + e.getMessage(), e);
            }
        }
    }
}
