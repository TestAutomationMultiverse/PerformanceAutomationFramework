# Generic Performance Framework - Comprehensive Guide

This guide provides a complete introduction to using the Generic Performance Framework for executing performance tests.

## Table of Contents

1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Prerequisites](#prerequisites)
4. [Installation](#installation)
5. [Quick Start](#quick-start)
   - [Method 1: Using JMeter DSL Test](#method-1-using-jmeter-dsl-test)
   - [Method 2: Using YAML Configuration](#method-2-using-yaml-configuration)
   - [Method 3: Using JMeter TreeBuilder Test](#method-3-using-jmeter-treebuilder-test)
5. [Configuration Options](#configuration-options)
   - [YAML Configuration Structure](#yaml-configuration-structure)
   - [Variable Substitution](#variable-substitution)
   - [Request Configuration](#request-configuration)
   - [Path Resolution System](#path-resolution-system)
6. [Programmatic API Guide](#programmatic-api-guide)
   - [Basic Test Setup](#basic-test-setup)
   - [Advanced Configuration](#advanced-configuration)
7. [Types of Tests](#types-of-tests)
   - [Load Testing](#load-testing)
   - [Stress Testing](#stress-testing)
   - [Endurance Testing](#endurance-testing)
   - [Spike Testing](#spike-testing)
8. [Test Results and Reports](#test-results-and-reports)
   - [Console Output](#console-output)
   - [HTML Reports](#html-reports)
   - [JTL Files](#jtl-files)
9. [Troubleshooting](#troubleshooting)
10. [Advanced Topics](#advanced-topics)
11. [Next Steps](#next-steps)

## Introduction

The Generic Performance Framework is a versatile tool for performance testing REST APIs and web services. It features:

- Simple JMeter DSL-based test creation
- YAML configuration for non-programmers
- Detailed HTML and JTL report generation
- Data-driven testing with CSV files
- Advanced metrics collection and analysis
- Support for various testing protocols (HTTP, HTTPS, JDBC, etc.)
- Alternative JMeter TreeBuilder implementation for native JMeter integration

## Project Structure

The Generic Performance Framework uses a multi-module Maven structure:

### Module Organization

```
generic-performance-framework/          # Parent project
├── generic-performance-framework-core/  # Core library module
│   ├── src/main/java/                   # Core framework code
│   ├── src/test/java/                   # Unit tests for core
│   └── pom.xml                          # Core module POM
├── generic-performance-framework-tests/ # Test examples module
│   ├── src/test/java/                   # Test examples
│   ├── src/test/resources/              # Test resources
│   │   ├── configs/                     # YAML configuration files
│   │   ├── data/                        # CSV test data
│   │   ├── templates/                   # Request templates
│   │   └── jmeter-props/                # JMeter properties
│   └── pom.xml                          # Tests module POM
└── pom.xml                             # Parent POM file
```

### Module Responsibilities

- **generic-performance-framework-core**:
  - Contains all framework core functionality
  - Designed to be used as a dependency in other projects
  - Published to GitHub Packages for easy consumption
  - Includes all engine implementations, protocol support, and reporting

- **generic-performance-framework-tests**:
  - Contains example tests and configurations
  - Demonstrates different testing approaches
  - Uses the core module as a dependency
  - Includes test resources and templates

### Maven Configuration

The Maven configuration follows best practices for multi-module projects:

- The parent POM defines common properties, dependencies, and plugin management
- Each module has its own POM file with specific configurations
- Dependencies are managed at the parent level for consistent versioning
- The core module includes source and javadoc JAR generation for publishing

## Prerequisites

Before starting, ensure you have:

- Java 19 or later installed
- Maven 3.8 or later installed
- Git to clone the repository (optional)

## Installation

1. Clone the repository:
```bash
git clone https://github.com/TestAutomationMultiverse/GenericPerformanceFramework.git
cd GenericPerformanceFramework
```

2. Build the project:
```bash
mvn clean compile
```

## Quick Start

The framework provides multiple ways to run tests:

### Method 1: Using JMeter DSL Test

The simplest way to start is by running the pre-configured JMeter DSL test:

```bash
mvn exec:java -Dexec.mainClass="io.ecs.system.MainRunner" -Dexec.args="dsl"
```

Or use the specific test class:

```bash
mvn exec:java -Dexec.mainClass="io.ecs.JMeterDSLTest"
```

This will:
1. Run a sample performance test against public test endpoints
2. Generate an HTML report in the `target/reports/jmeter-dsl-test` directory
3. Create JTL files in the `target/jmeter-reports` directory
4. Display test metrics in the console output

### Method 2: Using YAML Configuration

For a more declarative approach, run tests using a YAML configuration file:

```bash
mvn exec:java -Dexec.mainClass="io.ecs.system.MainRunner" -Dexec.args="yaml src/main/resources/configs/sample_config.yaml"
```

This approach is excellent for non-developers or for defining test configurations outside of code.

### Method 3: Using JMeter TreeBuilder Test

For native JMeter-like implementation using the standard JMeter API rather than the DSL:

```bash
mvn exec:java -Dexec.mainClass="io.ecs.system.MainRunner" -Dexec.args="treebuilder"
```

Or run the specific test class:

```bash
mvn exec:java -Dexec.mainClass="io.ecs.JMeterTreeBuilderTest"
```

This experimental implementation uses the standard JMeter API directly for a more traditional approach.

## Configuration Options

### YAML Configuration Structure

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
  successThreshold: 80.0                 # Success rate threshold (% of requests that must pass for test to succeed)
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

Variables are referenced using the `${variableName}` syntax and can be used in endpoints, headers, query parameters, request bodies, and other variable values.

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

### Path Resolution System

The framework includes a sophisticated path resolution system that allows you to reference templates and resource files using simplified paths in your YAML configurations.

#### Simplified Path References

Instead of specifying full paths to template files:

```yaml
requests:
  - name: Create User
    method: POST
    endpoint: ${baseUrl}/users
    headers: src/test/resources/templates/http/headers/default_headers.json
    body: src/test/resources/templates/http/body/create_user_body.json
    params: src/test/resources/templates/http/params/user_params.template
```

You can use just the filename:

```yaml
requests:
  - name: Create User
    method: POST
    endpoint: ${baseUrl}/users
    headers: default_headers.json
    body: create_user_body.json
    params: user_params.template
```

#### How Path Resolution Works

The framework uses several strategies to locate the correct file:

1. **Check if the full path exists**: If the provided path exists as-is, it's used directly
2. **Extension-based resolution**: Files are searched in appropriate directories based on their extension
3. **Name-based resolution**: Files with "header" in the name are checked in the headers directory, etc.
4. **Fallback mechanism**: If the exact location can't be determined, all potential directories are searched

#### Supported File Types

The path resolution system automatically resolves:

- **Header Templates**: JSON files with header definitions (`.json`)
- **Body Templates**: JSON files with request body templates (`.json`)
- **Parameter Templates**: Template files for query parameters (`.template`)
- **Schema Files**: JSON Schema files for response validation (`.schema.json`)
- **Data Files**: CSV files for data-driven testing (`.csv`)

#### File Naming Conventions

For optimal resolution, follow these naming conventions:

- Header files: Include "header" in the name (e.g., `default_headers.json`, `auth_header.json`)
- Body files: Include "body" in the name (e.g., `create_user_body.json`, `update_body.json`)
- Schema files: Include "schema" in the name (e.g., `user_schema.json`, `error.schema.json`)
- Parameter files: Use `.template` extension (e.g., `search_params.template`)
- Data files: Use `.csv` extension (e.g., `users.csv`, `test_data.csv`)

#### Implementation

The path resolution system is implemented in the `FileUtils` class, which handles all file operations in the framework. The key method is `resolveFilePath(String filePath)`, which converts simplified paths to full paths. This functionality is automatically used by the framework when loading template files and data sources from YAML configurations.

## Programmatic API Guide

### Basic Test Setup

Here's how to create a basic test using the programmatic API:

```java
import io.ecs.engine.JMDSLEngine;
import io.ecs.model.ExecutionConfig;
import io.ecs.model.TestResult;

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

## Types of Tests

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

## Test Results and Reports

After running a test, the framework generates several outputs:

### Console Output

The framework outputs detailed metrics to the console:

```
Test Results Summary:
- Total Requests: 3
- Success Rate: 100.0% (threshold: 80.0%)
- Avg Response Time: 104.0ms
- 90th Percentile: 193ms
INFO | Shutting down HTTP Performance Test Engine
INFO | Test report generated at: target/reports/jmeter-dsl-test/performance_report_2025-04-10_19-37-44.html
```

### HTML Reports

HTML reports provide detailed visualizations and metrics:

- **Summary**: Overall test statistics
- **Response Times**: Charts showing response time distribution
- **Throughput**: Requests per second over time
- **Errors**: Detailed error information and distribution
- **Request Details**: Individual request metrics and status codes

HTML reports are stored in the configured report directory (`target/reports/jmeter-dsl-test` by default).

### JTL Files

JTL (JMeter Test Log) files provide detailed test data:

- Stored in the `target/jmeter-reports` directory
- Standard JMeter CSV format compatible with JMeter's reporting tools
- Contains detailed metrics for each request including timestamp, response time, status codes
- Can be used with JMeter's reporting capabilities for further analysis

### Success Thresholds

The framework supports configurable success thresholds for test evaluation:

- Set `successThreshold` in the execution configuration to define the percentage of requests that must succeed for a test to pass
- Default value is 100% if not specified (all requests must succeed)
- Useful for tests against public APIs or environments where some failures are acceptable
- The success threshold is used by both JMDSLEngine and JMTreeBuilderEngine

Example in YAML configuration:
```yaml
executionConfig:
  threads: 10
  iterations: 5
  successThreshold: 80.0  # Test will pass if 80% or more requests are successful
```

Example in programmatic API:
```java
ExecutionConfig config = new ExecutionConfig();
config.setThreads(5);
config.setIterations(10);
config.setSuccessThreshold(90.0);  // 90% success rate required
```

Success rate is calculated as: (successful_requests ÷ total_requests) × 100
Success thresholds are particularly useful when testing against public APIs that might have rate limiting or occasional failures.

## Troubleshooting

Common issues and solutions:

### Connection Issues

If you encounter connection problems:

```
Error executing request Get Users: Connection refused
```

Check if:
- The target endpoint URL is correct
- Your network connection is working
- You have the necessary permissions to access the API

### Compilation Errors

For compilation errors:

```
[ERROR] Could not find goal 'exec' in plugin...
```

Make sure you:
- Have the correct Maven dependencies
- Are using the correct Maven command
- Have Java 19+ configured properly

### Performance Issues

If tests run slow:
- Reduce the thread count or iterations
- Check if the target API has rate limiting
- Ensure your machine has sufficient resources

## Advanced Topics

### Understanding Protocol Implementation

The framework has been consolidated to focus on HTTP/HTTPS protocol implementation for simplicity and maintainability:

1. The unified `Protocol` interface is in the `io.ecs.engine` package
2. The primary implementation is `HttpProtocol` in the `io.ecs.protocols` package
3. All HTTP and HTTPS requests are handled by this single implementation

#### Key Features of the HTTP Protocol Implementation

- Variable substitution in URLs, headers, and request bodies
- Comprehensive HTTP method support (GET, POST, PUT, DELETE, PATCH, etc.)
- Configurable timeouts and connection parameters
- Response metrics tracking (status code, response time, received bytes)
- Error handling and reporting

Example usage in YAML configuration:
```yaml
protocol: http  # or https - both use the same implementation internally
```

Example usage in code:
```java
// Get the protocol implementation
Protocol httpProtocol = ProtocolFactory.getProtocol("http");

// Execute a request
httpProtocol.setGlobalVariables(variables);
Response response = httpProtocol.execute(
    "https://api.example.com/users",
    "GET",
    null,  // No body for GET
    headers,
    params
);

// Check the results
System.out.println("Status code: " + response.getStatusCode());
System.out.println("Response time: " + response.getResponseTime() + "ms");
System.out.println("Received bytes: " + response.getReceivedBytes());
```

### Data-Driven Testing with CSV

To run data-driven tests:

1. Create a CSV file with test data:

```csv
# sample_HTTP_API_Test.csv
userId,postId,title
1,1,First Post
2,2,Second Post
3,3,Third Post
```

2. Reference the CSV file in your YAML configuration:

```yaml
scenarios:
  - name: Data-Driven Test
    dataFiles:
      posts: data/sample_HTTP_API_Test.csv
    requests:
      - name: Get Post by ID
        method: GET
        endpoint: ${baseUrl}/posts/${postId}
        dataSource: posts
```

### Using JMeter TreeBuilder Engine

For direct JMeter API integration, use the TreeBuilder engine:

```java
import io.ecs.engine.JMTreeBuilderEngine;
import io.ecs.model.ExecutionConfig;

// Create execution configuration
ExecutionConfig config = new ExecutionConfig();
config.setThreads(10);
config.setIterations(5);

// Initialize TreeBuilder engine
JMTreeBuilderEngine engine = new JMTreeBuilderEngine(config);
```

This approach builds a JMeter test plan directly rather than using the JMeter DSL, offering more traditional JMeter functionality.

## Next Steps

After mastering the basics, explore:
- [YAML Configuration Guide](YAML_CONFIG.md) for detailed configuration options
- [Extending the Framework](EXTENDING.md) to add custom protocols and features
- [Architecture Overview](ARCHITECTURE.md) to understand the framework's design
- [TreeBuilder Guide](TREEBUILDER.md) for using the experimental JMeter TreeBuilder engine

For more comprehensive information, refer to the [main README](../README.md).