#!/bin/bash

# Performance Automation Framework Setup Script
# This script clones the repository and sets up the required environment

echo "Setting up Performance Automation Framework..."

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "Git is not installed. Please install git first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java JDK 8 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Display Java and Maven versions
echo "Java version:"
java -version
echo "Maven version:"
mvn --version

# Clone the repository
echo "Cloning the Performance Automation Framework repository..."
git clone https://github.com/TestAutomationMultiverse/PerformanceAutomationFramework.git

# Change to the repository directory
cd PerformanceAutomationFramework

# Build the project with Maven
echo "Building the project with Maven..."
mvn clean install -DskipTests

echo "Setup completed successfully!"
echo "The Performance Automation Framework has been cloned and built."
echo "For usage instructions, please refer to the README.md file."
