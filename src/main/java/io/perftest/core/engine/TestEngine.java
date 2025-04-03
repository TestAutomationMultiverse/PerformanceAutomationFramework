package io.perftest.core.engine;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import io.perftest.components.graphql.GraphQLComponent;
import us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement.Scope;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion.TargetField;
import io.perftest.components.http.HttpComponent;
import io.perftest.components.xml.XmlComponent;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.RequestEntity;
import io.perftest.entities.request.SoapRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslThreadGroup;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Engine for executing performance tests
 */
public class TestEngine {
    private static final Logger logger = LoggerFactory.getLogger(TestEngine.class);
    
    private final HttpComponent httpComponent;
    private final GraphQLComponent graphQLComponent;
    private final XmlComponent xmlComponent;
    
    /**
     * Creates a new test engine instance
     */
    public TestEngine() {
        this.httpComponent = new HttpComponent();
        this.graphQLComponent = new GraphQLComponent();
        this.xmlComponent = new XmlComponent();
    }
    
    /**
     * Execute an HTTP test
     * @param requestEntity Request entity
     * @param threads Number of threads
     * @param iterations Number of iterations per thread
     * @return Test statistics
     * @throws IOException If an error occurs during test execution
     */
    public TestPlanStats executeHttpTest(RequestEntity requestEntity, int threads, int iterations) throws IOException {
        logger.info("Executing HTTP test for URL: {} with {} threads and {} iterations",
            requestEntity.getUrl(), threads, iterations);
        
        // Create HTTP sampler - Handle the generic RequestEntity by using appropriate sampler
        DslHttpSampler httpSampler;
        if (requestEntity instanceof io.perftest.entities.request.HttpRequestEntity) {
            httpSampler = httpComponent.createHttpSampler((io.perftest.entities.request.HttpRequestEntity) requestEntity);
        } else {
            // For non-specific RequestEntity, create a basic HttpRequestEntity
            io.perftest.entities.request.HttpRequestEntity httpRequestEntity = 
                new io.perftest.entities.request.HttpRequestEntity(requestEntity.getUrl());
            httpRequestEntity.setMethod(requestEntity.getMethod());
            httpRequestEntity.setHeaders(requestEntity.getHeaders());
            httpRequestEntity.setAssertions(requestEntity.getAssertions());
            httpRequestEntity.setConnectTimeout(requestEntity.getConnectTimeout());
            httpRequestEntity.setResponseTimeout(requestEntity.getResponseTimeout());
            
            httpSampler = httpComponent.createHttpSampler(httpRequestEntity);
        }
        
        // Add JSON assertions if defined
        if (!requestEntity.getAssertions().isEmpty()) {
            for (Map.Entry<String, String> assertion : requestEntity.getAssertions().entrySet()) {
                // Assertions for JSON properties
                String jsonPath = assertion.getKey();
                String expectedValue = assertion.getValue();
                
                if ("*".equals(expectedValue)) {
                    // Assert that the field exists but don't check value
                    httpSampler = httpSampler.children(
                        jsonAssertion(jsonPath)
                    );
                } else {
                    // Assert that the field has the specified value
                    httpSampler = httpSampler.children(
                        jsonAssertion(jsonPath)
                            .equalsTo(expectedValue)
                    );
                }
            }
        }
        
        // Add response status assertion (checking the last sampler in case of redirects)
        httpSampler = httpSampler.children(
            responseAssertion()
                .containsSubstrings(String.valueOf(requestEntity.getExpectedStatus()))
                .scope(Scope.ALL_SAMPLES)
                .fieldToTest(TargetField.RESPONSE_CODE)
                .ignoreStatus()
        );
        
        // Create thread group
        DslThreadGroup threadGroup = threadGroup(threads, iterations, httpSampler);
        
        // Create and run test plan with HTML reports
        return testPlan(
            threadGroup,
            jtlWriter("target/http-test-results.jtl"),
            htmlReporter("target/html-reports/http")
        ).run();
    }
    
    /**
     * Execute a GraphQL test
     * @param requestEntity GraphQL request entity
     * @param threads Number of threads
     * @param iterations Number of iterations per thread
     * @return Test statistics
     * @throws IOException If an error occurs during test execution
     */
    public TestPlanStats executeGraphQLTest(GraphQLRequestEntity requestEntity, int threads, int iterations) 
            throws IOException {
        logger.info("Executing GraphQL test for URL: {} with {} threads and {} iterations",
            requestEntity.getUrl(), threads, iterations);
        
        // Create GraphQL sampler
        DslHttpSampler graphQLSampler = graphQLComponent.createGraphQLSampler(requestEntity);
        
        // Add JSON assertions if defined
        if (!requestEntity.getAssertions().isEmpty()) {
            for (Map.Entry<String, String> assertion : requestEntity.getAssertions().entrySet()) {
                // Assertions for GraphQL JSON responses
                String jsonPath = assertion.getKey();
                String expectedValue = assertion.getValue();
                
                if ("*".equals(expectedValue)) {
                    // Assert that the field exists but don't check value
                    graphQLSampler = graphQLSampler.children(
                        jsonAssertion(jsonPath)
                    );
                } else {
                    // Assert that the field has the specified value
                    graphQLSampler = graphQLSampler.children(
                        jsonAssertion(jsonPath)
                            .equalsTo(expectedValue)
                    );
                }
            }
        }
        
        // Add response status assertion (checking the last sampler in case of redirects)
        graphQLSampler = graphQLSampler.children(
            responseAssertion()
                .containsSubstrings(String.valueOf(requestEntity.getExpectedStatus()))
                .scope(Scope.ALL_SAMPLES)
                .fieldToTest(TargetField.RESPONSE_CODE)
                .ignoreStatus()
        );
        
        // Create thread group
        DslThreadGroup threadGroup = threadGroup(threads, iterations, graphQLSampler);
        
        // Create and run test plan with HTML reports
        return testPlan(
            threadGroup,
            jtlWriter("target/graphql-test-results.jtl"),
            htmlReporter("target/html-reports/graphql")
        ).run();
    }
    
    /**
     * Execute a SOAP test
     * @param requestEntity SOAP request entity
     * @param threads Number of threads
     * @param iterations Number of iterations per thread
     * @return Test statistics
     * @throws IOException If an error occurs during test execution
     */
    public TestPlanStats executeSoapTest(SoapRequestEntity requestEntity, int threads, int iterations)
            throws IOException {
        logger.info("Executing SOAP test for URL: {} with {} threads and {} iterations",
            requestEntity.getUrl(), threads, iterations);
        
        // Create SOAP sampler
        DslHttpSampler soapSampler = xmlComponent.createSoapSampler(requestEntity);
        
        List<Object> children = new ArrayList<>();
        
        // Add XML/XPath assertions if defined
        for (Map.Entry<String, String> assertion : requestEntity.getAssertions().entrySet()) {
            String path = assertion.getKey();
            String expectedValue = assertion.getValue();
            
            logger.info("Adding assertion for path: {} with expected value: {}", path, expectedValue);
            
            if (path.startsWith("/")) {
                // XPath assertion for XML responses
                if ("*".equals(expectedValue)) {
                    // Just check existence with response assertion on XML document
                    logger.info("Adding XPath existence assertion for: {}", path);
                    children.add(
                        responseAssertion()
                            .containsSubstrings(path)
                            .fieldToTest(TargetField.RESPONSE_BODY)
                    );
                } else {
                    // Check specific value with response assertion on XML document
                    logger.info("Adding XPath value assertion for: {} = {}", path, expectedValue);
                    children.add(
                        responseAssertion()
                            .containsSubstrings(expectedValue)
                            .fieldToTest(TargetField.RESPONSE_BODY)
                    );
                }
            } else {
                // JSON assertion for JSON responses
                if ("*".equals(expectedValue)) {
                    // Assert that the field exists but don't check value
                    logger.info("Adding JSON existence assertion for: {}", path);
                    children.add(
                        jsonAssertion(path)
                    );
                } else {
                    // Assert that the field has the specified value
                    logger.info("Adding JSON value assertion for: {} = {}", path, expectedValue);
                    children.add(
                        jsonAssertion(path)
                            .equalsTo(expectedValue)
                    );
                }
            }
        }
        
        // Add response status assertion (checking the last sampler in case of redirects)
        children.add(
            responseAssertion()
                .containsSubstrings(String.valueOf(requestEntity.getExpectedStatus()))
                .scope(Scope.ALL_SAMPLES)
                .fieldToTest(TargetField.RESPONSE_CODE)
                .ignoreStatus()
        );
        
        // Add all children to sampler
        if (!children.isEmpty()) {
            // Need to add each child separately to the sampler
            for (Object child : children) {
                if (child instanceof us.abstracta.jmeter.javadsl.core.samplers.BaseSampler.SamplerChild) {
                    soapSampler = soapSampler.children((us.abstracta.jmeter.javadsl.core.samplers.BaseSampler.SamplerChild) child);
                }
            }
        }
        
        // Create thread group
        DslThreadGroup threadGroup = threadGroup(threads, iterations, soapSampler);
        
        // Create and run test plan with HTML reports
        return testPlan(
            threadGroup,
            jtlWriter("target/soap-test-results.jtl"),
            htmlReporter("target/html-reports/soap")
        ).run();
    }
    
    /**
     * Creates a basic load test configuration
     * @param duration Test duration
     * @param rampUp Ramp-up period
     * @param threads Number of threads
     * @param sampler HTTP sampler to execute
     * @return Test statistics
     * @throws IOException If an error occurs during test execution
     */
    public TestPlanStats executeLoadTest(Duration duration, Duration rampUp, int threads, DslHttpSampler sampler)
            throws IOException {
        logger.info("Executing load test with {} threads for {} duration and {} ramp-up",
            threads, duration, rampUp);
        
        // Create thread group with duration
        DslThreadGroup threadGroup = threadGroup()
            .rampTo(threads, Duration.parse("PT" + rampUp.getSeconds() + "S"))
            .holdIterating(duration.getSeconds() + "s")
            .children(sampler);
        
        // Create and run test plan with HTML reports
        return testPlan(
            threadGroup,
            jtlWriter("target/load-test-results.jtl"),
            htmlReporter("target/html-reports/load")
        ).run();
    }
}