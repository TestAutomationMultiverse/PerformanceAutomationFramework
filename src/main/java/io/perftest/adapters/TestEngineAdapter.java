package io.perftest.adapters;

import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.perftest.components.http.HttpComponent;
import io.perftest.engine.TestEngine;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.RequestEntity;
import io.perftest.systems.TestSystem;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

/**
 * Adapter for TestEngine to handle RequestEntity to HttpRequestEntity conversion
 */
public class TestEngineAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TestEngineAdapter.class);
    private final TestEngine testEngine;
    private final HttpComponent httpComponent;

    /**
     * Creates a new TestEngineAdapter
     */
    public TestEngineAdapter() {
        TestSystem testSystem = new TestSystem();
        testSystem.addComponent(HttpRequestEntity.class, new HttpComponent());
        this.testEngine = new TestEngine(testSystem);
        this.httpComponent = new HttpComponent();
    }

    /**
     * Execute HTTP test with generic RequestEntity
     * 
     * @param requestEntity Generic request entity
     * @param threads Number of threads
     * @param iterations Number of iterations
     * @return Test statistics
     * @throws IOException If an error occurs
     */
    public TestPlanStats executeHttpTest(RequestEntity requestEntity, int threads, int iterations)
            throws IOException {
        // Convert to HttpRequestEntity
        HttpRequestEntity httpRequestEntity = convertToHttpRequestEntity(requestEntity);

        logger.info("Executing HTTP test through adapter for URL: {}", httpRequestEntity.getUrl());

        // Configure the test engine
        testEngine.setThreads(threads);
        testEngine.setIterations(iterations);
        testEngine.setRampUp(Duration.ofSeconds(1));

        // Add the request to the engine
        testEngine.addRequest(httpRequestEntity);

        // Run the test
        return testEngine.run();
    }

    /**
     * Convert generic RequestEntity to HttpRequestEntity
     * 
     * @param requestEntity Generic request entity
     * @return Converted HTTP request entity
     */
    private HttpRequestEntity convertToHttpRequestEntity(RequestEntity requestEntity) {
        HttpRequestEntity httpRequestEntity = new HttpRequestEntity();

        // Copy properties
        httpRequestEntity.setUrl(requestEntity.getUrl());
        httpRequestEntity.setMethod(requestEntity.getMethod());
        httpRequestEntity.setHeaders(requestEntity.getHeaders());
        httpRequestEntity.setConnectTimeout(requestEntity.getConnectTimeout());
        httpRequestEntity.setResponseTimeout(requestEntity.getResponseTimeout());
        httpRequestEntity.setAssertions(requestEntity.getAssertions());

        return httpRequestEntity;
    }
}
