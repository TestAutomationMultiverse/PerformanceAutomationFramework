# Extending the Java Performance Testing Framework

This guide provides instructions on how to extend the framework with new protocols, engines, and features.

## Table of Contents

1. [Adding a New Protocol](#adding-a-new-protocol)
2. [Creating a Custom Test Engine](#creating-a-custom-test-engine)
3. [Enhancing Report Generation](#enhancing-report-generation)
4. [Adding Custom Metrics](#adding-custom-metrics)

## Adding a New Protocol

The framework uses the Protocol interface to abstract different communication protocols. Here's how to add a new one:

### Step 1: Implement the Protocol Interface

Create a new class that implements the `Protocol` interface:

```java
package io.perftest.protocol;

import io.perftest.model.Request;
import io.perftest.model.TestResult;

import java.util.Map;

public class MyCustomProtocol implements Protocol {
    
    @Override
    public TestResult execute(Request request, Map<String, String> variables) {
        // Initialize test result
        TestResult result = new TestResult();
        result.setRequestName(request.getName());
        result.setStartTime(System.currentTimeMillis());
        
        try {
            // Implement your protocol-specific logic here
            // For example, if this is an MQTT protocol:
            // 1. Connect to MQTT broker
            // 2. Subscribe or publish based on request
            // 3. Measure response time
            // 4. Set success status and response data
            
            // Simulate a successful response
            Thread.sleep(50); // Simulate network delay
            result.setSuccess(true);
            result.setStatusCode(200);
            result.setResponseBody("Protocol response data");
            
        } catch (Exception e) {
            // Handle errors
            result.setSuccess(false);
            result.setStatusCode(500);
            result.setErrorMessage(e.getMessage());
        } finally {
            // Calculate response time
            result.setEndTime(System.currentTimeMillis());
            result.setResponseTime(result.getEndTime() - result.getStartTime());
        }
        
        return result;
    }
}
```

### Step 2: Register the Protocol in the Factory

Update the `ProtocolFactory` class to include your new protocol:

```java
package io.perftest.protocol;

public class ProtocolFactory {
    
    public static Protocol getProtocol(String protocolName) {
        if (protocolName == null || protocolName.isEmpty()) {
            throw new IllegalArgumentException("Protocol name cannot be null or empty");
        }
        
        switch (protocolName.toLowerCase()) {
            case "http":
                return new HttpProtocol();
            case "https":
                return new HttpProtocol(); // Reuse HTTP for HTTPS
            case "mqtt":
                return new MqttProtocol(); // Your new protocol
            case "custom":
                return new MyCustomProtocol(); // Your custom protocol
            default:
                throw new IllegalArgumentException("Unsupported protocol: " + protocolName);
        }
    }
}
```

### Step 3: Use the New Protocol

Now you can use your new protocol in YAML configurations:

```yaml
scenarios:
  - name: MQTT Test
    requests:
      - name: Publish Message
        protocol: mqtt
        method: PUBLISH
        endpoint: mqtt://broker.example.com:1883/topic
        body: '{"temperature": 25.5, "humidity": 60}'
```

Or programmatically:

```java
Protocol mqttProtocol = ProtocolFactory.getProtocol("mqtt");
Request request = new Request();
request.setName("MQTT Publish");
request.setProtocol("mqtt");
request.setMethod("PUBLISH");
request.setEndpoint("mqtt://broker.example.com:1883/topic");
request.setBody("{\"temperature\": 25.5, \"humidity\": 60}");

Map<String, String> variables = new HashMap<>();
TestResult result = mqttProtocol.execute(request, variables);
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