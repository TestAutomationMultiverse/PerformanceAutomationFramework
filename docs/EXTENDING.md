# Extending the Java Performance Testing Framework

This guide provides instructions on how to extend the framework with new features.

## Table of Contents

1. [Extending the HTTP Protocol](#extending-the-http-protocol)
2. [Creating a Custom Test Engine](#creating-a-custom-test-engine)
3. [Enhancing Report Generation](#enhancing-report-generation)
4. [Adding Custom Metrics](#adding-custom-metrics)

## Extending the HTTP Protocol

The framework has been consolidated to focus on HTTP/HTTPS protocols. If you need to extend the HTTP protocol functionality, you can extend the existing `HttpProtocol` class:

### Step 1: Extend the HttpProtocol Class

Create a new class that extends the `HttpProtocol` class:

```java
package io.perftest.protocols;

import io.perftest.engine.Protocol;
import io.perftest.model.Response;
import java.util.Map;

public class EnhancedHttpProtocol extends HttpProtocol {
    
    // You can override methods to enhance functionality
    @Override
    public Response execute(String endpoint, String method, String body,
                      Map<String, String> headers, Map<String, String> params,
                      Map<String, String> requestVariables) throws Exception {
        
        // Pre-processing logic
        logRequest(endpoint, method, headers);
        
        // Call the parent implementation
        Response response = super.execute(endpoint, method, body, headers, params, requestVariables);
        
        // Post-processing logic
        logResponse(response);
        enhanceResponse(response);
        
        return response;
    }
    
    private void logRequest(String endpoint, String method, Map<String, String> headers) {
        // Custom request logging implementation
        System.out.println("Enhanced HTTP Request: " + method + " " + endpoint);
    }
    
    private void logResponse(Response response) {
        // Custom response logging implementation
        System.out.println("Enhanced HTTP Response: " + response.getStatusCode());
    }
    
    private void enhanceResponse(Response response) {
        // Add additional metrics or processing to the response
        response.addHeader("X-Enhanced", "true");
    }
}
```

### Step 2: Register Your Enhanced Protocol

You can register your enhanced protocol in your custom engine or test class:

```java
// Create an instance of your enhanced protocol
Protocol enhancedProtocol = new EnhancedHttpProtocol();

// Use it directly
Request request = new Request();
request.setName("Enhanced Request");
request.setMethod("GET");
request.setEndpoint("https://api.example.com/resource");

Map<String, String> variables = new HashMap<>();
TestResult result = enhancedProtocol.execute(request, variables);
```

### Step 3: Use Your Enhanced Protocol

You can use your enhanced protocol within your tests:

```java
// Create test with custom protocol
JMDSLEngine engine = new JMDSLEngine(executionConfig);
engine.setProtocol(new EnhancedHttpProtocol());

// Execute the test with enhanced capabilities
List<TestResult> results = engine.executeScenario("Test Scenario", requests);
```

## Creating a Custom Test Engine

The framework supports different test engines through the `Engine` interface. Here's how to create a custom engine:

### Step 1: Implement the Engine Interface

Create a new class that implements the `Engine` interface:

```java
package io.perftest.engine;

import io.perftest.model.ExecutionConfig;
import io.perftest.model.Request;
import io.perftest.model.TestResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCustomEngine implements Engine {
    
    private ExecutionConfig config;
    private Map<String, String> variables;
    private List<TestResult> results = new ArrayList<>();
    
    public MyCustomEngine(ExecutionConfig config) {
        this.config = config;
    }
    
    @Override
    public void initialize(Map<String, String> variables) {
        this.variables = variables;
        // Perform any engine-specific initialization
    }
    
    @Override
    public TestResult executeRequest(Request request) {
        // Implement your custom request execution logic
        TestResult result = new TestResult();
        result.setRequestName(request.getName());
        result.setStartTime(System.currentTimeMillis());
        
        // Custom implementation logic
        // ...
        
        result.setEndTime(System.currentTimeMillis());
        result.setResponseTime(result.getEndTime() - result.getStartTime());
        
        // Add to results for metrics calculation
        results.add(result);
        
        return result;
    }
    
    @Override
    public List<TestResult> executeScenario(String scenarioName, List<Request> requests) {
        List<TestResult> scenarioResults = new ArrayList<>();
        
        for (Request request : requests) {
            TestResult result = executeRequest(request);
            scenarioResults.add(result);
        }
        
        return scenarioResults;
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Calculate metrics based on results
        int totalRequests = results.size();
        metrics.put("totalRequests", totalRequests);
        
        if (totalRequests > 0) {
            // Success rate
            long successfulRequests = results.stream().filter(TestResult::isSuccess).count();
            double successRate = ((double) successfulRequests / totalRequests) * 100.0;
            metrics.put("successRate", successRate);
            
            // Response times
            double totalResponseTime = results.stream().mapToDouble(TestResult::getResponseTime).sum();
            double avgResponseTime = totalResponseTime / totalRequests;
            metrics.put("avgResponseTime", avgResponseTime);
            
            // Additional metrics
            // ...
        }
        
        return metrics;
    }
    
    @Override
    public void shutdown() {
        // Clean up resources
        // ...
    }
}
```

### Step 2: Register the Engine in the Factory

Update the `EngineFactory` class to include your new engine:

```java
package io.perftest.engine;

import io.perftest.model.ExecutionConfig;

public class EngineFactory {
    
    public static Engine getEngine(String engineType, ExecutionConfig config) {
        if (engineType == null || engineType.isEmpty()) {
            engineType = "jmdsl"; // Default engine
        }
        
        switch (engineType.toLowerCase()) {
            case "jmdsl":
                return new JMDSLEngine(config);
            case "custom":
                return new MyCustomEngine(config);
            default:
                throw new IllegalArgumentException("Unsupported engine type: " + engineType);
        }
    }
}
```

### Step 3: Use the Custom Engine

Now you can use your custom engine in YAML configurations:

```yaml
scenarios:
  - name: Custom Engine Test
    engine: custom
    requests:
      - name: Custom Request
        # Request properties...
```

## Enhancing Report Generation

You can enhance the report generation to include more details or different formats:

### Creating a Custom Report Generator

```java
package io.perftest.report;

import io.perftest.model.Scenario;

import java.util.List;
import java.util.Map;

public class CustomReportGenerator extends ReportGenerator {
    
    @Override
    public void createTestReport(String reportPath, Scenario scenario, List<Map<String, Object>> metrics) {
        // Implement custom report generation
        // You could generate:
        // - PDF reports
        // - CSV data
        // - Interactive HTML with charts
        // - JSON format for API consumption
        
        // Example: Generate a custom HTML report with charts
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='en'>\n");
        html.append("<head>\n");
        html.append("  <meta charset='UTF-8'>\n");
        html.append("  <title>Custom Performance Test Report</title>\n");
        html.append("  <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>\n");
        html.append("  <style>/* Your custom CSS */</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Add custom report content
        html.append("  <h1>Performance Test Report: ").append(scenario.getName()).append("</h1>\n");
        
        // Add metrics tables, charts, etc.
        
        html.append("</body>\n");
        html.append("</html>");
        
        // Write to file
        try {
            java.nio.file.Files.write(java.nio.file.Paths.get(reportPath), html.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## Adding Custom Metrics

You can add custom metrics calculation in your engine:

```java
@Override
public Map<String, Object> getMetrics() {
    Map<String, Object> metrics = new HashMap<>();
    
    // Basic metrics
    int totalRequests = results.size();
    metrics.put("totalRequests", totalRequests);
    
    if (totalRequests > 0) {
        // Success rate
        long successfulRequests = results.stream().filter(TestResult::isSuccess).count();
        double successRate = ((double) successfulRequests / totalRequests) * 100.0;
        metrics.put("successRate", successRate);
        
        // Response times
        List<Double> responseTimes = results.stream()
            .map(TestResult::getResponseTime)
            .collect(Collectors.toList());
            
        double totalResponseTime = responseTimes.stream().mapToDouble(Double::doubleValue).sum();
        double avgResponseTime = totalResponseTime / totalRequests;
        metrics.put("avgResponseTime", avgResponseTime);
        
        // Min/Max response times
        double minResponseTime = Collections.min(responseTimes);
        double maxResponseTime = Collections.max(responseTimes);
        metrics.put("minResponseTime", minResponseTime);
        metrics.put("maxResponseTime", maxResponseTime);
        
        // Standard deviation
        double variance = responseTimes.stream()
            .mapToDouble(rt -> Math.pow(rt - avgResponseTime, 2))
            .sum() / totalRequests;
        double stdDev = Math.sqrt(variance);
        metrics.put("stdDeviation", stdDev);
        
        // Percentiles
        Collections.sort(responseTimes);
        int index90 = (int) Math.ceil(0.9 * totalRequests) - 1;
        int index95 = (int) Math.ceil(0.95 * totalRequests) - 1;
        int index99 = (int) Math.ceil(0.99 * totalRequests) - 1;
        
        metrics.put("90thPercentile", responseTimes.get(Math.min(index90, totalRequests - 1)));
        metrics.put("95thPercentile", responseTimes.get(Math.min(index95, totalRequests - 1)));
        metrics.put("99thPercentile", responseTimes.get(Math.min(index99, totalRequests - 1)));
        
        // Requests per second
        double totalDurationSec = (results.get(results.size() - 1).getEndTime() - 
                                  results.get(0).getStartTime()) / 1000.0;
        if (totalDurationSec > 0) {
            double rps = totalRequests / totalDurationSec;
            metrics.put("requestsPerSecond", rps);
        }
        
        // Error rate
        double errorRate = 100.0 - successRate;
        metrics.put("errorRate", errorRate);
        
        // Status code distribution
        Map<Integer, Long> statusCodeDistribution = results.stream()
            .collect(Collectors.groupingBy(TestResult::getStatusCode, Collectors.counting()));
        metrics.put("statusCodeDistribution", statusCodeDistribution);
    }
    
    return metrics;
}
```

This demonstrates how to extend the framework with additional components and features to meet your specific testing needs.