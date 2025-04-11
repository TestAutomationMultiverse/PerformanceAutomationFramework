# Generic Performance Framework

A comprehensive Performance Testing Automation Framework for Java applications supporting multiple performance testing engines including JMeter and Gatling.

## Overview

The Generic Performance Framework is a flexible, extensible Java-based solution for automating performance tests across different testing engines. It provides a unified interface for defining test scenarios, executing performance tests, and collecting metrics regardless of the underlying testing tool.

This project has been structured as a multi-module Maven project:

- **generic-performance-framework-core**: Core library that can be used as a dependency in your projects
- **generic-performance-framework-tests**: Test module with examples of using the core library

### Key Features

- **Multiple Engine Support**: Run tests through JMeter or Gatling using the same test definitions
- **YAML Configuration**: Define test scenarios in easy-to-read YAML files
- **Simplified Path Resolution**: Use just filenames instead of full paths in your test configurations
- **Component-Based Architecture**: Easily extend with new protocols, engines, or reporting capabilities
- **Data-Driven Testing**: Integrate with CSV data sources for parameterized tests
- **Comprehensive Reporting**: Collect and analyze performance metrics in a standardized format

## Quick Start

1. **Clone the repository**:
   ```bash
   git clone https://github.com/TestAutomationMultiverse/PerformanceAutomationFramework.git
   cd PerformanceAutomationFramework
   ```

2. **Compile the project**:
   ```bash
   mvn clean compile
   ```

3. **Run a test using predefined YAML configuration**:
   ```bash
   mvn exec:java -Dexec.mainClass="io.ecs.system.MainRunner" -Dexec.args="src/test/resources/configs/gatling_config.yaml"
   ```

## Available Engines

- **JMeter Tree Builder**: Create and execute JMeter test plans using a tree-based approach
- **JMeter DSL**: Use the JMeter DSL to programmatically define and execute tests
- **Gatling**: Execute Gatling simulations with the same test definitions

## YAML Configuration

Define your test scenarios in YAML for a simple, readable test definition:

```yaml
# Enhanced Performance Test Configuration
protocol: http
engine: JMDSL  # Can be JMDSL, JMTREE, or GATLING

# Global variables
variables:
  baseUrl: https://jsonplaceholder.typicode.com
  timeout: 30000
  contentType: application/json

# Execution configuration
execution:
  threads: 5
  iterations: 10
  rampUpSeconds: 2
  holdSeconds: 5
  duration: 60

# Test scenarios
scenarios:
  - name: HTTP API Test
    requests:
      - name: "Get Users"
        endpoint: "https://jsonplaceholder.typicode.com/users"
        method: "GET"
        headers: default_headers.json
```

## Simplified Path Resolution

The framework supports simplified path references in your YAML configurations. Instead of specifying full paths like:

```yaml
headers: src/test/resources/templates/http/headers/default_headers.json
```

You can simply use:

```yaml
headers: default_headers.json
```

The framework will automatically resolve the appropriate path based on file naming conventions and extensions.

## Using as a Dependency

### Add GitHub Packages Repository

To use this framework in your project, add the GitHub Packages repository to your Maven settings or project POM:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/TestAutomationMultiverse/GenericPerformanceFramework</url>
    </repository>
</repositories>
```

### Add Dependency

Then add the core module as a dependency:

```xml
<dependency>
    <groupId>io.ecs</groupId>
    <artifactId>generic-performance-framework-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Authentication with GitHub Packages

To authenticate with GitHub Packages, you'll need to add your GitHub credentials to your Maven `settings.xml` file:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password>
    </server>
</servers>
```

The GitHub token should have the `read:packages` scope for downloading packages. For more information, see [GitHub Packages Documentation](https://docs.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages).

## Documentation

For more detailed information, please refer to the documentation files in the `docs` directory:

- [Architecture Overview](docs/ARCHITECTURE.md)
- [Comprehensive Guide](docs/COMPREHENSIVE_GUIDE.md)
- [Quick Start Guide](docs/QUICKSTART.md)
- [YAML Configuration Reference](docs/YAML_CONFIG.md)
- [Extending the Framework](docs/EXTENDING.md)
- [JMeter Tree Builder Guide](docs/TREEBUILDER.md)
- [Migration Guide](docs/MIGRATION.md)
- [Usage Examples](docs/USAGE.md)

## Development Scripts

The project includes utility scripts to help with development and maintenance:

### Clone and Fix Script

This script (`clone_and_fix.py`) clones the repository if it doesn't exist locally and fixes several build issues:

- Fixes circular imports in runner classes
- Updates the TestResult class with missing methods
- Fixes the GatlingEngine implementation to properly implement the Engine interface
- Ensures proper path resolution in YAML configurations
- Updates Maven POM files with correct dependencies

To run the script:

```bash
python clone_and_fix.py
```

### Update Structure Script

This script (`update_structure.py`) updates the project to a multi-module Maven structure:

- Creates the multi-module directory structure
- Moves source code to appropriate modules
- Updates POM files for the new structure
- Creates GitHub Actions workflow files
- Builds the multi-module project

To run the script:

```bash
python update_structure.py
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
