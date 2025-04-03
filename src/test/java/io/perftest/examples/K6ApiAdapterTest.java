package io.perftest.examples;

import io.perftest.adapters.TestEngineAdapter;
import io.perftest.entities.request.RequestEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;



/**
 * Test for K6 API using the adapter pattern
 */
public class K6ApiAdapterTest {
    private static final Logger logger = LoggerFactory.getLogger(K6ApiAdapterTest.class);
    
    @Test
    public void testGetCrocodileById() throws Exception {
        // Set up test parameters
        int threads = 1;
        int iterations = 1;
        
        // Create test engine adapter
        TestEngineAdapter adapter = new TestEngineAdapter();
        
        // Create request entity
        RequestEntity requestEntity = new RequestEntity("https://test-api.k6.io/public/crocodiles/1/");
        requestEntity.setMethod("GET");
        requestEntity.addAssertion("$.id", "1");
        requestEntity.addAssertion("$.name", "*");
        
        // Execute test using adapter
        TestPlanStats stats = adapter.executeHttpTest(requestEntity, threads, iterations);
        
        // Get and log the average response time
        logger.info("Test completed with average response time: {}ms", stats.overall().sampleTime().mean());
        
        // Get and log the error count
        long errorCount = stats.overall().errorsCount();
        logger.info("Error count: {}", errorCount);
        
        // Assert that there are no errors
        assert errorCount == 0 : "Expected no errors but got " + errorCount;
    }
    
    @Test
    public void testGetAllCrocodiles() throws Exception {
        // Set up test parameters
        int threads = 1;
        int iterations = 1;
        
        // Create test engine adapter
        TestEngineAdapter adapter = new TestEngineAdapter();
        
        // Create request entity
        RequestEntity requestEntity = new RequestEntity("https://test-api.k6.io/public/crocodiles/");
        requestEntity.setMethod("GET");
        requestEntity.addAssertion("$[0].id", "*");
        requestEntity.addAssertion("$[0].name", "*");
        
        // Execute test using adapter
        TestPlanStats stats = adapter.executeHttpTest(requestEntity, threads, iterations);
        
        // Get and log the error count
        long errorCount = stats.overall().errorsCount();
        logger.info("Error count: {}", errorCount);
        
        // Assert that there are no errors
        assert errorCount == 0 : "Expected no errors but got " + errorCount;
    }
}