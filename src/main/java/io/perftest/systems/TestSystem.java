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
 * The TestSystem manages components and processes requests
 * It follows the Entity-Component-System (ECS) architectural pattern
 */
public class TestSystem {
    private static final Logger logger = LoggerFactory.getLogger(TestSystem.class);
    
    private final Map<Class<?>, Component<?, ?>> components = new HashMap<>();
    
    /**
     * Add a component to the system
     * 
     * @param <E> Entity type
     * @param <T> Component result type
     * @param entityClass The entity class this component processes
     * @param component The component instance
     */
    public <E extends RequestEntity, T> void addComponent(Class<E> entityClass, Component<E, T> component) {
        logger.info("Adding component for entity class: {}", entityClass.getSimpleName());
        ErrorHandler.validateNotNull(entityClass, ErrorCode.GENERAL_ERROR, "Entity class cannot be null");
        ErrorHandler.validateNotNull(component, ErrorCode.GENERAL_ERROR, "Component cannot be null");
        
        components.put(entityClass, component);
        logger.debug("Component added successfully: {}", component.getClass().getSimpleName());
    }
    
    /**
     * Process an entity with the appropriate component
     * 
     * @param <E> Entity type
     * @param <T> Component result type
     * @param entity The entity to process
     * @param componentClass The component class to use
     * @return The result of processing
     * @throws TestExecutionException If an error occurs during processing
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
     * Process a request entity with its registered component and return the JMeter sampler
     * 
     * @param <E> Request entity type
     * @param request The request entity to process
     * @return The JMeter sampler (ThreadGroupChild)
     * @throws TestExecutionException If an error occurs during processing
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
     * Process an entity with the component registered for its type
     * 
     * @param <E> Entity type
     * @param <T> Component result type
     * @param entity The entity to process
     * @return The result of processing
     * @throws TestExecutionException If an error occurs during processing
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
     * Process a request entity with its registered component and return the JMeter sampler,
     * wrapped in a Result object.
     * 
     * @param <E> Request entity type
     * @param request The request entity to process
     * @return A Result containing the JMeter sampler (ThreadGroupChild) or an error
     */
    @SuppressWarnings("unchecked")
    public <E extends RequestEntity> Result<BaseThreadGroup.ThreadGroupChild> safeProcessRequest(E request) {
        logger.info("Safely processing request entity: {}", request.getClass().getSimpleName());
        
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
     * Process an entity with the component registered for its type,
     * wrapped in a Result object.
     * 
     * @param <E> Entity type
     * @param <T> Component result type
     * @param entity The entity to process
     * @return A Result containing the processing result or an error
     */
    @SuppressWarnings("unchecked")
    public <E extends RequestEntity, T> Result<T> safeProcessEntity(E entity) {
        logger.info("Safely processing entity: {}", entity.getClass().getSimpleName());
        
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
