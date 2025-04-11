# Java Performance Testing Framework - Quickstart Guide

This guide provides a quick introduction to using the Java Performance Testing Framework for executing HTTP performance tests.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Running Your First Test](#running-your-first-test)
4. [Creating Custom Tests](#creating-custom-tests)
5. [Using CSV Data Sources](#using-csv-data-sources)
6. [Analyzing Test Results](#analyzing-test-results)
7. [Troubleshooting](#troubleshooting)
8. [Next Steps](#next-steps)

## Prerequisites

Before starting, ensure you have:

- Java 19 or later installed
- Maven 3.8 or later installed
- Git to clone the repository (optional)

## Installation

1. Clone the repository
2. Build the project:

```bash
mvn clean compile
```

## Running Your First Test

The framework provides two ways to run tests:

### Method 1: Using JMeter DSL Test (Recommended)

The simplest way to start is by running the pre-configured JMeter DSL test:

```bash
mvn exec:java -Dexec.mainClass="io.perftest.JMeterDSLTest"
```

This will:
1. Run a sample performance test against public test endpoints
2. Generate an HTML report in the `target/reports/jmeter-dsl-test` directory
3. Create JTL files in the `target/jmeter-reports` directory
4. Display test metrics in the console output

The sample test in JMeterDSLTest.java includes:
- A GET request test to retrieve users
- A POST request test to create a new post
- Basic performance metrics collection and reporting

### Method 2: Using YAML Configuration

You can also run tests using a YAML configuration file:

```bash
mvn exec:java -Dexec.mainClass="io.perftest.App" -Dexec.args="src/main/resources/configs/sample_config.yaml"
```

## Creating Custom Tests

### Option 1: Create a Custom YAML Configuration

Create a YAML file with your test scenarios:

```yaml
# test_config.yaml
executionConfig:
  threads: 5
  iterations: 10
  rampUpSeconds: 2
  holdSeconds: 3

variables:
  baseUrl: https://jsonplaceholder.typicode.com
  userId: 1

protocolName: http

scenarios:
  - name: REST API Test
    variables:
      scenarioVar: value1
    requests:
      - name: Get User
        method: GET
        endpoint: ${baseUrl}/users/${userId}
        
      - name: Create Post
        method: POST
        endpoint: ${baseUrl}/posts
        headers:
          Content-Type: application/json
        body: '{"title": "Test Post", "body": "This is a test post", "userId": ${userId}}'
```

Then run it:

```bash
mvn exec:java -Dexec.mainClass="io.perftest.App" -Dexec.args="path/to/test_config.yaml"
```

### Option 2: Create a Custom Java Test Class

Create a Java class that uses the framework's API:

```java
import io.perftest.engine.JMDSLEngine;
import io.perftest.model.ExecutionConfig;
import io.perftest.model.TestResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTest {
    public static void main(String[] args) throws Exception {
        // Configure test
        ExecutionConfig config = new ExecutionConfig();
        config.setThreads(10);
        config.setIterations(5);
        config.setRampUpSeconds(2);
        
        // Set variables
        Map<String, String> variables = new HashMap<>();
        variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
        config.setVariables(variables);
        
        // Initialize engine
        JMDSLEngine engine = new JMDSLEngine(config);
        engine.initialize(variables);
        
        // Set headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        
        // Execute test
        List<TestResult> results = engine.executeJMeterDslTest(
            "My API Test", 
            "http", 
            "${baseUrl}/users/1", 
            "GET", 
            null, 
            headers, 
            null
        );
        
        // Print results
        Map<String, Object> metrics = engine.getMetrics();
        System.out.println("Success rate: " + metrics.get("successRate") + "%");
        
        // Cleanup
        engine.shutdown();
    }
}
```

Then run your custom test:

```bash
mvn exec:java -Dexec.mainClass="MyTest"
```

## Using CSV Data Sources

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

## Analyzing Test Results

After running a test, the framework generates:

1. Console output with basic metrics:
   - Total request count
   - Success rate
   - Average response time
   - 90th percentile response time

2. HTML report files in the `target/reports` directory:
   - For JMeter DSL tests: `target/reports/jmeter-dsl-test/performance_report_TIMESTAMP.html`
   - These reports include charts, graphs, and detailed statistics

3. JTL files in the `target/jmeter-reports` directory for compatibility with JMeter reporting tools:
   - Format: `<request_name>_<timestamp>.jtl`
   - Standard JMeter CSV format with all required columns (timeStamp, elapsed, label, responseCode, etc.)
   - Can be imported into JMeter for further analysis

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

## Next Steps

After mastering the basics, explore:
- [YAML Configuration Guide](YAML_CONFIG.md) for detailed configuration options
- [Extending the Framework](EXTENDING.md) to add custom protocols and features
- [Usage Guide](USAGE.md) for more detailed examples
- [Architecture Overview](ARCHITECTURE.md) to understand the framework's design

For more comprehensive information, refer to the [main README](../README.md).