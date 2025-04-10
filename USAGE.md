# Using the Performance Testing Framework

This guide provides detailed instructions on how to use the Java Performance Testing Framework for different testing scenarios.

## Table of Contents

- [Running Tests](#running-tests)
  - [Using Maven](#using-maven)
  - [Using the JAR File](#using-the-jar-file)
- [YAML Configuration Guide](#yaml-configuration-guide)
  - [Configuration Structure](#configuration-structure)
  - [Variable Substitution](#variable-substitution)
  - [Request Configuration](#request-configuration)
- [Programmatic API Guide](#programmatic-api-guide)
  - [Basic Test Setup](#basic-test-setup)
  - [Advanced Configuration](#advanced-configuration)
- [Interpreting Test Results](#interpreting-test-results)
  - [Console Output](#console-output)
  - [HTML Reports](#html-reports)
- [Common Testing Scenarios](#common-testing-scenarios)
  - [Load Testing](#load-testing)
  - [Stress Testing](#stress-testing)
  - [Endurance Testing](#endurance-testing)
  - [Spike Testing](#spike-testing)

## Running Tests

### Using Maven

#### Running the JMeter DSL Test Example

```bash
# Clean, compile, and run the JMeter DSL test example
mvn clean compile exec:java -Dexec.mainClass="io.perftest.JMeterDSLTest"
```

#### Running with a YAML Configuration

```bash
# Run the main application with a YAML configuration file
mvn clean compile exec:java -Dexec.mainClass="io.perftest.App" -Dexec.args="src/main/resources/configs/sample_config.yaml"
```

#### Running with Custom Parameters

```bash
# Example with additional JVM parameters
mvn clean compile exec:java -Dexec.mainClass="io.perftest.App" -Dexec.args="config.yaml" -Dexec.vmArgs="-Xmx1g -Dlogback.configurationFile=logback-test.xml"
```

### Using the JAR File

```bash
# First, create the JAR file with dependencies
mvn clean package shade:shade

# Then run the application
java -jar target/java-performance-framework-1.0-SNAPSHOT.jar path/to/config.yaml
```

## YAML Configuration Guide

### Configuration Structure

The YAML configuration file has the following structure:

```yaml
name: API Performance Test                # Test suite name
description: Test suite for HTTP API endpoints # Optional description
protocol: http                           # Default protocol (http, https, etc.)
executionConfig:                         # Global execution configuration
  threads: 10                            # Number of concurrent users
  iterations: 5                          # Iterations per thread
  rampUpSeconds: 2                       # Time to ramp up to full thread count
  holdSeconds: 3                         # Time to maintain full load
variables:                               # Global variables for substitution
  baseUrl: https://api.example.com
  authToken: YOUR_AUTH_TOKEN
  userId: 12345
scenarios:                               # Test scenarios (one or more)
  - name: User API Tests                 # Scenario name
    description: User-related API tests  # Optional description
    variables:                           # Scenario-specific variables
      endpoint: /users
    requests:                            # Requests in this scenario
      - name: Get Users                  # Request name
        method: GET                      # HTTP method (GET, POST, PUT, DELETE, etc.)
        protocol: http                   # Protocol override (optional)
        endpoint: ${baseUrl}${endpoint}  # URL with variable substitution
        headers:                         # HTTP headers
          Content-Type: application/json
          Authorization: Bearer ${authToken}
        params:                          # Query parameters
          page: 1
          limit: 10
        variables:                       # Request-specific variables
          requestId: req-${userId}-123
      - name: Create User                # Another request in the same scenario
        method: POST
        endpoint: ${baseUrl}${endpoint}
        headers:
          Content-Type: application/json
          Authorization: Bearer ${authToken}
        body: >                          # Request body (for POST, PUT, etc.)
          {
            "name": "Test User",
            "email": "test-${userId}@example.com"
          }
```

### Variable Substitution

Variables can be defined at three levels:

1. **Global**: Available to all scenarios and requests
2. **Scenario**: Available to all requests in that scenario
3. **Request**: Available only to that specific request

Variables are referenced using the `${variableName}` syntax and can be used in:

- Endpoints
- Headers
- Query parameters
- Request bodies
- Other variable values

Example:
```yaml
variables:
  baseUrl: https://api.example.com
  userId: 123
scenarios:
  - name: User API Test
    variables:
      endpoint: /users/${userId}  # Uses global userId variable
    requests:
      - name: Get User
        endpoint: ${baseUrl}${endpoint}  # Uses both global and scenario variables
```

### Request Configuration

Each request can have the following properties:

- **name**: Request name (required)
- **method**: HTTP method (GET, POST, PUT, DELETE, etc.) (required)
- **protocol**: Protocol to use (http, https, etc.) (optional)
- **endpoint**: Full URL or path (required)
- **headers**: HTTP headers (optional)
- **params**: Query parameters (optional)
- **body**: Request body for POST/PUT requests (optional)
- **variables**: Request-specific variables (optional)
- **validation**: Response validation criteria (optional)
- **extractions**: Data to extract from responses (optional)

## Programmatic API Guide

### Basic Test Setup

Here's how to create a basic test using the programmatic API:

```java
import io.perftest.engine.JMDSLEngine;
import io.perftest.model.ExecutionConfig;
import io.perftest.model.TestResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicTest {
    public static void main(String[] args) throws Exception {
        // 1. Create execution configuration
        ExecutionConfig config = new ExecutionConfig();
        config.setThreads(5);                  // 5 concurrent users
        config.setIterations(10);              // 10 iterations per user
        config.setRampUpSeconds(2);            // 2 seconds ramp-up time
        config.setHoldSeconds(0);              // No hold time
        config.setReportDirectory("target/reports"); // Report directory

        // 2. Define variables for substitution
        Map<String, String> variables = new HashMap<>();
        variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
        variables.put("endpoint", "/posts");
        config.setVariables(variables);

        // 3. Initialize the JMeter DSL engine
        JMDSLEngine engine = new JMDSLEngine(config);
        engine.initialize(config.getVariables());

        // 4. Define headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        // 5. Define query parameters
        Map<String, String> params = new HashMap<>();
        params.put("userId", "1");

        // 6. Execute a GET request
        List<TestResult> results = engine.executeJMeterDslTest(
            "Get Posts Test",               // Test name
            "http",                         // Protocol
            "${baseUrl}${endpoint}",        // URL with variable substitution
            "GET",                          // HTTP method
            null,                           // No body for GET
            headers,                        // Headers
            params                          // Query parameters
        );

        // 7. Get and display metrics
        Map<String, Object> metrics = engine.getMetrics();
        System.out.println("Test completed with " + results.size() + " results");
        System.out.println("Success rate: " + metrics.get("successRate") + "%");
        System.out.println("Average response time: " + metrics.get("avgResponseTime") + " ms");

        // 8. Clean up
        engine.shutdown();
    }
}
```

### Advanced Configuration

For more advanced scenarios:

```java
// Configure exponential think time between requests
config.setThinkTimeEnabled(true);
config.setThinkTimeMinMs(500);    // Minimum think time in ms
config.setThinkTimeMaxMs(2000);   // Maximum think time in ms
config.setThinkTimeType("exponential"); // Distribution type (exponential, uniform, constant)

// Configure connection pooling
config.setConnectionTimeout(3000);  // Connection timeout in ms
config.setSocketTimeout(5000);      // Socket timeout in ms
config.setKeepAlive(true);          // Keep connections alive
config.setMaxConnections(100);      // Maximum connections

// Configure test duration instead of iterations
config.setDurationBasedTest(true);
config.setTestDurationSeconds(60);  // 1-minute test
```

## Interpreting Test Results

### Console Output

The framework outputs detailed metrics to the console:

```
Test completed with 50 results
Total requests: 50
Success rate: 98.0%
Average response time: 234.5 ms
Min/Max response time: 87/532 ms
90th percentile: 312 ms
95th percentile: 389 ms
99th percentile: 492 ms
Report generated at: target/reports/http_test_report_2025-04-10_17-45-11.html
```

### HTML Reports

HTML reports provide detailed visualizations and metrics:

- **Summary**: Overall test statistics
- **Response Times**: Charts showing response time distribution
- **Throughput**: Requests per second over time
- **Errors**: Detailed error information and distribution
- **Request Details**: Individual request metrics and status codes

Reports are stored in the configured report directory (`target/reports` by default).

## Common Testing Scenarios

### Load Testing

Test system performance under expected load:

```yaml
executionConfig:
  threads: 50             # Simulate 50 concurrent users
  iterations: 10          # Each user performs 10 iterations
  rampUpSeconds: 30       # Gradually ramp up over 30 seconds
  holdSeconds: 60         # Maintain full load for 60 seconds
```

### Stress Testing

Test system limits by gradually increasing load:

```yaml
executionConfig:
  threads: 200            # High thread count
  iterations: 5           # Fewer iterations per thread
  rampUpSeconds: 120      # Long ramp-up to increase load gradually
  holdSeconds: 30         # Hold at peak load briefly
```

### Endurance Testing

Test system stability over an extended period:

```yaml
executionConfig:
  threads: 25             # Moderate user load
  durationBasedTest: true # Use time-based test instead of iterations
  testDurationSeconds: 3600 # 1-hour test
  rampUpSeconds: 60       # 1-minute ramp-up
```

### Spike Testing

Test system response to sudden load spikes:

```yaml
executionConfig:
  threads: 100            # High thread count
  iterations: 2           # Just a few iterations
  rampUpSeconds: 5        # Very short ramp-up (spike)
  holdSeconds: 30         # Brief hold at peak
```

---

For more details and advanced usage, refer to the Javadoc documentation in the `doc/` directory.