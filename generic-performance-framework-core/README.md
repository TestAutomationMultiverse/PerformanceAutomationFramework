# Generic Performance Framework Core

This module contains the core functionality of the Generic Performance Framework, a comprehensive solution for performance testing with JMeter and Gatling integration.

## Features

- Multi-protocol support (HTTP, HTTPS)
- YAML configuration for test scenarios
- Simplified path resolution for templates and resources
- Integration with JMeter Tree Builder and JMeter DSL
- Integration with Gatling for performance testing
- Detailed metrics collection and reporting

## Using as a Dependency

### Add GitHub Packages Repository

To use this library in your project, first add the GitHub Packages repository to your Maven settings or project POM:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/yourusername/generic-performance-framework</url>
    </repository>
</repositories>
```

### Add Dependency

Then add the dependency to your project:

```xml
<dependency>
    <groupId>io.ecs</groupId>
    <artifactId>generic-performance-framework-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Building from Source

To build the project from source:

```bash
mvn clean install
```

## Documentation

For detailed documentation, refer to the [main project documentation](../docs/).