package io.perftest.core;

import io.perftest.model.Request;
import io.perftest.model.Response;
import io.perftest.protocol.Protocol;
import io.perftest.report.MetricsCollector;
import io.perftest.util.SchemaValidator;
import io.perftest.util.TemplateProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Represents a single performance test execution unit
 */
public class PerformanceTest implements Callable<TestResult> {
    private final Protocol protocol;
    private final Request request;
    private final Map<String, String> variables;
    private final MetricsCollector metricsCollector;
    private final SchemaValidator schemaValidator;
    
    public PerformanceTest(Protocol protocol, Request request, Map<String, String> variables) {
        this.protocol = protocol;
        this.request = request;
        this.variables = variables != null ? variables : new HashMap<>();
        this.metricsCollector = new MetricsCollector();
        this.schemaValidator = new SchemaValidator();
    }
    
    @Override
    public TestResult call() {
        TestResult result = new TestResult();
        result.setRequestName(request.getName());
        result.setStartTime(System.currentTimeMillis());
        
        try {
            // Process templates with variables
            TemplateProcessor templateProcessor = new TemplateProcessor();
            
            String processedBody = null;
            if (request.getBodyTemplate() != null) {
                processedBody = templateProcessor.process(request.getBodyTemplate(), variables);
            }
            
            Map<String, String> processedHeaders = null;
            if (request.getHeadersTemplate() != null) {
                String headersJson = templateProcessor.process(request.getHeadersTemplate(), variables);
                processedHeaders = templateProcessor.parseJsonToMap(headersJson);
            }
            
            Map<String, String> processedParams = null;
            if (request.getParamsTemplate() != null) {
                processedParams = templateProcessor.processParamsTemplate(request.getParamsTemplate(), variables);
            }
            
            // Execute request
            long startNanos = System.nanoTime();
            Response response = protocol.execute(
                    request.getEndpoint(),
                    request.getMethod(),
                    processedBody,
                    processedHeaders,
                    processedParams
            );
            long endNanos = System.nanoTime();
            
            // Collect metrics
            long responseTimeMs = (endNanos - startNanos) / 1_000_000;
            metricsCollector.recordResponseTime(responseTimeMs);
            
            result.setResponseTime(responseTimeMs);
            result.setResponse(response);
            
            // Validate response against schema if validators exist
            boolean validationPassed = false;
            
            if (request.getResponseValidators() != null && !request.getResponseValidators().isEmpty()) {
                for (Map.Entry<String, String> validator : request.getResponseValidators().entrySet()) {
                    String status = validator.getKey();
                    String schemaPath = validator.getValue();
                    
                    if (schemaValidator.validate(response.getBody(), schemaPath)) {
                        result.setStatus(status);
                        validationPassed = true;
                        break;
                    }
                }
            } else {
                // No validators, so pass if response code is 2xx
                validationPassed = response.getStatusCode() >= 200 && response.getStatusCode() < 300;
            }
            
            result.setSuccess(validationPassed);
            if (!validationPassed && result.getStatus() == null) {
                result.setStatus("Failed");
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setStatus("Error");
            result.setErrorMessage(e.getMessage());
            metricsCollector.recordError();
        } finally {
            result.setEndTime(System.currentTimeMillis());
        }
        
        return result;
    }
}
