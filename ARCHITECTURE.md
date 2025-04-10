# Performance Testing Framework Architecture

This document provides an overview of the architecture and design principles of the Java Performance Testing Framework.

## Table of Contents

- [Architectural Overview](#architectural-overview)
- [Core Components](#core-components)
- [Test Execution Flow](#test-execution-flow)
- [Extension Points](#extension-points)
- [Design Decisions](#design-decisions)

## Architectural Overview

The framework follows a modular, layered architecture designed for extensibility and separation of concerns:

![Architecture Diagram](architecture-diagram.png)

### Layer 1: Configuration

Handles the loading, parsing, and validation of test configurations from YAML files or programmatic definitions.

### Layer 2: Test Orchestration

Manages the lifecycle of test execution, including initialization, thread management, and shutdown.

### Layer 3: Protocol Implementation

Provides protocol-specific adapters that handle the details of executing requests using different protocols.

### Layer 4: Metrics Collection

Gathers and processes performance data during test execution.

### Layer 5: Reporting

Generates human-readable reports from test results.

## Core Components

### App.java

The main entry point for command-line execution. It processes command-line arguments, loads configuration files, and initiates the test execution process.

### Configuration System

#### TestConfiguration.java

The root model object that represents a complete test configuration, including global settings, variables, and test scenarios.

#### YamlConfig.java

Handles parsing YAML configuration files into `TestConfiguration` objects.

#### ExecutionConfig.java

Contains settings that control test execution behavior, such as thread count, iterations, and timing parameters.

### Engine System

#### Engine.java (Interface)

Defines the contract for test execution engines, with methods for initialization, scenario execution, and shutdown.

#### JMDSLEngine.java

Implementation of the `Engine` interface using the JMeter DSL to execute tests. It handles thread management, test execution, and metrics collection.

### Protocol System

#### Protocol.java (Interface)

Defines the contract for protocol implementations, with methods to execute requests and process responses.

#### HttpProtocol.java

Implementation of the `Protocol` interface for HTTP/HTTPS requests, handling details like header processing, query parameters, and variable substitution.

#### ProtocolFactory.java

Factory class that creates and manages protocol implementations.

### Model System

#### Request.java

Represents a single request to be executed, including details like name, endpoint, method, headers, and body.

#### TestResult.java

Represents the result of executing a request, including status, response time, and any errors or extracted data.

#### Scenario.java

Represents a group of related requests that form a logical test scenario.

### Reporting System

#### ReportGenerator.java

Generates HTML reports from test results, including charts and tables for performance metrics.

#### MetricsCollector.java

Collects and processes performance metrics during test execution.

## Test Execution Flow

1. **Configuration Loading**
   - Parse YAML file or process programmatic configuration
   - Create `TestConfiguration` object with scenarios and requests

2. **Engine Initialization**
   - Create engine instance (e.g., `JMDSLEngine`)
   - Process global variables and execution configuration
   - Initialize engine resources

3. **Scenario Execution**
   - For each scenario:
     - Process scenario-specific variables
     - For each request:
       - Apply variable substitution
       - Get appropriate protocol implementation
       - Execute request with specified parameters
       - Collect metrics and results

4. **Results Processing**
   - Compute aggregate metrics (success rate, percentiles, etc.)
   - Generate timestamp-based report files
   - Output summary to console

5. **Engine Shutdown**
   - Release resources
   - Finalize reports

## Extension Points

The framework is designed with several extension points for customization:

### Adding New Protocols

Implement the `Protocol` interface and register your implementation with `ProtocolFactory`:

```java
public class TCPProtocol implements Protocol {
    @Override
    public TestResult execute(Request request, Map<String, String> variables) {
        // TCP-specific implementation
    }
}

// Registration
ProtocolFactory.registerProtocol("tcp", new TCPProtocol());
```

### Creating Custom Engines

Implement the `Engine` interface to create your own test execution engine:

```java
public class CustomEngine implements Engine {
    @Override
    public void initialize(Map<String, String> variables) {
        // Custom initialization
    }

    @Override
    public List<TestResult> executeScenario(String scenarioName, List<Request> requests) {
        // Custom execution logic
    }

    @Override
    public void shutdown() {
        // Custom cleanup
    }
}
```

### Extending Reports

Create custom report templates and implement a custom `ReportGenerator`:

```java
public class CustomReportGenerator extends ReportGenerator {
    @Override
    public void generateReport(String reportPath, List<TestResult> results, Map<String, Object> metrics) {
        // Custom report generation
    }
}
```

## Design Decisions

### JMeter DSL vs. Raw Implementation

The framework uses JMeter DSL for test execution because it provides:
- Robust thread management
- Built-in metrics collection
- Proven stability and performance

However, it also maintains a direct HTTP implementation to:
- Reduce dependencies for simple cases
- Provide a reference implementation
- Allow for comparison testing

### YAML Configuration

YAML was selected for configuration files because:
- Human-readable format
- Support for comments
- Good representation of hierarchical data
- Wide adoption in configuration management

### Modular Protocol System

The protocol system is designed to be modular to:
- Support multiple protocols beyond HTTP
- Allow for protocol-specific optimizations
- Facilitate testing of complex systems with multiple protocols

### Variable Substitution

The multi-level variable substitution system enables:
- Dynamic test data generation
- Environment-specific configurations
- Complex test scenarios with correlations between requests

### Thread Management

The framework handles threads through the engine layer rather than directly:
- Provides consistent behavior across protocols
- Centralizes control of concurrency
- Simplifies metrics collection