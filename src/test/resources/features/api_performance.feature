Feature: API Performance Tests
  As a performance engineer
  I want to test the performance of various APIs
  So that I can ensure they meet performance requirements

  Scenario: Basic K6 API GET Test
    Given a performance test "K6_GET_Crocodiles" with description "Performance test for K6 API - GET /public/crocodiles"
    And the base URL is "https://test-api.k6.io"
    And the test uses 10 threads with 5 seconds ramp-up
    And the test runs for 30 seconds
    When I send a GET request to "/public/crocodiles/"
    And I execute the performance test
    Then the test should complete without errors
    And the error percentage should be less than 1.0%
    And the average response time should be less than 2000 ms
    And the 90th percentile response time should be less than 3000 ms
    And the throughput should be greater than 1.0 requests per second
    And I store the test results for reporting

  Scenario: User registration and authentication flow
    Given a performance test "K6_User_Flow" with description "User registration and authentication flow"
    And the base URL is "https://test-api.k6.io"
    And the test uses 5 threads with 3 seconds ramp-up
    And the test runs for 20 seconds
    And a user with random data
    When I execute a transaction named "User Registration and Auth Flow"
    And I add a POST request to "/user/register/" with body:
      """
      {
        "username": "{{ user.username }}",
        "first_name": "{{ user.attributes.firstName }}",
        "last_name": "{{ user.attributes.lastName }}",
        "email": "{{ user.email }}",
        "password": "{{ user.password }}"
      }
      """
    And I add a POST request to "/auth/token/login/" with body:
      """
      {
        "username": "{{ user.username }}",
        "password": "{{ user.password }}"
      }
      """
    And I add a JSON extractor "$.access" to variable "accessToken"
    And I add a GET request to "/my/crocodiles/"
    And I complete the transaction
    And I execute the performance test
    Then the test should complete without errors
    And the average response time should be less than 2000 ms
    And I store the test results for reporting

  Scenario: SOAP Calculator API Test
    Given a performance test "SOAP_Calculator" with description "Test SOAP calculator operations"
    And the test uses 3 threads with 2 seconds ramp-up
    And the test runs for 15 seconds
    When I send a SOAP request to "http://www.dneonline.com/calculator.asmx" with operation "http://tempuri.org/Add" and payload:
      """
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tem="http://tempuri.org/">
         <soapenv:Header/>
         <soapenv:Body>
            <tem:Add>
               <tem:intA>10</tem:intA>
               <tem:intB>20</tem:intB>
            </tem:Add>
         </soapenv:Body>
      </soapenv:Envelope>
      """
    And I execute the performance test
    Then the test should complete without errors
    And the error percentage should be less than 1.0%
    And the average response time should be less than 2000 ms
    And I store the test results for reporting

  Scenario: GraphQL Countries API Test
    Given a performance test "GraphQL_Countries" with description "Test GraphQL countries API"
    And the test uses 5 threads with 3 seconds ramp-up
    And the test runs for 20 seconds
    And the test uses think time between 500 and 1500 milliseconds
    When I send a GraphQL query to "https://countries.trevorblades.com/graphql" with operation "CountriesQuery" and query:
      """
      query CountriesQuery {
        countries {
          code
          name
          native
          continent {
            code
            name
          }
          languages {
            code
            name
          }
        }
      }
      """
    And I execute the performance test
    Then the test should complete without errors
    And the average response time should be less than 3000 ms
    And the throughput should be greater than 0.5 requests per second
    And I store the test results for reporting

  Scenario: Complex K6 API Transaction Flow
    Given a performance test "K6_Complex_Flow" with description "Complex K6 API transaction flow"
    And the base URL is "https://test-api.k6.io"
    And the test uses 3 threads with 2 seconds ramp-up
    And the test runs for 30 seconds
    And a user with random data
    When I execute a transaction named "Complete User and Crocodile Flow"
    And I add a POST request to "/user/register/" with body:
      """
      {
        "username": "{{ user.username }}",
        "first_name": "{{ user.attributes.firstName }}",
        "last_name": "{{ user.attributes.lastName }}",
        "email": "{{ user.email }}",
        "password": "{{ user.password }}"
      }
      """
    And I add a POST request to "/auth/token/login/" with body:
      """
      {
        "username": "{{ user.username }}",
        "password": "{{ user.password }}"
      }
      """
    And I add a JSON extractor "$.access" to variable "accessToken"
    And I add a POST request to "/my/crocodiles/" with body:
      """
      {
        "name": "Test Croc",
        "sex": "M",
        "date_of_birth": "2020-01-01"
      }
      """
    And I add a JSON extractor "$.id" to variable "crocId"
    And I add a GET request to "/my/crocodiles/${crocId}/"
    And I add a GET request to "/public/crocodiles/"
    And I complete the transaction
    And I execute the performance test
    Then the test should complete without errors
    And the error percentage should be less than 5.0%
    And the average response time should be less than 3000 ms
    And I store the test results for reporting
