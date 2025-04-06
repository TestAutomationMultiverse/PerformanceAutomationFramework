# Continuous Integration

The Performance Automation Framework includes a comprehensive CI/CD pipeline to ensure code quality, automate testing, and streamline release processes.

## GitHub Actions Workflows

The project includes several GitHub Actions workflows:

1. **CI Workflow (`ci.yml`)**: Runs on every push and pull request to verify the build and execute tests
2. **Static Analysis (`static-analysis.yml`)**: Performs code quality checks using SpotBugs and Checkstyle
3. **Release (`release.yml`)**: Creates a new release when a tag is pushed or manually triggered
4. **GitHub Pages (`pages.yml`)**: Deploys documentation to GitHub Pages
5. **Manual Performance Test (`manual-performance-test.yml`)**: Allows on-demand execution of performance tests

### Workflow Process

All code quality checks run automatically on GitHub Actions:

1. On PR creation and updates
2. On merges to main or develop branches
3. Manually via workflow dispatch

## Manual Performance Test Workflow

The Manual Performance Test workflow provides a way to run performance tests on demand using GitHub Actions. It includes:

- **Customizable test parameters**: Threads, ramp-up time, duration, iterations, and target URL
- **Protocol selection**: Choose HTTP, GraphQL, JDBC, SOAP, or run all protocols
- **Custom configuration support**: Use your own configuration files
- **Performance threshold assertions**: Validate performance against predefined thresholds
- **HTML and JTL reporting**: Comprehensive test results with visual reports
- **Reports deployment**: Automatic deployment to GitHub Pages
- **Historical comparison**: Compare results with previous test runs
- **Slack notifications**: Optional notifications with test results

### Using the Manual Performance Test Workflow

To use the workflow:

1. Go to the Actions tab in your GitHub repository
2. Select "Manual Performance Test" workflow
3. Click "Run workflow"
4. Configure your test parameters
5. Run the workflow

### Configuration Options

The workflow supports the following configuration options:

#### General Settings
- **Test Type**: Select the protocol to test (HTTP, GraphQL, JDBC, SOAP, or All)
- **Threads**: Number of concurrent users
- **Ramp-up Time**: Time in seconds to ramp up to full thread count
- **Duration**: Test duration in seconds
- **Iterations**: Number of test iterations to run
- **Target Environment**: Target environment (Dev, QA, Staging, Production)

#### Protocol-Specific Settings
- **Base URL**: Base URL for the test target
- **Custom Config Path**: Path to custom YAML configuration file
- **Database Type**: For JDBC tests (MySQL, PostgreSQL, SQLite)
- **Database URL**: JDBC connection URL

#### Reporting Options
- **Deploy Reports**: Whether to deploy reports to GitHub Pages
- **Send Notifications**: Whether to send Slack notifications
- **Notification Channel**: Slack channel for notifications
- **Performance Threshold**: Response time threshold in milliseconds

## CI Workflows for Pull Requests

For every pull request:

1. The CI workflow builds the project and runs unit tests
2. The Static Analysis workflow runs SpotBugs and Checkstyle
3. JaCoCo generates code coverage reports
4. Results are reported as PR comments and status checks

## Release Workflow

The release workflow is triggered:

1. Automatically when a tag with the pattern `v*` is pushed
2. Manually via workflow dispatch with a specified version

The workflow:

1. Builds and tests the project
2. Packages the artifacts
3. Creates a GitHub release with release notes
4. Attaches the artifacts to the release
5. Optionally publishes to Maven Central

## GitHub Pages Workflow

The GitHub Pages workflow:

1. Builds the documentation using Jekyll
2. Publishes the documentation to GitHub Pages
3. Includes test reports from the most recent manual test run

## Performance Monitoring

Continuous performance monitoring is implemented via:

1. GitHub Actions scheduled workflows that run performance tests daily
2. Historical data tracking to identify performance trends
3. Alerting when performance degrades beyond set thresholds
