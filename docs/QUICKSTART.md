# Java Performance Testing Framework Quickstart Guide

This guide provides a quick introduction to using the Java Performance Testing Framework for executing HTTP performance tests.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Running a Simple HTTP Test](#running-a-simple-http-test)
3. [Using YAML Configuration](#using-yaml-configuration)
4. [Analyzing Test Results](#analyzing-test-results)
5. [Troubleshooting](#troubleshooting)

## Prerequisites

Before starting, ensure you have:

- Java 19 or later installed
- Maven 3.8 or later installed
- Git to clone the repository (optional)

## Running a Simple HTTP Test

The framework provides two ways to run tests:
1. Using direct Java code
2. Using YAML configuration files

### Method 1: Using Java Code

The simplest way to create a test is by using the JMeterDSLTest class:

```java
// Clone the repository and navigate to the project directory
git clone https://github.com/yourusername/java-performance-framework.git
cd java-performance-framework

// Run the existing sample test
mvn clean compile exec:java -Dexec.mainClass="io.perftest.JMeterDSLTest"
```

The sample test in JMeterDSLTest.java includes:
- A GET request test to retrieve users
- A POST request test to create a new user
- Basic performance metrics collection

### Method 2: Using the App Class

You can also run tests using the App class:

```bash
mvn clean compile exec:java -Dexec.mainClass="io.perftest.App" -Dexec.args="sample_config.yaml"
```

## Using YAML Configuration

Creating tests with YAML provides a more flexible approach without changing code:

1. Create a YAML configuration file:

```yaml
# sample_config.yaml
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
      - name: Get All Posts
        protocol: http
        method: GET
        endpoint: ${baseUrl}/posts
        
      - name: Get User
        protocol: http
        method: GET
        endpoint: ${baseUrl}/users/${userId}
        
      - name: Create Post
        protocol: http
        method: POST
        endpoint: ${baseUrl}/posts
        headers:
          Content-Type: application/json
        body: '{"title": "Test Post", "body": "This is a test post", "userId": ${userId}}'
```

2. Run the test using the App class:

```bash
mvn clean compile exec:java -Dexec.mainClass="io.perftest.App" -Dexec.args="sample_config.yaml"
```

## Adding Data-Driven Testing

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
      posts: sample_HTTP_API_Test.csv
    requests:
      - name: Get Post by ID
        protocol: http
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
   - 90th and 95th percentiles

2. HTML report files in the `target/reports` directory:
   - For JMeter DSL tests: `target/reports/jmeter-dsl-test/performance_report_TIMESTAMP.html`
   - For direct HTTP tests: `target/reports/direct-http-test/http_test_report_TIMESTAMP.html`

3. JTL files for compatibility with JMeter reporting tools

## Customizing Tests

To customize tests:

1. Modify the threads and iterations in the YAML configuration or Java code
2. Add custom headers, parameters, and authentication
3. Use variable substitution with `${variableName}` syntax
4. Create custom scenarios with different request sequences

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
- API documentation in the JavaDocs

For more detailed information, refer to the [full documentation](../README.md).