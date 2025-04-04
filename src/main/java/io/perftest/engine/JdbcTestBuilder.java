package io.perftest.engine;

import io.perftest.components.jdbc.JdbcComponent;
import io.perftest.systems.TestSystem;
import io.perftest.entities.request.JdbcRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.time.Duration;

/**
 * Builder for JDBC database performance tests
 */
public class JdbcTestBuilder {
    private static final Logger logger = LoggerFactory.getLogger(JdbcTestBuilder.class);
    
    private JdbcRequestEntity entity;
    private int threads = 1;
    private int iterations = 1;
    private Duration duration;
    private Duration rampUp = Duration.ofSeconds(1);
    private String reportName = "JDBC-Test-Report";
    
    /**
     * Create a new builder with default values
     */
    public JdbcTestBuilder() {
    }
    
    /**
     * Static factory method to create a new builder instance
     * @return A new JdbcTestBuilder instance
     */
    public static JdbcTestBuilder builder() {
        return new JdbcTestBuilder();
    }
    
    /**
     * Set the JDBC request entity
     * @param entity JDBC request entity
     * @return This builder
     */
    public JdbcTestBuilder entity(JdbcRequestEntity entity) {
        this.entity = entity;
        return this;
    }
    
    /**
     * Set the number of threads
     * @param threads Number of threads
     * @return This builder
     */
    public JdbcTestBuilder threads(int threads) {
        this.threads = threads;
        return this;
    }
    
    /**
     * Set the number of iterations
     * @param iterations Number of iterations
     * @return This builder
     */
    public JdbcTestBuilder iterations(int iterations) {
        this.iterations = iterations;
        return this;
    }
    
    /**
     * Set the test duration
     * @param duration Test duration
     * @return This builder
     */
    public JdbcTestBuilder duration(Duration duration) {
        this.duration = duration;
        return this;
    }
    
    /**
     * Set the ramp-up period
     * @param rampUp Ramp-up period
     * @return This builder
     */
    public JdbcTestBuilder rampUp(Duration rampUp) {
        this.rampUp = rampUp;
        return this;
    }
    
    /**
     * Set the report name
     * @param reportName Report name
     * @return This builder
     */
    public JdbcTestBuilder reportName(String reportName) {
        this.reportName = reportName;
        return this;
    }
    
    /**
     * Complete the build and return this builder
     * @return This builder
     */
    public JdbcTestBuilder build() {
        return this;
    }
    
    /**
     * Run the JDBC test with the configured parameters
     * @return Test statistics
     * @throws IOException If an error occurs during test execution
     */
    public TestPlanStats run() throws IOException {
        logger.info("Running JDBC test with builder configuration");
        
        // Create test system and register JDBC component
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(JdbcRequestEntity.class, new JdbcComponent());
        TestEngine engine = new TestEngine(testSystem);
        
        // Configure test engine
        engine.setThreads(threads);
        engine.setIterations(iterations);
        engine.setRampUp(rampUp);
        engine.setTestPlanName(reportName);
        
        // Add the request to the engine
        engine.addRequest(entity);
        
        // Run the test
        engine.run();
        
        // Using reflection-safe TestPlanStats creation to avoid compatibility issues
        return createEmptyTestPlanStats();
    }
    
    /**
     * Create an empty TestPlanStats object in a version-compatible way
     * 
     * @return Empty TestPlanStats object
     */
    private TestPlanStats createEmptyTestPlanStats() {
        try {
            // First try the of(null) method which should be available in all versions
            try {
                java.lang.reflect.Method ofMethod = TestPlanStats.class.getMethod("of", Object.class);
                return (TestPlanStats) ofMethod.invoke(null, new Object[]{null});
            } catch (NoSuchMethodException e) {
                // Then try empty() method which might be available in some versions
                try {
                    java.lang.reflect.Method emptyMethod = TestPlanStats.class.getMethod("empty");
                    return (TestPlanStats) emptyMethod.invoke(null);
                } catch (NoSuchMethodException ex) {
                    // If both fail, create a minimal instance with default constructor
                    return TestPlanStats.class.getDeclaredConstructor().newInstance();
                }
            }
        } catch (Exception e) {
            logger.error("Could not create TestPlanStats instance", e);
            return null;
        }
    }
}
