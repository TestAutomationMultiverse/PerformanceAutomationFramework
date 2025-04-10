# YAML Configuration Reference

This document provides a complete reference for the YAML configuration format used by the Java Performance Testing Framework.

## Table of Contents

1. [Basic Structure](#basic-structure)
2. [Execution Configuration](#execution-configuration)
3. [Variables](#variables)
4. [Scenarios](#scenarios)
5. [Requests](#requests)
6. [Data-Driven Testing](#data-driven-testing)
7. [Complete Example](#complete-example)

## Basic Structure

A typical YAML configuration file has the following structure:

```yaml
# Global execution configuration
executionConfig:
  threads: 5
  iterations: 10
  rampUpSeconds: 2
  holdSeconds: 3

# Global variables
variables:
  baseUrl: https://api.example.com
  userId: 123
  apiKey: my-api-key

# Default protocol
protocolName: http

# Test scenarios
scenarios:
  - name: Scenario 1
    variables:
      scenarioVar: value1
    requests:
      - name: Request 1
        protocol: http
        method: GET
        endpoint: /users
        
  - name: Scenario 2
    variables:
      scenarioVar: value2
    requests:
      - name: Request 1
        protocol: http
        method: POST
        endpoint: /users
        body: '{"name": "Test User"}'
```

## Execution Configuration

The `executionConfig` section defines the global performance test parameters:

```yaml
executionConfig:
  threads: 5         # Number of concurrent threads (virtual users)
  iterations: 10     # Number of iterations per thread
  rampUpSeconds: 2   # Time to ramp up to full thread count
  holdSeconds: 3     # Time to hold at full thread count
```

Each scenario can also override these settings with its own values.

## Variables

Variables allow you to define values that can be reused throughout the configuration. 
They are defined at different levels:

### Global Variables

```yaml
variables:
  baseUrl: https://api.example.com
  userId: 123
  apiKey: my-api-key
  timeout: 5000
```

### Scenario Variables

```yaml
scenarios:
  - name: Scenario 1
    variables:
      scenarioVar: value1
      userId: 456  # Overrides global userId
```

### Request Variables

```yaml
requests:
  - name: Request 1
    variables:
      requestVar: value1
      userId: 789  # Overrides scenario and global userId
```

### Variable Substitution

Variables can be referenced in the configuration using the `${variable}` syntax:

```yaml
requests:
  - name: Get User
    method: GET
    endpoint: ${baseUrl}/users/${userId}
    headers:
      Authorization: Bearer ${apiKey}
```

## Scenarios

A scenario represents a group of related requests that are executed together:

```yaml
scenarios:
  - name: User API Test         # Scenario name
    engine: jmdsl               # Engine to use (jmdsl or custom)
    threads: 5                  # Override global thread count
    iterations: 10              # Override global iterations
    rampUp: 2                   # Override global ramp-up time
    hold: 3                     # Override global hold time
    variables:                  # Scenario-specific variables
      scenarioVar: value1
    dataFiles:                  # CSV data sources
      users: data/users.csv
    requests:                   # Requests in this scenario
      - name: Request 1
        # Request properties...
```

## Requests

A request defines an individual API call:

```yaml
requests:
  - name: Create User                     # Request name
    protocol: http                        # Protocol (http, https, etc.)
    method: POST                          # HTTP method
    endpoint: ${baseUrl}/users            # Endpoint URL
    headers:                              # HTTP headers
      Content-Type: application/json
      Accept: application/json
      Authorization: Bearer ${apiKey}
    params:                               # Query parameters
      filter: active
      sort: name
    body: '{"name": "Test User", "email": "test@example.com"}'  # Request body
    dataSource: users                     # CSV data source reference
    variables:                            # Request-specific variables
      requestVar: value1
```

## Data-Driven Testing

You can use CSV files for data-driven testing:

### Defining Data Sources

```yaml
scenarios:
  - name: Data-Driven Test
    dataFiles:
      users: data/users.csv     # Name 'users' maps to file 'data/users.csv'
      products: data/products.csv
```

### CSV File Format

CSV files should have a header row defining the variable names:

```
username,password,expected_status
user1,pass1,200
user2,pass2,200
invalid_user,wrong_pass,401
```

### Referencing in Requests

```yaml
requests:
  - name: Login
    method: POST
    endpoint: ${baseUrl}/login
    body: '{"username": "${username}", "password": "${password}"}'
    dataSource: users
```

During execution, the test will iterate through each row in the CSV file, substituting variables accordingly.

## Complete Example

Here's a complete YAML configuration example:

```yaml
executionConfig:
  threads: 5
  iterations: 10
  rampUpSeconds: 2
  holdSeconds: 3

variables:
  baseUrl: https://jsonplaceholder.typicode.com
  userId: 123
  apiKey: my-api-key

protocolName: http

scenarios:
  - name: HTTP API Test
    variables:
      scenarioVar: value1
    dataFiles:
      users: sample_HTTP_API_Test.csv
    requests:
      - name: Create User
        protocol: http
        method: POST
        endpoint: ${baseUrl}/users
        headers:
          Content-Type: application/json
          Authorization: Bearer ${apiKey}
        body: '{"name": "Test User", "email": "test@example.com"}'
        
      - name: Get Users
        protocol: http
        method: GET
        endpoint: ${baseUrl}/users
        headers:
          Accept: application/json
        params:
          filter: active
          
  - name: HTTPS API Test
    threads: 10
    iterations: 5
    rampUp: 3
    hold: 5
    variables:
      userId: 1
    requests:
      - name: Get User Details
        protocol: https
        method: GET
        endpoint: ${baseUrl}/users/${userId}
        headers:
          Accept: application/json
```

This configuration defines two scenarios with different requests, demonstrating various configuration options and features of the framework.