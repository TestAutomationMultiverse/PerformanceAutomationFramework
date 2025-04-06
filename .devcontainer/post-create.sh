#!/bin/bash

echo "Initializing development environment..."

# Check if the Maven settings are configured correctly
echo "Verifying Maven configuration..."
mkdir -p ~/.m2
if [ ! -f ~/.m2/settings.xml ]; then
  echo "Creating Maven settings.xml..."
  cat > ~/.m2/settings.xml << 'XML'
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository>${user.home}/.m2/repository</localRepository>
  <interactiveMode>true</interactiveMode>
  <usePluginRegistry>false</usePluginRegistry>
  <offline>false</offline>
</settings>
XML
fi

# Create directories for linting configuration if they don't exist
echo "Setting up linting configuration..."
mkdir -p config/checkstyle
mkdir -p config/spotbugs
mkdir -p config/pmd

# Create SpotBugs exclusion file if it doesn't exist
if [ ! -f spotbugs-exclude.xml ]; then
  echo "Creating SpotBugs exclusion file..."
  touch spotbugs-exclude.xml
fi

# Update Maven plugins for linting
echo "Configuring Maven plugins for code quality..."

# Build the project to verify everything works
echo "Building project to verify setup..."
mvn clean compile

echo "Development environment setup complete!"
