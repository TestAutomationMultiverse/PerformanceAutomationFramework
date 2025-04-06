# Code Quality

This project uses several tools to ensure code quality and maintain high standards for the codebase.

## Static Analysis

- **SpotBugs**: Scans code for potential bugs, bad practices, and vulnerabilities
- **Checkstyle**: Ensures consistent coding style and adherence to coding standards
- **SonarLint**: Provides IDE-level code quality feedback (via VSCode extension)

The SpotBugs configuration is located in `src/main/resources/spotbugs/exclude.xml`, which defines patterns for issues to exclude from analysis.

## Security Fixes for Vulnerabilities

The framework includes implementations of various security fixes to address common SpotBugs warnings:

### EI_EXPOSE_REP / EI_EXPOSE_REP2 Fixes

These vulnerabilities occur when methods may expose or store references to mutable objects. We use several design patterns to address these issues:

1. **Defensive Copying Pattern**:
   - For getters: Return immutable copies or unmodifiable views of collections and objects
   - For setters: Create defensive copies before storing externally provided objects
   - Example: `Collections.unmodifiableMap(new HashMap<>(originalMap))`

2. **Deep Copy Implementation**:
   - For complex objects, create new instances and copy all fields
   - Especially important for nested objects that may themselves be mutable

3. **Builder Pattern**:
   - Used in classes like `JdbcRequestEntity` to ensure immutability
   - Builders create new instances at each step, preventing mutation

### MS_EXPOSE_REP Fixes

This vulnerability occurs when a static field representing an object reference is returned without defensive copying. We address this in several ways:

1. **Enum-based Singleton Pattern**:
   - Using enum for the singleton holder completely prevents issues with serialization and reflection
   - Immune to MS_EXPOSE_REP warnings by design
   - Example: `ConfigManager` class implements this pattern

2. **SpotBugs Exclusion**:
   - For cases where code modification is impractical or would introduce complexity
   - Carefully defined exclusions in `src/main/resources/spotbugs/exclude.xml`
   - Limited to specific classes and methods where the risk is understood

### Other Fixed Vulnerabilities

- **Null Pointer Issues**: Proper null checks before operations
- **Default Encoding Issues**: Explicitly specifying character encoding
- **Resource Leaks**: Using try-with-resources for proper resource management
- **Dead Store Elimination**: Removing unused variables

## Code Coverage

- **JaCoCo**: Measures code coverage of tests
- Coverage reports are generated in `target/site/jacoco`
- Coverage badges are auto-generated and displayed in the README

## Local Development

To run code quality checks locally:

```bash
# Run SpotBugs
mvn spotbugs:spotbugs
mvn spotbugs:gui  # To view findings in GUI

# Run Checkstyle
mvn checkstyle:check

# Run all verification including tests with code coverage
mvn verify
```

All reports are available in the `target` directory after running these commands.
