# Java Performance Testing Framework

A flexible, extensible Java framework for performing comprehensive performance testing across multiple protocols with advanced configuration options and intelligent reporting capabilities.

## Overview

This performance testing framework is designed to provide an intuitive, configurable approach to load and performance testing of APIs and services. Built on Java 19 with a modular architecture following the Entity Component System (ECS) pattern, it supports both programmatic test definition and YAML-based configuration.

Key features:
- Multi-protocol support (HTTP, with planned extension to HTTPS, TCP, UDP, JDBC, JMS, MQTT)
- Flexible test configuration through YAML files or direct Java API
- Dynamic variable substitution for realistic test scenarios
- CSV-driven parameterized testing
- Comprehensive performance metrics with percentile calculations
- Integration with JMeter reporting tools
- Entity Component System (ECS) architecture for maximum flexibility and extensibility
- JMeter-compatible JTL file format (CSV) for integration with existing JMeter tools
- Standard and experimental JMeter engines (DSL and TreeBuilder)

## Architecture

The framework follows a layered architecture:

```
┌────────────────────────────────────────────────────────┐
│                    User Interface                       │
│  ┌─────────────────┐            ┌──────────────────┐   │
│  │     YAML        │            │   Direct API     │   │
│  │  Configuration  │            │      Usage       │   │
│  └─────────────────┘            └──────────────────┘   │
└────────────────────────────────────────────────────────┘
                        │                  │
                        ▼                  ▼
┌────────────────────────────────────────────────────────┐
│                 Test Execution Core                     │
│  ┌─────────────────┐            ┌──────────────────┐   │
│  │  Configuration  │            │   Test Runner    │   │
│  │     Parser      │            │                  │   │
│  └─────────────────┘            └──────────────────┘   │
└────────────────────────────────────────────────────────┘
                        │                  │
                        ▼                  ▼
┌────────────────────────────────────────────────────────┐
│                 Engine Implementation                   │
│  ┌─────────────────┐            ┌──────────────────┐   │
│  │     JMeter      │            │  Custom Engine   │   │
│  │   DSL Engine    │            │  Implementations │   │
│  └─────────────────┘            └──────────────────┘   │
└────────────────────────────────────────────────────────┘
                        │                  │
                        ▼                  ▼
┌────────────────────────────────────────────────────────┐
│                   Protocol Layer                        │
│  ┌─────────────────┐            ┌──────────────────┐   │
│  │      HTTP       │            │  Other Protocol  │   │
│  │    Protocol     │            │  Implementations │   │
│  └─────────────────┘            └──────────────────┘   │
└────────────────────────────────────────────────────────┘
                        │                  │
                        ▼                  ▼
┌────────────────────────────────────────────────────────┐
│                    Target Systems                       │
│  (HTTP APIs, Databases, Message Queues, etc.)          │
└────────────────────────────────────────────────────────┘
```

### Core Components

1. **Test Configuration**
   - YAML Configuration Parser
   - Direct Java API

2. **Test Execution**
   - Scenario Manager
   - Request Builder
   - Variable Resolver

3. **Test Engines**
   - JMeter DSL Engine
   - Extensible Engine Interface

4. **Protocol Support**
   - HTTP/HTTPS Implementation
   - Protocol Interface for Extensions

5. **Reporting**
   - Real-time Metrics
   - JMeter-style HTML Report Generation
   - Standard JTL Export in JMeter CSV Format
   - Comprehensive Visualization of Test Results

## Getting Started

### Prerequisites
- Java 19 or later
- Maven 3.8 or later

### Quick Start

Run a sample performance test:

```bash
# Clone the repository
git clone https://github.com/yourusername/java-performance-framework.git
cd java-performance-framework

# Run with Maven (choose one of the runners)
mvn clean compile exec:java -Dexec.mainClass="io.ecs.runner.JMeterDSLRunner"
mvn clean compile exec:java -Dexec.mainClass="io.ecs.runner.GatlingRunner"
mvn clean compile exec:java -Dexec.mainClass="io.ecs.runner.JMeterTreeRunner"
```

See the [Quickstart Guide](docs/QUICKSTART.md) for more examples.

### Using YAML Configuration

Create a YAML file defining your test:

```yaml
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

Run the test using the YAML configuration:

```bash
mvn clean compile exec:java -Dexec.mainClass="io.ecs.runner.JMeterDSLRunner" -Dexec.args="your_config.yaml"
```

For more details on YAML configuration, see the [YAML Configuration Guide](docs/YAML_CONFIG.md).

### Using the Java API

```java
import io.perftest.engine.JMDSLEngine;
import io.perftest.model.ExecutionConfig;
import io.perftest.model.Request;
import io.perftest.model.TestResult;

import java.util.HashMap;
import java.util.Map;

public class SimpleTest {
    public static void main(String[] args) {
        // Create execution configuration
        ExecutionConfig config = new ExecutionConfig();
        config.setThreads(5);
        config.setIterations(10);
        
        // Initialize the engine
        JMDSLEngine engine = new JMDSLEngine(config);
        
        // Set global variables
        Map<String, String> variables = new HashMap<>();
        variables.put("baseUrl", "https://jsonplaceholder.typicode.com");
        variables.put("userId", "1");
        engine.initialize(variables);
        
        // Create a request
        Request request = new Request();
        request.setName("Get User");
        request.setProtocol("http");
        request.setMethod("GET");
        request.setEndpoint("${baseUrl}/users/${userId}");
        
        // Execute the request
        TestResult result = engine.executeRequest(request);
        
        // Get performance metrics
        Map<String, Object> metrics = engine.getMetrics();
        System.out.println("Test Results:");
        System.out.println("Success: " + result.isSuccess());
        System.out.println("Response Time: " + result.getResponseTime() + "ms");
        System.out.println("Success Rate: " + metrics.get("successRate") + "%");
        
        // Shut down the engine
        engine.shutdown();
    }
}
```

## Documentation

- [Quickstart Guide](docs/QUICKSTART.md)
- [YAML Configuration Reference](docs/YAML_CONFIG.md)
- [Extending the Framework](docs/EXTENDING.md)
- [Usage Guide](docs/USAGE.md)
- [Architecture Overview](docs/ARCHITECTURE.md)

## Features

### Current Features

- HTTP protocol support
- Configurable thread and iteration count
- Variable substitution
- Request templating
- Performance metrics collection
- JMeter-style HTML report generation with color-coded metrics
- CSV data-driven testing
- Standard JTL files in CSV format for JMeter compatibility
- Detailed logging of test execution
- Robust thread management through JMeter DSL

### Planned Features

- Support for additional protocols (HTTPS, TCP, UDP, JDBC, JMS, MQTT)
- Advanced scenario flow control (conditions, loops)
- Response validation
- Response extraction for variables
- Custom assertion support
- Distributed testing
- Real-time monitoring dashboard

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.