# Entity Factory

## Overview

The `EntityFactory` class centralizes the creation of request entities in the Performance Automation Framework. It is an implementation of the Factory design pattern that fits within the Entity-Component-System (ECS) architecture.

## Purpose

The primary goals of the EntityFactory are:

1. **Encapsulate entity creation logic** - Move entity creation out of test classes
2. **Standardize configuration** - Ensure consistent entity configuration across tests
3. **Simplify test code** - Make tests more concise and focused on test scenarios
4. **Support ECS architecture** - Maintain separation between entities, components, and systems

## Usage Examples

### Creating HTTP Entities

#### From YAML Config
```java
// Load configuration from YAML file
Map<String, Object> config = YamlConfigLoader.loadConfig("http-config.yml");

// Create HTTP entity from config
HttpRequestEntity request = EntityFactory.createHttpEntity(config);
```

#### Simple HTTP Request
```java
// Create a simple GET request
HttpRequestEntity request = EntityFactory.createHttpEntity("https://api.example.com/users");

// Create a POST request with body
HttpRequestEntity postRequest = EntityFactory.createHttpEntity(
    "https://api.example.com/users",
    "POST",
    "{\"name\": \"John\", \"email\": \"john@example.com\"}"
);
```

### Creating GraphQL Entities

```java
// Create a GraphQL request
GraphQLRequestEntity request = EntityFactory.createGraphQLEntity(
    "https://graphql.example.com",
    "query { users { id name email } }"
);
```

### Creating JDBC Entities

```java
// Create a JDBC request
JdbcRequestEntity request = EntityFactory.createJdbcEntity(
    "jdbc:postgresql://localhost:5432/testdb",
    "postgres",
    "postgres",
    "SELECT * FROM users WHERE active = true"
);
```

## Integration with Test Engine

The EntityFactory works seamlessly with the TestEngine and TestSystem:

```java
// Create test system with components
TestSystem testSystem = new TestSystem();
testSystem.addComponent(HttpRequestEntity.class, new HttpComponent());

// Configure test engine
TestEngine engine = new TestEngine(testSystem);
engine.setThreads(10);
engine.setIterations(5);

// Create and add HTTP request using EntityFactory
HttpRequestEntity request = EntityFactory.createHttpEntity("https://api.example.com/users");
engine.addRequest(request);

// Run the test
TestPlanStats stats = engine.run();
```

## Benefits

- **Cleaner test code** - Tests focus on what to test, not how to create entities
- **Consistent configuration** - Entities are created with standard defaults
- **Better maintainability** - Changes to entity creation are centralized in one place
- **Enhanced reusability** - Factory methods can be used across multiple tests
- **Proper ECS implementation** - Maintains separation of concerns between entities, components, and systems

## Implementation Details

The `EntityFactory` class is implemented as a utility class with static methods for creating different types of request entities. Each creation method follows a similar pattern:

1. Create a new entity instance
2. Set default values and configurations
3. Apply custom configurations from parameters or YAML
4. Return the fully configured entity

### Core Factory Methods

```java
public class EntityFactory {
    
    // HTTP Entity Factory Methods
    public static HttpRequestEntity createHttpEntity(String endpoint) { ... }
    public static HttpRequestEntity createHttpEntity(String endpoint, String method) { ... }
    public static HttpRequestEntity createHttpEntity(String endpoint, String method, String body) { ... }
    public static HttpRequestEntity createHttpEntity(Map<String, Object> config) { ... }
    
    // GraphQL Entity Factory Methods
    public static GraphQLRequestEntity createGraphQLEntity(String endpoint, String query) { ... }
    public static GraphQLRequestEntity createGraphQLEntity(Map<String, Object> config) { ... }
    
    // JDBC Entity Factory Methods
    public static JdbcRequestEntity createJdbcEntity(String url, String user, String password, String query) { ... }
    public static JdbcRequestEntity createJdbcEntity(Map<String, Object> config) { ... }
    
    // SOAP/XML Entity Factory Methods
    public static SoapRequestEntity createSoapEntity(String endpoint, String soapAction, String body) { ... }
    public static SoapRequestEntity createSoapEntity(Map<String, Object> config) { ... }
    
    // Helper methods for common configuration
    private static void applyCommonConfig(RequestEntity entity, Map<String, Object> config) { ... }
    private static void applyHeaders(RequestEntity entity, Map<String, String> headers) { ... }
    private static void applyAssertions(RequestEntity entity, Map<String, Object> assertions) { ... }
}
```

## Advanced Usage

### Adding Custom Headers

```java
HttpRequestEntity request = EntityFactory.createHttpEntity("https://api.example.com/users");
request.addHeader("Authorization", "Bearer " + token);
request.addHeader("Content-Type", "application/json");
```

### Adding Response Assertions

```java
HttpRequestEntity request = EntityFactory.createHttpEntity("https://api.example.com/users");
request.addAssertion("$.status", "success");
request.addAssertion("$.data[0].id", id -> Integer.parseInt(id) > 0);
request.addAssertion("$.data", array -> array.length() > 5);
```

### Adding Parameterized Queries

```java
GraphQLRequestEntity request = EntityFactory.createGraphQLEntity(
    "https://graphql.example.com",
    "query GetUser($id: ID!) { user(id: $id) { name email } }"
);
request.addVariable("id", "12345");
```

### Using Templates with Entities

```java
// Load template from file
String template = TemplateLoader.loadTemplate("user-query.graphql");

// Create entity with template
GraphQLRequestEntity request = EntityFactory.createGraphQLEntity("https://graphql.example.com", template);

// Add template variables
Map<String, Object> context = new HashMap<>();
context.put("userId", "12345");
context.put("fields", Arrays.asList("name", "email", "address"));

// Process template
request.processTemplate(context);
```

## Testing Multiple Protocols

The EntityFactory supports creating entities for different protocols, making it easy to test systems that use multiple API types:

```java
@Test
public void testMultiProtocolWorkflow() throws IOException {
    // Create HTTP entity for authentication
    HttpRequestEntity authRequest = EntityFactory.createHttpEntity(
        "https://api.example.com/auth",
        "POST",
        "{\"username\": \"admin\", \"password\": \"password\"}"
    );
    authRequest.setName("Authentication");
    authRequest.addAssertion("$.token", token -> token != null && !token.isEmpty());
    
    // Run auth request
    TestPlanStats authStats = testEngine.executeHttpTest(authRequest, 1, 1);
    String token = getTokenFromResponse(authStats);
    
    // Create GraphQL entity using the auth token
    GraphQLRequestEntity graphqlRequest = EntityFactory.createGraphQLEntity(
        "https://api.example.com/graphql",
        "query { currentUser { id name permissions } }"
    );
    graphqlRequest.setName("Current User Query");
    graphqlRequest.addHeader("Authorization", "Bearer " + token);
    
    // Run GraphQL request
    TestPlanStats graphqlStats = testEngine.executeGraphQLTest(graphqlRequest, 1, 1);
    
    // Create JDBC entity to verify database state
    JdbcRequestEntity jdbcRequest = EntityFactory.createJdbcEntity(
        "jdbc:postgresql://localhost:5432/testdb",
        "postgres",
        "postgres",
        "SELECT last_login FROM users WHERE username = 'admin'"
    );
    jdbcRequest.setName("Verify Login Timestamp");
    
    // Run JDBC request
    TestPlanStats jdbcStats = testEngine.executeJdbcTest(jdbcRequest, 1, 1);
}
```

## Best Practices

When using the EntityFactory, follow these best practices:

1. **Use configuration files for complex tests** - YAML configuration files are more maintainable for complex test scenarios
2. **Create entities in setup methods** - For reusable entities across multiple test methods
3. **Customize entities after creation** - Use factory methods for basic creation, then customize for specific test needs
4. **Use meaningful entity names** - Set descriptive names for better test reports and debugging
5. **Separate assertions from entity creation** - Add assertions after creation for clearer test logic
6. **Leverage template processing** - Use templates for complex or repetitive request bodies
7. **Reuse entities when possible** - Clone and modify existing entities for similar tests

## Future Enhancements

Planned enhancements for the EntityFactory include:

1. Support for additional protocols (gRPC, WebSockets)
2. Enhanced template processing with more variable types
3. Entity validation to catch configuration errors early
4. Integration with data generation libraries for dynamic test data
5. Support for complex assertion chains and response processing

