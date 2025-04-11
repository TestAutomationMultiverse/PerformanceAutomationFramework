# Deployment Guide

This document explains how to deploy the Generic Performance Framework to GitHub Packages.

## Publishing to GitHub Packages

The framework is configured to be published to GitHub Packages at `https://maven.pkg.github.com/TestAutomationMultiverse/GenericPerformanceFramework`, which allows other developers to use it as a dependency in their projects.

### Prerequisites

1. GitHub account with write access to the repository
2. GitHub Personal Access Token (PAT) with `repo` and `write:packages` scopes

### Automated Deployment with GitHub Actions

The project includes GitHub Actions workflows that automate the deployment process:

1. **CI Workflow (`ci.yml`)**: Builds and tests the project on every push to main and develop branches
2. **Release Workflow (`release.yml`)**: Publishes the framework to GitHub Packages

#### Triggering a Release

You can trigger a release in one of two ways:

1. **Manual Trigger**:
   - Go to the GitHub repository
   - Navigate to Actions > Release to GitHub Packages
   - Click "Run workflow" and select the branch (usually main)

2. **Tag-based Release**:
   - Create and push a new tag with the format `v*` (e.g., `v1.0.0`)
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
   - This will automatically trigger the release workflow and create a GitHub Release with the generated artifacts

### Manual Deployment

If you need to deploy manually, you can do so using Maven:

1. Set up authentication in your `~/.m2/settings.xml`:
   ```xml
   <settings>
     <servers>
       <server>
         <id>github</id>
         <username>YOUR_GITHUB_USERNAME</username>
         <password>YOUR_GITHUB_TOKEN</password>
       </server>
     </servers>
   </settings>
   ```

2. Build and deploy the core module:
   ```bash
   cd generic-performance-framework-core
   mvn clean deploy
   ```

## Version Management

When releasing a new version, make sure to update the version number in the following files:

1. Parent POM: `pom.xml`
2. Core module POM: `generic-performance-framework-core/pom.xml`
3. Tests module POM: `generic-performance-framework-tests/pom.xml`

Follow semantic versioning (MAJOR.MINOR.PATCH):
- MAJOR: Incompatible API changes
- MINOR: Add functionality in a backward-compatible manner
- PATCH: Backward-compatible bug fixes

## Consuming the Library

To use the published library in another project, see the [Using as a Dependency](../README.md#using-as-a-dependency) section in the main README.