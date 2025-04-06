# Framework Architecture

The Performance Automation Framework is structured using the Entity-Component-System (ECS) architectural pattern, which provides a modular, extensible approach to performance testing.

## Entity-Component-System Pattern

The ECS pattern divides the framework into three main concepts:

- **Entities**: Represent the API requests (HTTP, GraphQL, SOAP, JDBC)
- **Components**: Process and handle requests for specific protocols
- **Systems**: Coordinate test execution, reporting, and assertions

This architectural approach provides:

1. **Modularity**: Each protocol is isolated in its own component
2. **Extensibility**: New protocols can be added with minimal changes to existing code
3. **Reusability**: Common functionality is shared across different request types
4. **Testability**: Components can be tested independently

## Core Components

### Entities

Entities represent the core data structures for different protocol requests. All request entities inherit from base classes that provide common functionality.

#### Request Entities

- **RequestEntity**: Base class for all request entities
- **HttpRequestEntity**: Represents HTTP/REST API requests
- **GraphQLRequestEntity**: Represents GraphQL API requests
- **SoapRequestEntity**: Represents SOAP/XML API requests
- **JdbcRequestEntity**: Represents JDBC database queries

#### Entity Structure

Each entity contains:

- Protocol-specific data (headers, queries, parameters)
- Common properties (name, description, tags)
- Metadata for reporting
- Assertion definitions

### Components

Components process specific protocol requests. Each component handles the conversion of request entities into executable JMeter test elements.

#### Protocol Components

- **HttpComponent**: Processes HTTP/REST API requests
- **GraphQLComponent**: Processes GraphQL API requests
- **XmlComponent**: Manages XML API requests
- **SoapComponent**: Processes SOAP API requests
- **JdbcComponent**: Handles database testing via JDBC

#### Component Responsibilities

Each component:

1. Validates request entities
2. Transforms entities into JMeter DSL objects
3. Applies protocol-specific settings and configurations
4. Handles protocol-specific error scenarios
5. Extracts and processes responses

### Systems

Systems coordinate high-level operations across entities and components.

#### Core Systems

- **TestEngine**: Orchestrates test execution and reporting
- **TestSystem**: Manages test lifecycle and coordination
- **ReportSystem**: Handles report generation and metrics collection
- **ConfigSystem**: Manages configuration and environment settings

#### Test Execution Flow

1. TestSystem initializes the test environment
2. Entities are created based on test configuration
3. Components process entities according to their protocol
4. TestEngine executes the test plan
5. ReportSystem collects and processes results
6. TestSystem finalizes the test and cleans up resources

## Entity Factory

The EntityFactory is a central component that creates request entities. It implements a factory pattern to:

1. Centralize entity creation logic
2. Apply default configurations
3. Validate entity properties
4. Create protocol-specific entities

### Entity Factory Implementations

- **HttpEntityFactory**: Creates HTTP request entities
- **GraphQLEntityFactory**: Creates GraphQL request entities
- **SoapEntityFactory**: Creates SOAP request entities
- **JdbcEntityFactory**: Creates JDBC request entities

### Entity Creation Process

The entity creation process follows these steps:

1. Client code requests an entity from the appropriate factory
2. Factory creates a base entity with default settings
3. Factory applies any custom configurations
4. Factory validates the entity's properties
5. Entity is returned ready for use in tests

## Exception Handling System

The framework includes a hierarchical exception handling system aligned with the ECS architecture:

- **EcsException**: Base exception class for all ECS-related exceptions
- **EntityException**: Exceptions related to entity creation and validation
- **ComponentException**: Exceptions occurring during component processing
- **SystemException**: Exceptions occurring in system-level operations
- **Result<T>**: Functional type for handling success and failure states
- **ErrorCode**: Enumeration of error codes organized by ECS component
- **ErrorHandler**: Utilities for handling and converting exceptions
- **ErrorReporter**: Centralized reporting of errors and warnings

### Error Handling Flow

1. Errors are captured at their source
2. Wrapped in appropriate exception types
3. Annotated with context and error codes
4. Propagated to appropriate handlers
5. Logged and reported centrally
6. Converted to user-friendly messages

## Configuration System

The framework uses a flexible configuration system:

- **YAML-based configuration**: Define test specifications in YAML files
- **Runtime configuration**: Modify test parameters programmatically
- **Environment overrides**: Override settings based on environment variables
- **Profiles**: Define environment-specific configurations

### Configuration Loading Process

1. Default configurations are loaded from resources
2. Environment-specific configurations are applied
3. Runtime overrides are merged
4. Final configuration is validated
5. Configuration is made available to all framework components

## Test Lifecycle

The framework defines a clear test lifecycle:

1. **Initialization**: Test environment setup and configuration loading
2. **Entity Creation**: Test entities are created based on configuration
3. **Component Processing**: Entities are processed by appropriate components
4. **Execution**: Tests are executed against target systems
5. **Results Collection**: Test results are collected and processed
6. **Reporting**: Reports and metrics are generated
7. **Cleanup**: Resources are released and environment is cleaned up
