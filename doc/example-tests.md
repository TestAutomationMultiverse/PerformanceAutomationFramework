# Example Tests

The Performance Automation Framework includes a variety of example tests that demonstrate how to use the framework with different protocols.

## HTTP Protocol Tests

### Simple HTTP Test

This test demonstrates the basic functionality of the HTTP component:

```java
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.engine.TestEngine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleHttpTest {
    @Test
    public void testHttpGetRequest() {
        // Create an HTTP request entity
        HttpRequestEntity entity = new HttpRequestEntity();
        entity.setName("Simple HTTP GET");
        entity.setUrl("https://httpbin.org/get");
        entity.setMethod("GET");
        
        // Add headers
        entity.addHeader("Content-Type", "application/json");
        entity.addHeader("Accept", "application/json");
        
        // Add assertions
        entity.addAssertion("status", "200");
        entity.addAssertion("contains", "headers");
        
        // Execute the test
        TestEngine engine = new TestEngine();
        TestResult result = engine.execute(entity);
        
        // Verify results
        assertTrue(result.isSuccess());
        assertEquals(200, result.getStatusCode());
        assertTrue(result.getBody().contains("headers"));
    }
}
```

### K6 Public API Test

This test demonstrates testing a public API:

```java
import io.perftest.core.test.BaseTest;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.engine.TestResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class K6PublicApiTest extends BaseTest {
    @Test
    public void testPublicApi() {
        // Create request entity
        HttpRequestEntity entity = httpEntityFactory.createEntity();
        entity.setName("K6 Public API Test");
        entity.setUrl("https://test-api.k6.io/public/crocodiles/");
        entity.setMethod("GET");
        
        // Execute test
        TestResult result = engine.execute(entity);
        
        // Verify results
        assertTrue(result.isSuccess());
        assertEquals(200, result.getStatusCode());
        assertTrue(result.getBody().contains("crocodiles"));
    }
}
```

## GraphQL Protocol Tests

### Simple GraphQL Test

This test demonstrates the basic functionality of the GraphQL component:

```java
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.engine.TestEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleGraphQLTest {
    @Test
    public void testGraphQLQuery() {
        // Create a GraphQL request entity
        GraphQLRequestEntity entity = new GraphQLRequestEntity();
        entity.setName("Countries GraphQL Query");
        entity.setUrl("https://countries.trevorblades.com/");
        entity.setQuery("query { countries { code name } }");
        
        // Add assertions
        entity.addAssertion("status", "200");
        entity.addAssertion("json_path", "$.data.countries[0].code");
        
        // Execute the test
        TestEngine engine = new TestEngine();
        TestResult result = engine.execute(entity);
        
        // Verify results
        assertTrue(result.isSuccess());
        assertTrue(result.getBody().contains("countries"));
    }
}
```

### Countries API Test

This test demonstrates querying a GraphQL countries API:

```java
import io.perftest.core.test.BaseTest;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.engine.TestResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountriesApiTest extends BaseTest {
    @Test
    public void testCountriesApi() {
        // Create request entity
        GraphQLRequestEntity entity = graphqlEntityFactory.createEntity();
        entity.setName("Countries API Test");
        entity.setUrl("https://countries.trevorblades.com/");
        entity.setQuery("query { continents { code name } }");
        
        // Execute test
        TestResult result = engine.execute(entity);
        
        // Verify results
        assertTrue(result.isSuccess());
        assertEquals(200, result.getStatusCode());
        assertTrue(result.getBody().contains("continents"));
    }
}
```

## JDBC Protocol Tests

### Simple JDBC Test

This test demonstrates the basic functionality of the JDBC component:

```java
import io.perftest.entities.request.JdbcRequestEntity;
import io.perftest.engine.TestEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleJdbcTest {
    @Test
    public void testJdbcQuery() {
        // Create a JDBC request entity
        JdbcRequestEntity entity = new JdbcRequestEntity();
        entity.setName("Simple H2 Query");
        entity.setUrl("jdbc:h2:mem:testdb");
        entity.addQuery("CREATE TABLE users (id INT, name VARCHAR(255))");
        entity.addQuery("INSERT INTO users VALUES (1, 'John')");
        entity.addQuery("SELECT * FROM users");
        
        // Execute the test
        TestEngine engine = new TestEngine();
        TestResult result = engine.execute(entity);
        
        // Verify results
        assertTrue(result.isSuccess());
        assertTrue(result.getRows() > 0);
    }
}
```

### JdbcConfig Test

This test demonstrates using YAML configuration for JDBC tests:

```java
import io.perftest.config.JdbcConfig;
import io.perftest.core.test.BaseTest;
import io.perftest.entities.request.JdbcRequestEntity;
import io.perftest.engine.TestResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcYamlConfigTest extends BaseTest {
    @Test
    public void testJdbcWithYamlConfig() {
        // Load configuration from YAML
        JdbcConfig config = configManager.loadFromYaml("config/jdbc/h2-test.yml", JdbcConfig.class);
        
        // Create request entity from configuration
        JdbcRequestEntity entity = jdbcEntityFactory.createEntityFromConfig(config);
        
        // Execute test
        TestResult result = engine.execute(entity);
        
        // Verify results
        assertTrue(result.isSuccess());
        assertTrue(result.getRows() > 0);
    }
}
```

## SOAP Protocol Tests

### SOAP XML Test

This test demonstrates the basic functionality of the SOAP component:

```java
import io.perftest.entities.request.SoapRequestEntity;
import io.perftest.engine.TestEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleSoapTest {
    @Test
    public void testSoapRequest() {
        // Create a SOAP request entity
        SoapRequestEntity entity = new SoapRequestEntity();
        entity.setName("SOAP Calculator Add");
        entity.setUrl("http://www.dneonline.com/calculator.asmx");
        entity.setSoapAction("http://tempuri.org/Add");
        
        // Set SOAP envelope
        String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">" +
                          "   <soapenv:Header/>" +
                          "   <soapenv:Body>" +
                          "      <tem:Add>" +
                          "         <tem:intA>5</tem:intA>" +
                          "         <tem:intB>3</tem:intB>" +
                          "      </tem:Add>" +
                          "   </soapenv:Body>" +
                          "</soapenv:Envelope>";
        entity.setEnvelope(envelope);
        
        // Add assertions
        entity.addAssertion("status", "200");
        entity.addAssertion("xpath", "//AddResult");
        
        // Execute the test
        TestEngine engine = new TestEngine();
        TestResult result = engine.execute(entity);
        
        // Verify results
        assertTrue(result.isSuccess());
        assertTrue(result.getBody().contains("<AddResult>8</AddResult>"));
    }
}
```

## Multi-Protocol Tests

### Combined Protocol Test

This test demonstrates using multiple protocols in a single test:

```java
import io.perftest.core.test.BaseTest;
import io.perftest.entities.request.*;
import io.perftest.engine.TestResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiProtocolTest extends BaseTest {
    @Test
    public void testMultipleProtocols() {
        // Create HTTP request
        HttpRequestEntity httpEntity = httpEntityFactory.createEntity();
        httpEntity.setName("HTTP Request");
        httpEntity.setUrl("https://httpbin.org/get");
        httpEntity.setMethod("GET");
        
        // Create GraphQL request
        GraphQLRequestEntity graphqlEntity = graphqlEntityFactory.createEntity();
        graphqlEntity.setName("GraphQL Request");
        graphqlEntity.setUrl("https://countries.trevorblades.com/");
        graphqlEntity.setQuery("query { continents { code name } }");
        
        // Execute tests
        TestResult httpResult = engine.execute(httpEntity);
        TestResult graphqlResult = engine.execute(graphqlEntity);
        
        // Verify results
        assertTrue(httpResult.isSuccess());
        assertTrue(graphqlResult.isSuccess());
    }
}
```

## HTML Reporting Demo

This test demonstrates generating HTML reports:

```java
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.engine.TestEngine;
import io.perftest.util.JtlToHtmlReportConverter;
import org.junit.jupiter.api.Test;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class HtmlReportDemo {
    @Test
    public void testWithHtmlReport() throws Exception {
        // Create HTTP request entity
        HttpRequestEntity entity = new HttpRequestEntity();
        entity.setName("HTTP Request for Report");
        entity.setUrl("https://httpbin.org/get");
        entity.setMethod("GET");
        entity.setThreads(5);
        entity.setIterations(10);
        
        // Create test engine with HTML reporting
        TestEngine engine = new TestEngine();
        engine.setGenerateHtmlReport(true);
        engine.setReportOutputPath("target/html-reports/demo");
        
        // Execute test
        engine.execute(entity);
        
        // The HTML report is automatically generated
    }
}
```

## Configuration Examples

### YAML Configuration

Example YAML configuration for an HTTP test:

```yaml
# config/http/api-test.yml
name: API Test Configuration
url: https://httpbin.org/get
method: GET
headers:
  Content-Type: application/json
  Accept: application/json
parameters:
  param1: value1
  param2: value2
assertions:
  status: 200
  contains: headers
  json_path: $.headers
testSettings:
  threads: 10
  iterations: true
  iterationCount: 100
  rampUp: 30s
  duration: 5m
```

Example YAML configuration for a JDBC test:

```yaml
# config/jdbc/h2-test.yml
connection:
  jdbcUrl: jdbc:h2:mem:testdb
  username: sa
  password: 
  driverClass: org.h2.Driver
  connectionPoolSize: 10
  validateConnection: true
queries:
  - name: Create Table
    sql: CREATE TABLE users (id INT, name VARCHAR(255))
  - name: Insert Data
    sql: INSERT INTO users VALUES (?, ?)
    parameters:
      - 1
      - John
  - name: Select Data
    sql: SELECT * FROM users
testSettings:
  threads: 5
  iterations: true
  iterationCount: 20
  duration: 1m
```
