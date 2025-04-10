# Java Performance Testing Framework - Quick Start Guide

This guide will help you quickly get started with the Java Performance Testing Framework.

## Prerequisites

- Java 19 or higher
- Maven 3.6 or higher

## Installation

1. Clone the repository
2. Build the project:

```bash
mvn clean compile
```

## Running Your First Test

### Method 1: Using the JMeter DSL Test (Programmatic API)

The easiest way to start is by running the pre-configured JMeter DSL test:

```bash
mvn exec:java -Dexec.mainClass="io.perftest.JMeterDSLTest"
```

This will:
1. Run a sample performance test against public test endpoints
2. Generate an HTML report in `target/reports/jmeter-dsl-test` directory
3. Display test metrics in the console output

### Method 2: Using YAML Configuration

Create a YAML configuration file or use the sample one provided at `src/main/resources/configs/sample_config.yaml`:

```bash
mvn exec:java -Dexec.mainClass="io.perftest.App" -Dexec.args="src/main/resources/configs/sample_config.yaml"
```

This will:
1. Parse the YAML configuration file
2. Execute performance tests for all scenarios defined in the file
3. Generate HTML reports in the `target/reports` directory

## Creating Your Own Tests

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
  baseUrl: https://api.example.com
  userId: 123
  apiKey: your-api-key

protocolName: http

scenarios:
  - name: API Test
    variables:
      scenarioSpecificVar: value1
    requests:
      - name: Get User
        method: GET
        endpoint: ${baseUrl}/users/${userId}
        headers:
          Accept: application/json
          Authorization: Bearer ${apiKey}
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
        variables.put("baseUrl", "https://api.example.com");
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
            "${baseUrl}/endpoint", 
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

You can use CSV files for data-driven testing:

1. Create a CSV file with test data:
```
username,password,expected_status
user1,pass1,200
user2,pass2,200
invalid_user,wrong_pass,401
```

2. Reference it in your YAML config:
```yaml
scenarios:
  - name: Login Test
    dataFiles:
      userCredentials: path/to/users.csv
    requests:
      - name: Login
        method: POST
        endpoint: ${baseUrl}/login
        body: '{"username": "${username}", "password": "${password}"}'
        dataSource: userCredentials
```

## Understanding Test Reports

After running tests, HTML reports are generated in the `target/reports` directory. These reports include:

- Test execution summary
- Success/failure rates
- Response time statistics (min, max, average)
- Percentile distributions (90th, 95th, 99th)
- Charts and graphs

## Next Steps

- Review the complete [README.md](README.md) for more detailed information
- Explore the [sample configurations](src/main/resources/configs) for more examples
- Check the [JMeterDSLTest.java](src/test/java/io/perftest/JMeterDSLTest.java) file for programmatic API usage