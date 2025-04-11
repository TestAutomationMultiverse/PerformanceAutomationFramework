# Migration Guide: perftest to ecs Package

This document provides guidance for migrating from the legacy `io.perftest` package to the new Entity Component System (ECS) architecture in the `io.ecs` package.

## Overview

The `io.ecs` package implements a proper Entity Component System (ECS) architecture, providing better separation of concerns, improved flexibility, and a flatter package structure. This migration is part of a broader effort to enhance the performance testing framework with a more modular and maintainable design.

## Key Changes

1. **Package Restructuring**:
   - All classes are now in the `io.ecs` package instead of `io.perftest`
   - Removed nested packages in favor of a flatter structure
   - Consolidated duplicated functionality

2. **ECS Architecture Improvements**:
   - `TestEntity` represents a test entity with components
   - Components (like `ProtocolComponent`, `ReportingComponent`) provide specific functionality
   - `EntityManager` handles entity creation and management
   - `TestExecutionSystem` processes entities with relevant components

3. **Request/Response Model**:
   - Migrated from `io.perftest.model.Request` to `io.ecs.model.Request`
   - Enhanced property handling for better type safety
   - Improved variable resolution in request templates

## Migration Steps

### 1. Update Import Statements

Replace all imports from the `io.perftest` package with their `io.ecs` equivalents:

```java
// Old
import io.perftest.model.Request;
import io.perftest.engine.Engine;
import io.perftest.core.TestResult;

// New
import io.ecs.model.Request;
import io.ecs.engine.Engine;
import io.ecs.model.TestResult;
```

### 2. Use New Factory Methods

Use the new factory methods and builders from the ECS package:

```java
// Old
Engine engine = io.perftest.engine.EngineFactory.createEngine("JMeter DSL", config);

// New
Engine engine = io.ecs.engine.EngineFactory.createEngine("JMeter DSL", config);
```

### 3. Update Object Creation

Create objects using the new package structure:

```java
// Old
Request request = new io.perftest.model.Request();

// New
Request request = new io.ecs.model.Request();
```

### 4. Leverage the ECS Architecture

Utilize the new ECS components for more flexible test configuration:

```java
// New ECS approach
TestEntity testEntity = new TestEntity("API Test");
testEntity.addComponent(new ProtocolComponent("http"));
testEntity.addComponent(new ReportingComponent(reportConfig));

EntityManager.getInstance().registerEntity(testEntity);
TestExecutionSystem.getInstance().executeTest(testEntity);
```

### 5. Use Conversion Utilities (During Transition)

During the transition period, you can use our conversion utilities to convert between packages:

```java
// Convert from io.perftest.model.Request to io.ecs.model.Request
io.perftest.model.Request legacyRequest = ...;
io.ecs.model.Request ecsRequest = io.ecs.util.RequestConverter.convert(legacyRequest);
```

## Example Migration

Here's a complete example showing the migration of a simple API test:

### Before (io.perftest)

```java
import io.perftest.model.Request;
import io.perftest.engine.Engine;
import io.perftest.engine.EngineFactory;
import io.perftest.model.TestResult;

// Create execution config
ExecutionConfig config = new io.perftest.model.ExecutionConfig();
config.setThreads(2);
config.setIterations(5);

// Initialize engine
Engine engine = EngineFactory.createEngine("JMeter DSL", config);

// Create request
Request request = new Request();
request.setName("Get User");
request.setProtocol("http");
request.setMethod("GET");
request.setEndpoint("https://api.example.com/users/1");

// Execute test
List<TestResult> results = engine.executeRequest(request);
```

### After (io.ecs)

```java
import io.ecs.model.Request;
import io.ecs.engine.Engine;
import io.ecs.engine.EngineFactory;
import io.ecs.model.TestResult;

// Create execution config
ExecutionConfig config = new io.ecs.model.ExecutionConfig();
config.setThreads(2);
config.setIterations(5);

// Initialize engine
Engine engine = EngineFactory.createEngine("JMeter DSL", config);

// Create request
Request request = new Request();
request.setName("Get User");
request.setProtocol("http");
request.setMethod("GET");
request.setEndpoint("https://api.example.com/users/1");

// Execute test
List<TestResult> results = engine.executeRequest(request);
```

## Deprecation Timeline

- **Current Phase**: Dual support for both `io.perftest` and `io.ecs` packages
- **Next Release**: `io.perftest` package will be marked as deprecated
- **Future Release**: `io.perftest` package will be removed completely

## Need Help?

If you encounter any issues during migration, please create an issue in the repository or contact the maintainers for assistance.