# Performance Automation Framework

This project contains a comprehensive performance testing framework leveraging JMeter DSL for testing various API protocols. The framework follows the Entity-Component-System (ECS) architectural pattern and is designed to test HTTP, GraphQL, SOAP/XML services, and databases via JDBC.

[![Manual Performance Test](https://github.com/username/jmeter-dsl-performance-framework/actions/workflows/manual-performance-test.yml/badge.svg)](https://github.com/username/jmeter-dsl-performance-framework/actions/workflows/manual-performance-test.yml)
[![Build Status](https://github.com/username/jmeter-dsl-performance-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/username/jmeter-dsl-performance-framework/actions)
[![Static Analysis](https://github.com/username/jmeter-dsl-performance-framework/actions/workflows/static-analysis.yml/badge.svg)](https://github.com/username/jmeter-dsl-performance-framework/actions)
[![Release](https://img.shields.io/github/v/release/username/jmeter-dsl-performance-framework)](https://github.com/username/jmeter-dsl-performance-framework/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Recent Updates

- **Fixed ConfigManager Security Issue**: Addressed the MS_EXPOSE_REP vulnerability in ConfigManager using enum-based singleton pattern and SpotBugs exclude configuration
- **Resolved TestEngine Compilation Error**: Fixed error in TestEngine.java by replacing invalid getError() call with proper error code and message reporting
- **Enhanced Security**: Implemented defensive copying across multiple classes to address EI_EXPOSE_REP and EI_EXPOSE_REP2 vulnerabilities
- **Improved Documentation**: Updated security fixes documentation and added changelog
- **Verified Build Process**: Successfully validated all fixes with specialized workflows

## Overview

The Performance Automation Framework provides:

1. Multi-protocol support (HTTP, GraphQL, XML/SOAP, JDBC)
2. Entity-Component-System (ECS) architecture
3. YAML configuration-driven testing
4. Fluent Java DSL for test definition
5. Template-based request generation with Jinjava
6. Data generation with JavaFaker
7. Comprehensive JUnit integration
8. JSON and XPath-based response validation
9. Advanced performance metrics and reporting
10. Cross-protocol testing in a single test class
11. Real-time metrics visualization with Prometheus and Grafana
12. Advanced code quality tools integration:
    - SpotBugs for bug detection
    - Checkstyle for code style enforcement
    - JaCoCo for code coverage
13. CI/CD with GitHub Actions (automated tests, static analysis, and releases)
14. Comprehensive documentation with GitHub Pages

## Prerequisites

- Java 17+
- Maven 3.8+
- JMeter (handled automatically via JMeter DSL)

## Documentation

The framework documentation is organized into the following sections:

- [Code Quality](doc/code-quality.md) - Security fixes, code quality tools, and vulnerability management
- [Security Fixes Summary](doc/security-fixes-summary.md) - Comprehensive overview of security vulnerability fixes
- [Continuous Integration](doc/continuous-integration.md) - CI/CD workflows and automation
- [Framework Architecture](doc/framework-architecture.md) - ECS pattern, components, and entity factory
- [Example Tests](doc/example-tests.md) - Sample tests for each protocol (HTTP, GraphQL, JDBC, SOAP)

### Project Structure

```
├── config/                   # Configuration files
│   └── linting/              # Linting configuration
│       └── spotbugs-exclude.xml  # SpotBugs exclusion patterns
├── doc/                      # Documentation files
│   ├── code-quality.md       # Code quality documentation
│   ├── continuous-integration.md # CI/CD documentation
│   ├── framework-architecture.md # Architecture documentation
│   ├── example-tests.md      # Example tests documentation
│   └── entity-factory.md     # Entity Factory documentation
├── src/                      # Source code
│   ├── main/                 # Main source code
│   │   └── java/io/perftest/
│   │       ├── components/   # Protocol-specific components
│   │       ├── ecs/          # Entity-Component-System core
│   │       │   └── exception/# Exception handling framework
│   │       ├── engine/       # Test execution engine
│   │       ├── systems/      # System-level management
│   │       └── util/         # Utilities and helpers
│   └── test/                 # Test source code
│       └── java/io/perftest/
│           ├── core/         # Core framework tests
│           │   └── test/     # Base test classes and core tests
│           └── protocol/     # Protocol-specific tests
│               ├── http/     # HTTP/REST API tests
│               ├── graphql/  # GraphQL API tests
│               ├── jdbc/     # JDBC database tests
│               ├── soap/     # SOAP/XML tests
│               └── multi/    # Multi-protocol tests
└── target/                   # Build output
    ├── logs/                 # Test logs by protocol
    ├── jtl-results/          # JMeter JTL results
    ├── html-reports/         # HTML test reports
    └── spotbugs/             # SpotBugs reports
```

## Usage

### Using the Unified CLI Tool

The framework provides a unified command-line interface through the `perftest.sh` script, which provides a simpler way to run tests and manage reports.

```bash
# Check environment setup
./perftest.sh setup

# Build the project
./perftest.sh build

# Run different types of tests
./perftest.sh run simple              # Run a simple HTTP test
./perftest.sh run http                # Run HTTP protocol tests
./perftest.sh run graphql             # Run GraphQL protocol tests
./perftest.sh run jdbc                # Run JDBC protocol tests
./perftest.sh run soap                # Run SOAP protocol tests
./perftest.sh run all                 # Run all protocol tests
./perftest.sh run html-demo           # Run a simple HTML report demo
./perftest.sh run full-demo           # Run the full HTML report demo

# Generate and view reports
./perftest.sh report --view           # Start a server to view example reports
./perftest.sh report --generate --path=target/jtl-results/results.jtl  # Generate HTML report from JTL

# Clean the build
./perftest.sh clean

# Get help on available commands
./perftest.sh help
```

### Using Maven Directly

Alternatively, you can use Maven commands directly:

1. Navigate to the repository:

```bash
cd PerformanceAutomationFramework
```

2. Build the project:

```bash
mvn clean compile
```

3. Run the tests:

```bash
mvn test
```

4. Run a specific test:

```bash
# HTTP Tests
mvn test -Dtest=SimpleTest
mvn test -Dtest=CustomHttpTest
mvn test -Dtest=HttpYamlConfigTest
mvn test -Dtest=io.perftest.http_tests.K6PublicApiTest

# GraphQL Tests
mvn test -Dtest=GraphQLApiTest
mvn test -Dtest=GraphQLYamlConfigTest
mvn test -Dtest=io.perftest.graphql_tests.CountriesApiTest

# SOAP Tests
mvn test -Dtest=SoapYamlConfigTest

# JDBC Tests
mvn test -Dtest=SimpleJdbcTest
mvn test -Dtest=JdbcYamlConfigTest

# Multi-Protocol Tests
mvn test -Dtest=MultiProtocolTest
```

## Development Environment

This project includes a fully configured development environment using Visual Studio Code devcontainers. The devcontainer setup provides:

### Features

- Java 17 with Maven 3.9.5
- Node.js 20.x (for potential future front-end components)
- Apache JMeter 5.6.2 with essential plugins
- Docker-in-Docker for running Prometheus and Grafana
- Pre-configured code quality tools

### IDE Extensions

The development container automatically installs VS Code extensions for:

- Java development and debugging
- XML/YAML editing support
- Maven integration
- Docker management
- Code quality tools (SonarLint, Checkstyle, SpotBugs)
- GitHub integration (Actions, PR reviews)
- Markdown linting

### Getting Started with Devcontainer

1. Install [Visual Studio Code](https://code.visualstudio.com/)
2. Install the [Remote Development Extension Pack](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.vscode-remote-extensionpack)
3. Clone this repository
4. Open the repository folder in VS Code
5. When prompted, click "Reopen in Container"
   - Or use Command Palette (F1): "Remote-Containers: Reopen in Container"
6. Wait for the container to build and initialize

Once the container is ready, you'll have a fully configured environment with all necessary tools and dependencies.

### Running Tests in the Development Environment

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=HttpYamlConfigTest

# Run tests with JaCoCo coverage
mvn test jacoco:report
```

### Viewing Reports

After running tests, various reports are available:

- JaCoCo coverage: `target/site/jacoco/index.html`
- JMeter HTML reports: `target/html-reports/[protocol]/index.html`
- SpotBugs reports: `target/spotbugs/spotbugs.html`

### What's next

- remove all old html repots and accociated code only keep unified-reports
- Update code to support different http methods like get, post, put, delete for http protocol
- Multi-protocol support (HTTP, GraphQL, SOAP/XML, JDBC)
- Entity-Component-System (ECS) architecture
- YAML/Data configuration-driven testing
- Unified CLI tool (perftest.sh)
- Advanced reporting capabilities
- JMeter DSL integration
- Comprehensive JUnit support
- Enhanced error reporting
- fix pom.xml package conflicts
- GraphQLYamlConfigTest. testGraphQLWithYamlConfig:53->createGraphQLRequestEntity:110->lambda$createGraphQLRequestEntity$0:113 » UnsupportedOperation
