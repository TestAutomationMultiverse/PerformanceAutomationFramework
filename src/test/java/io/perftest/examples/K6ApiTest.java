package io.perftest.examples;

import io.perftest.core.engine.TestEngine;
import io.perftest.entities.request.HttpRequestEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;

/**
 * Test class for K6 API using the TestEngineAdapter with config-driven URLs
 */
public class K6ApiTest {
    private static final Logger logger = LoggerFactory.getLogger(K6ApiTest.class);
    private static TestEngine testEngine;
    
    @BeforeAll
    public static void setup() {
        testEngine = new TestEngine();
    }
    
    @Test
    public void testGetCrocodileById() throws IOException {
        // Log test start
        logger.info("Starting test: testGetCrocodileById");
        
        // Create HTTP request entity using the constructor with endpoint and useBaseUrl flag
        HttpRequestEntity requestEntity = new HttpRequestEntity("https://test-api.k6.io/public/crocodiles/1/");
        requestEntity.setMethod("GET");
        requestEntity.setName("Get Crocodile by ID");
        requestEntity.addAssertion("$.id", "1");
        requestEntity.addAssertion("$.name", "*");
        
        // Execute test
        TestPlanStats stats = testEngine.executeHttpTest(requestEntity, 1, 1);
        
        // Log results
        logger.info("Test completed with average time: {}ms", stats.overall().sampleTime().mean());
        logger.info("Error count: {}", stats.overall().errorsCount());
    }
}