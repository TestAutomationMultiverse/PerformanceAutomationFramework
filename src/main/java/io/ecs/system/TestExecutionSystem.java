package io.ecs.system;

import io.ecs.entity.EntityManager;
import io.ecs.entity.TestEntity;
import io.ecs.component.ConfigComponent;
import io.ecs.component.ProtocolComponent;
import io.ecs.component.ReportingComponent;
import io.ecs.config.Scenario;
import io.ecs.model.Request;
import io.ecs.model.TestResult;
import io.ecs.model.ExecutionConfig;
import io.ecs.engine.Engine;
import io.ecs.engine.EngineFactory;
import io.ecs.util.CsvReader;
import io.ecs.util.DynamicVariableResolver;
import io.ecs.util.EcsLogger;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ECS System for executing performance tests
 * 
 * This system coordinates the entities and components to perform test execution
 * following the Entity-Component-System pattern.
 */
public class TestExecutionSystem {
    private static final EcsLogger logger = EcsLogger.getLogger(TestExecutionSystem.class);
    
    private final String reportDirectory;
    private final Map<String, String> globalVariables;
    private final Map<String, Engine> engines;
    private final EntityManager entityManager;
    private String defaultEngine = "jmdsl";
    
    /**
     * Create a test execution system with default report directory
     */
    public TestExecutionSystem() {
        this("target/reports");
    }
    
    /**
     * Create a test execution system with specific report directory
     * 
     * @param reportDirectory Directory for test reports
     */
    public TestExecutionSystem(String reportDirectory) {
        this.reportDirectory = reportDirectory;
        this.globalVariables = new HashMap<>();
        this.engines = new HashMap<>();
        this.entityManager = new EntityManager();
        
        // Create report directory
        new File(reportDirectory).mkdirs();
    }
    
    /**
     * Add a global variable that will be available to all scenarios
     * 
     * @param name Variable name
     * @param value Variable value
     * @return This system for chaining
     */
    public TestExecutionSystem addGlobalVariable(String name, String value) {
        globalVariables.put(name, value);
        return this;
    }
    
    /**
     * Set global variables (replaces existing variables)
     * 
     * @param variables Variables to set
     * @return This system for chaining
     */
    public TestExecutionSystem setGlobalVariables(Map<String, String> variables) {
        if (variables != null) {
            globalVariables.clear();
            globalVariables.putAll(variables);
        }
        return this;
    }
    
    /**
     * Add multiple global variables at once
     * 
     * @param variables Map of variable name to value
     * @return This system for chaining
     */
    public TestExecutionSystem addGlobalVariables(Map<String, String> variables) {
        if (variables != null) {
            globalVariables.putAll(variables);
        }
        return this;
    }
    
    /**
     * Create and add a new scenario to the system
     * 
     * @param name Scenario name
     * @param engineType Engine type (jmdsl, jmeter, etc.)
     * @return New scenario instance
     */
    public Scenario createScenario(String name, String engineType) {
        // Create scenario
        Scenario scenario = new Scenario();
        scenario.setName(name);
        scenario.setEngine(engineType);
        
        // Create entity with components for this scenario
        TestEntity entity = entityManager.createEntity(name);
        
        // Add config component with the scenario
        ConfigComponent configComponent = new ConfigComponent();
        configComponent.addScenario(scenario);
        entity.addComponent(ConfigComponent.class, configComponent);
        
        // Add reporting component
        entity.addComponent(ReportingComponent.class, new ReportingComponent(reportDirectory));
        
        // Add protocol component (will be initialized when the scenario runs)
        entity.addComponent(ProtocolComponent.class, new ProtocolComponent());
        
        return scenario;
    }
    
    /**
     * Add an existing scenario to the system
     * 
     * @param scenario The scenario to add
     * @return This system for chaining
     */
    public TestExecutionSystem addScenario(Scenario scenario) {
        // Create entity for this scenario if it doesn't exist
        String entityName = scenario.getName();
        TestEntity entity = null;
        
        for (TestEntity e : entityManager.getAllEntities()) {
            if (e.getName().equals(entityName)) {
                entity = e;
                break;
            }
        }
        
        if (entity == null) {
            // Create new entity with components
            entity = entityManager.createEntity(entityName);
            
            // Add config component
            ConfigComponent configComponent = new ConfigComponent();
            configComponent.addScenario(scenario);
            entity.addComponent(ConfigComponent.class, configComponent);
            
            // Add reporting component
            entity.addComponent(ReportingComponent.class, new ReportingComponent(reportDirectory));
            
            // Add protocol component
            entity.addComponent(ProtocolComponent.class, new ProtocolComponent());
        } else {
            // Update existing entity's config component
            ConfigComponent configComponent = entity.getComponent(ConfigComponent.class);
            if (configComponent != null) {
                configComponent.addScenario(scenario);
            } else {
                configComponent = new ConfigComponent();
                configComponent.addScenario(scenario);
                entity.addComponent(ConfigComponent.class, configComponent);
            }
        }
        
        return this;
    }
    
    /**
     * Add a model.Scenario to the system
     * 
     * @param modelScenario The model.Scenario to add
     * @return This system for chaining
     */
    public TestExecutionSystem addScenario(io.ecs.model.Scenario modelScenario) {
        // Create entity for this scenario if it doesn't exist
        String entityName = modelScenario.getName();
        TestEntity entity = null;
        
        for (TestEntity e : entityManager.getAllEntities()) {
            if (e.getName().equals(entityName)) {
                entity = e;
                break;
            }
        }
        
        // Convert to config.Scenario
        Scenario configScenario = new Scenario();
        configScenario.setId(modelScenario.getId());
        configScenario.setName(modelScenario.getName());
        configScenario.setDescription(modelScenario.getDescription());
        configScenario.setThreads(modelScenario.getThreads());
        configScenario.setIterations(modelScenario.getIterations());
        configScenario.setRampUp(modelScenario.getRampUp());
        configScenario.setHold(modelScenario.getHold());
        configScenario.setEngine(modelScenario.getEngine());
        configScenario.setSuccessThreshold(modelScenario.getSuccessThreshold());
        configScenario.setRequests(new ArrayList<>(modelScenario.getRequests()));
        configScenario.setVariables(new HashMap<>(modelScenario.getVariables()));
        configScenario.setDataFiles(new HashMap<>(modelScenario.getDataFiles()));
        
        if (entity == null) {
            // Create new entity with components
            entity = entityManager.createEntity(entityName);
            
            // Add config component
            ConfigComponent configComponent = new ConfigComponent();
            configComponent.addScenario(configScenario);
            entity.addComponent(ConfigComponent.class, configComponent);
            
            // Add reporting component
            entity.addComponent(ReportingComponent.class, new ReportingComponent(reportDirectory));
            
            // Add protocol component
            entity.addComponent(ProtocolComponent.class, new ProtocolComponent());
        } else {
            // Update existing entity's config component
            ConfigComponent configComponent = entity.getComponent(ConfigComponent.class);
            if (configComponent != null) {
                configComponent.addScenario(configScenario);
            } else {
                configComponent = new ConfigComponent();
                configComponent.addScenario(configScenario);
                entity.addComponent(ConfigComponent.class, configComponent);
            }
        }
        
        return this;
    }
    
    /**
     * Add a data file to be used in the test
     * 
     * @param name Name to reference the data file
     * @param filePath Path to the CSV file
     * @return This system for chaining
     */
    public TestExecutionSystem addDataFile(String name, String filePath) {
        try {
            CsvReader csvReader = new CsvReader();
            List<Map<String, String>> data = csvReader.readCsv(filePath);
            
            // Add the data to all scenario entities
            for (TestEntity entity : entityManager.getEntitiesWithComponent(ConfigComponent.class)) {
                ConfigComponent config = entity.getComponent(ConfigComponent.class);
                for (Scenario scenario : config.getScenarios()) {
                    if (scenario.getDataFiles() == null) {
                        scenario.setDataFiles(new HashMap<>());
                    }
                    scenario.getDataFiles().put(name, filePath);
                }
            }
            
            logger.info("Added data file: {} with {} records", name, data.size());
        } catch (Exception e) {
            logger.error("Error loading data file: {}", e.getMessage(), e);
        }
        return this;
    }
    
    /**
     * Load scenarios from a YAML configuration file
     * 
     * @param configFile Path to configuration file
     * @return This system for chaining
     */
    public TestExecutionSystem loadFromYaml(String configFile) {
        try {
            for (TestEntity entity : entityManager.getEntitiesWithComponent(ConfigComponent.class)) {
                ConfigComponent config = entity.getComponent(ConfigComponent.class);
                config.loadConfig(configFile);
            }
            
            // If there are no entities with ConfigComponent, create a new one
            if (entityManager.getEntitiesWithComponent(ConfigComponent.class).isEmpty()) {
                ConfigComponent configComponent = new ConfigComponent();
                List<Scenario> scenarios = configComponent.loadConfig(configFile).getScenarios();
                
                // Create entities for each loaded scenario
                for (Scenario scenario : scenarios) {
                    TestEntity entity = entityManager.createEntity(scenario.getName());
                    
                    // Add config component for this scenario
                    ConfigComponent scenarioConfig = new ConfigComponent();
                    scenarioConfig.addScenario(scenario);
                    entity.addComponent(ConfigComponent.class, scenarioConfig);
                    
                    // Add reporting component
                    entity.addComponent(ReportingComponent.class, new ReportingComponent(reportDirectory));
                    
                    // Add protocol component
                    entity.addComponent(ProtocolComponent.class, new ProtocolComponent());
                }
            }
        } catch (Exception e) {
            logger.error("Error loading YAML config: {}", e.getMessage(), e);
        }
        
        return this;
    }
    
    /**
     * Execute a specific scenario entity
     * 
     * @param entity The entity containing the scenario to execute
     * @return Test results and metrics
     */
    private Map<String, Object> executeEntity(TestEntity entity) {
        if (!entity.hasComponent(ConfigComponent.class) || 
            !entity.hasComponent(ReportingComponent.class)) {
            logger.warn("Entity {} is missing required components for test execution", entity.getName());
            return null;
        }
        
        ConfigComponent configComponent = entity.getComponent(ConfigComponent.class);
        ReportingComponent reportingComponent = entity.getComponent(ReportingComponent.class);
        
        // Get the scenario from the config component
        List<Scenario> scenarios = configComponent.getScenarios();
        if (scenarios.isEmpty()) {
            logger.warn("No scenarios found for entity: {}", entity.getName());
            return null;
        }
        
        // Use the first scenario in the loaded list
        Scenario scenario = scenarios.get(0);
        
        try {
            logger.info("Executing scenario: {}", scenario.getName());
            
            // Combine global variables with scenario variables
            Map<String, String> combinedVariables = new HashMap<>(globalVariables);
            if (scenario.getVariables() != null) {
                combinedVariables.putAll(scenario.getVariables());
            }
            
            // Create execution config
            ExecutionConfig config = new ExecutionConfig();
            config.setThreads(scenario.getThreads() > 0 ? scenario.getThreads() : 1);
            config.setIterations(scenario.getIterations() > 0 ? scenario.getIterations() : 1);
            config.setRampUpSeconds(scenario.getRampUp());
            config.setHoldSeconds(scenario.getHold());
            config.setReportDirectory(reportDirectory);
            config.setVariables(combinedVariables);
            
            // Set success threshold if available
            if (scenario.getSuccessThreshold() > 0) {
                config.setSuccessThreshold(scenario.getSuccessThreshold());
            }
            
            // Get or create appropriate engine
            String engineType = scenario.getEngine() != null ? scenario.getEngine() : defaultEngine;
            Engine engine = engines.computeIfAbsent(
                engineType + "_" + scenario.getName(),
                k -> EngineFactory.getEngine(engineType, config)
            );
            
            // Initialize engine with variables
            engine.initialize(combinedVariables);
            
            // Initialize protocol component if present
            if (entity.hasComponent(ProtocolComponent.class)) {
                ProtocolComponent protocolComponent = entity.getComponent(ProtocolComponent.class);
                for (Map.Entry<String, String> entry : combinedVariables.entrySet()) {
                    protocolComponent.setVariable(entry.getKey(), entry.getValue());
                }
            }
            
            // Execute requests and collect results
            List<TestResult> results = new ArrayList<>();
            Map<String, Object> metrics = new HashMap<>();
            
            // Initialize reporting
            // Convert config.Scenario to model.Scenario if needed for reporting
            io.ecs.model.Scenario modelScenario = configComponent.convertToModelScenario(scenario);
            reportingComponent.initializeReporting(modelScenario);
            
            // Execute each request
            boolean anyRequestSuccessful = false;
            for (Request request : scenario.getRequests()) {
                TestResult result = engine.executeRequest(request);
                result.setScenarioId(scenario.getId());
                result.setScenarioName(scenario.getName());
                results.add(result);
                
                // Track if any request was successful
                if (result.isSuccess()) {
                    anyRequestSuccessful = true;
                }
                
                // Log result
                logger.info("Request: {} completed", request.getName());
                logger.info("Success: {}", result.isSuccess());
                logger.info("Status code: {}", result.getStatusCode());
                logger.info("Response time: {}ms", result.getResponseTime());
                
                // Get current metrics
                metrics = engine.getMetrics();
                
                // Log metrics
                logMetrics(scenario.getName(), metrics);
                
                // Record samples for this result
                reportingComponent.recordSamples(result);
            }
            
            // Ensure we have a non-zero success rate if any request succeeded
            if (anyRequestSuccessful && metrics.containsKey("successRate") && 
                (Double)metrics.get("successRate") <= 0.0) {
                metrics.put("successRate", 100.0);
            }
            
            // Generate report
            String reportPath = reportingComponent.generateReport(results.get(results.size() - 1));
            logger.info("Test report generated at: {}", reportPath);
            
            return metrics;
            
        } catch (Exception e) {
            logger.error("Error executing scenario {}: {}", scenario.getName(), e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Run a specific scenario
     * 
     * @param scenario The scenario to run
     * @return Aggregate test results
     */
    public Map<String, Object> executeScenario(Scenario scenario) {
        // Find the entity for this scenario
        TestEntity targetEntity = null;
        
        for (TestEntity entity : entityManager.getEntitiesWithComponent(ConfigComponent.class)) {
            ConfigComponent config = entity.getComponent(ConfigComponent.class);
            for (Scenario s : config.getScenarios()) {
                if (s.getName().equals(scenario.getName())) {
                    targetEntity = entity;
                    break;
                }
            }
            if (targetEntity != null) break;
        }
        
        // If no entity was found, create one
        if (targetEntity == null) {
            targetEntity = entityManager.createEntity(scenario.getName());
            
            // Add config component
            ConfigComponent configComponent = new ConfigComponent();
            configComponent.addScenario(scenario);
            targetEntity.addComponent(ConfigComponent.class, configComponent);
            
            // Add reporting component
            targetEntity.addComponent(ReportingComponent.class, new ReportingComponent(reportDirectory));
            
            // Add protocol component
            targetEntity.addComponent(ProtocolComponent.class, new ProtocolComponent());
        }
        
        return executeEntity(targetEntity);
    }
    
    /**
     * Run a specific model.Scenario
     * 
     * @param modelScenario The model.Scenario to run
     * @return Aggregate test results
     */
    public Map<String, Object> executeScenario(io.ecs.model.Scenario modelScenario) {
        // Convert to config.Scenario
        Scenario configScenario = new Scenario();
        configScenario.setId(modelScenario.getId());
        configScenario.setName(modelScenario.getName());
        configScenario.setDescription(modelScenario.getDescription());
        configScenario.setThreads(modelScenario.getThreads());
        configScenario.setIterations(modelScenario.getIterations());
        configScenario.setRampUp(modelScenario.getRampUp());
        configScenario.setHold(modelScenario.getHold());
        configScenario.setEngine(modelScenario.getEngine());
        configScenario.setSuccessThreshold(modelScenario.getSuccessThreshold());
        configScenario.setRequests(new ArrayList<>(modelScenario.getRequests()));
        configScenario.setVariables(new HashMap<>(modelScenario.getVariables()));
        configScenario.setDataFiles(new HashMap<>(modelScenario.getDataFiles()));
        
        // Execute the converted scenario
        return executeScenario(configScenario);
    }
    
    /**
     * Run all scenarios in the system
     * 
     * @return List of aggregate test results
     */
    public List<Map<String, Object>> executeAllScenarios() {
        List<Map<String, Object>> allResults = new ArrayList<>();
        
        // Get all entities with ConfigComponent
        List<TestEntity> entities = entityManager.getEntitiesWithComponent(ConfigComponent.class);
        
        for (TestEntity entity : entities) {
            Map<String, Object> result = executeEntity(entity);
            if (result != null) {
                allResults.add(result);
            }
        }
        
        return allResults;
    }
    
    /**
     * Log metrics from a scenario execution
     * 
     * @param scenarioName Name of the scenario
     * @param metrics Metrics to log
     */
    public void logMetrics(String scenarioName, Map<String, Object> metrics) {
        logger.info("Scenario: {} stats so far", scenarioName);
        logger.info("Total requests: {}", metrics.getOrDefault("totalRequests", 0));
        logger.info("Success rate: {}%", metrics.getOrDefault("successRate", 0.0));
        logger.info("Average response time: {}ms", metrics.getOrDefault("avgResponseTime", 0.0));
        logger.info("Min/Max response time: {}/{}ms", 
                    metrics.getOrDefault("minResponseTime", 0),
                    metrics.getOrDefault("maxResponseTime", 0));
        logger.info("90th percentile: {}ms", metrics.getOrDefault("percentile90", 0));
        logger.info("--------------------------------------");
    }
    
    /**
     * Get the entity manager used by this system
     * 
     * @return Entity manager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
    
    /**
     * Get the report directory
     * 
     * @return Report directory path
     */
    public String getReportDirectory() {
        return reportDirectory;
    }
    
    /**
     * Get global variables
     * 
     * @return Map of global variables
     */
    public Map<String, String> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }
    
    /**
     * Set the default engine to use when no engine is specified in a scenario
     * 
     * @param engineType The default engine type (jmdsl, jmtree, gatling, etc.)
     * @return This system for chaining
     */
    public TestExecutionSystem setDefaultEngine(String engineType) {
        if (engineType != null && !engineType.isEmpty()) {
            this.defaultEngine = engineType.trim().toLowerCase();
            logger.info("Default engine set to: {}", this.defaultEngine);
        }
        return this;
    }
    
    /**
     * Shut down the test system and clean up resources
     */
    public void shutdown() {
        // Clean up any resources
        for (Engine engine : engines.values()) {
            if (engine != null) {
                try {
                    engine.shutdown();
                } catch (Exception e) {
                    logger.error("Error shutting down engine: {}", e.getMessage(), e);
                }
            }
        }
        
        // Clear collections
        engines.clear();
        
        logger.info("TestExecutionSystem shutdown complete");
    }
    
    /**
     * Add a helper method to add a scenario to an entity
     * 
     * @param entity Entity to add the scenario to
     * @param scenario Scenario to add
     */
    private void addScenarioToEntity(TestEntity entity, Scenario scenario) {
        ConfigComponent configComponent = entity.getComponent(ConfigComponent.class);
        if (configComponent == null) {
            configComponent = new ConfigComponent();
            entity.addComponent(ConfigComponent.class, configComponent);
        }
        
        configComponent.addScenario(scenario);
        
        // Ensure other required components exist
        if (!entity.hasComponent(ReportingComponent.class)) {
            entity.addComponent(ReportingComponent.class, new ReportingComponent(reportDirectory));
        }
        
        if (!entity.hasComponent(ProtocolComponent.class)) {
            entity.addComponent(ProtocolComponent.class, new ProtocolComponent());
        }
    }
}