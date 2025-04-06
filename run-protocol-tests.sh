#!/bin/bash

echo "Running Protocol-specific tests..."

# Create build directory if it doesn't exist
mkdir -p target/logs
mkdir -p target/html-reports
mkdir -p target/jtl-results

# Run HTTP tests
echo "Running HTTP protocol tests..."
mvn test -Dtest=io.perftest.protocol.http.** -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO

# Run GraphQL tests
echo "Running GraphQL protocol tests..."
mvn test -Dtest=io.perftest.protocol.graphql.** -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO

# Run JDBC tests
echo "Running JDBC protocol tests..."
mvn test -Dtest=io.perftest.protocol.jdbc.** -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO

# Run SOAP tests
echo "Running SOAP protocol tests..."
mvn test -Dtest=io.perftest.protocol.soap.** -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO

# Run Multi-protocol tests
echo "Running Multi-protocol tests..."
mvn test -Dtest=io.perftest.protocol.multi.** -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO

echo "All protocol tests completed!"