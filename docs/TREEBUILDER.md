# JMeter TreeBuilder API Integration (Experimental)

This document describes the experimental JMeter TreeBuilder API integration in the Performance Testing Framework.

## Overview

The JMeter TreeBuilder implementation provides a way to create programmatic JMeter test plans using the standard JMeter API. This approach gives direct access to JMeter's internal components and allows for more complex test scenarios than the DSL approach.

**Note:** This feature is experimental and is based on the [JMeter Programmatic API](https://jmeter.apache.org/usermanual/build-programmatic-test-plan.html).

## Key Components

### JMTreeBuilderEngine

This engine implements the `Engine` interface and provides JMeter test execution capabilities through the standard JMeter API:

- Creates test plans programmatically using JMeter's internal components
- Allows direct manipulation of JMeter elements like ThreadGroups, Controllers, and Samplers
- Uses the same reporting and metrics infrastructure as other engines

### Test Execution Flow

1. Create JMeter test elements (TestPlan, ThreadGroup, LoopController, HTTPSamplerProxy)
2. Assemble them into a test plan tree using ListedHashTree structure
3. Configure elements with thread count, iterations, test endpoints, etc.
4. Execute the test and collect results
5. Generate reports in JTL format

## Using the TreeBuilder Engine

### Basic Usage

```java
// Create execution configuration
ExecutionConfig config = new ExecutionConfig();
config.setThreads(10);
config.setIterations(5);

// Create the engine
Engine engine = new JMTreeBuilderEngine(config);

// Set global variables
Map<String, String> variables = new HashMap<>();
variables.put("baseUrl", "https://api.example.com");
engine.initialize(variables);

// Create requests
List<Request> requests = new ArrayList<>();
Request getRequest = new Request();
getRequest.setName("Get User");
getRequest.setMethod("GET");
getRequest.setEndpoint("${baseUrl}/users/1");
requests.add(getRequest);

// Execute scenario
List<TestResult> results = engine.executeScenario("My Scenario", requests);

// Shutdown
engine.shutdown();
```

### JMeterTreeBuilderTest Example

The `JMeterTreeBuilderTest` class demonstrates a complete example of using the TreeBuilder engine:

```bash
mvn clean compile exec:java -Dexec.mainClass="io.perftest.JMeterTreeBuilderTest"
```

This will execute a test scenario with three requests (GET, GET, POST) using the TreeBuilder engine.

## Advantages over JMeter DSL

While the JMeter DSL provides a simpler interface, the TreeBuilder approach offers:

1. **More control** - Direct access to JMeter components
2. **Advanced features** - Support for complex test plans with listeners, assertions, etc.
3. **Extended test elements** - Use any JMeter test element, not just those exposed by the DSL
4. **Custom logic** - Insert custom processing between test steps

## JTL File Generation

The TreeBuilder engine uses the same JTL file generation system as other engines to maintain compatibility. Test results are stored in the standard JMeter JTL format:

```
timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
```

## Using with YAML Configuration

While designed for programmatic use, the TreeBuilder engine can also be used with YAML configuration files through the App class:

```yaml
engineType: jmeter-treebuilder
executionConfig:
  threads: 5
  iterations: 10
  rampUpSeconds: 2
  holdSeconds: 3

scenarios:
  - name: API Test
    requests:
      - name: Get User
        protocol: http
        method: GET
        endpoint: ${baseUrl}/users/1
```

```bash
mvn exec:java -Dexec.mainClass="io.perftest.App" -Dexec.args="path/to/config.yaml"
```

## Limitations and Considerations

- This is an experimental feature that may change in future releases
- Some advanced JMeter features may require additional implementation
- Uses simulated execution mode for demonstration purposes
- JMeter Props directory must be available at src/main/resources/jmeter-props

## References

- [JMeter Website](https://jmeter.apache.org/)
- [JMeter API Documentation](https://jmeter.apache.org/api/index.html)
- [Programmatic JMeter Plans](https://jmeter.apache.org/usermanual/build-programmatic-test-plan.html)