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

#### JMTreeBuilderEngine.java (Experimental)

An experimental implementation of the `Engine` interface using JMeter's TreeBuilder API. This approach provides direct access to JMeter's programmatic test plan creation API, allowing for more complex test scenarios and deeper JMeter integration. It follows the same interface as other engine implementations while leveraging JMeter's TreeBuilder capabilities.

### Protocol System

The framework has been consolidated to focus on a unified HTTP/HTTPS protocol implementation:

#### Protocol.java (Interface)

Located in the `io.perftest.engine` package, this interface defines the contract for protocol implementations with methods to:
- Execute requests and process responses
- Handle variable substitution
- Manage global variables
- Convert raw responses to TestResult objects

#### HttpProtocol.java

Located in the `io.perftest.protocols` package, this is the primary implementation of the Protocol interface. It provides:
- Support for both HTTP and HTTPS protocols
- Full HTTP method coverage (GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD)
- Comprehensive metrics tracking (status code, response time, bytes received)
- Error handling and reporting
- Rich variable substitution in URLs, headers, and bodies

#### ProtocolFactory.java

Factory class that creates and manages protocol implementations. The factory has been updated to:
- Default to HttpProtocol for all protocol types
- Maintain a cache of protocol instances
- Provide a consistent approach to protocol creation across the framework

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

### Extending the HTTP Protocol

The framework now focuses on HTTP/HTTPS protocols for simplicity and maintainability. If you need to extend the protocol functionality:

```java
// Use the existing HttpProtocol as a base
public class EnhancedHttpProtocol extends HttpProtocol {
    @Override
    public Response execute(String endpoint, String method, String body,
                      Map<String, String> headers, Map<String, String> params,
                      Map<String, String> requestVariables) throws Exception {
        
        // Add pre-processing logic
        logRequest(endpoint, method, headers);
        
        // Call the parent implementation
        Response response = super.execute(endpoint, method, body, headers, params, requestVariables);
        
        // Add post-processing logic
        logResponse(response);
        
        return response;
    }
    
    private void logRequest(String endpoint, String method, Map<String, String> headers) {
        // Custom request logging
    }
    
    private void logResponse(Response response) {
        // Custom response logging
    }
}

// Usage
Protocol protocol = new EnhancedHttpProtocol();
```

This approach allows for customization while maintaining compatibility with the framework's core components.

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

### JMeter DSL Implementation

The framework uses JMeter DSL for test execution because it provides:
- Robust thread management
- Built-in metrics collection
- Proven stability and performance
- Standardized JTL reports compatible with JMeter
- Extensive protocol support

### YAML Configuration

YAML was selected for configuration files because:
- Human-readable format
- Support for comments
- Good representation of hierarchical data
- Wide adoption in configuration management

### Consolidated Protocol System

The protocol system has been consolidated to focus on HTTP/HTTPS:
- Simplified architecture with a single robust implementation
- Enhanced maintainability with less code duplication
- Improved stability by focusing on the most common use case
- Comprehensive support for all HTTP methods and features
- Extension point for additional HTTP functionality when needed

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