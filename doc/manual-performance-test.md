# Manual Performance Test Workflow

This document describes the Manual Performance Test workflow, which allows you to run performance tests on demand using GitHub Actions.

## Overview

The Manual Performance Test workflow (`manual-performance-test.yml`) is designed to provide an easy way to execute performance tests against various API protocols (HTTP, GraphQL, JDBC, and SOAP) without requiring code changes or commits. This enables teams to:

- Run performance tests on demand
- Test different configurations and parameters
- Compare results across multiple runs
- Track performance over time
- Identify regressions early

## Workflow Features

The workflow includes the following features:

### Customizable Test Parameters

- **Protocol Selection**: Choose from HTTP, GraphQL, JDBC, SOAP, or run all protocols
- **Thread Configuration**: Set the number of concurrent threads
- **Timing Options**: Configure ramp-up period and test duration
- **Iterations**: Set a specific number of iterations or run continuously
- **Target URL**: Specify the endpoint to test (for HTTP, GraphQL, and SOAP tests)
- **Custom Configuration**: Use your own YAML configuration files

### Reporting and Analysis

- **JTL Results**: Raw JMeter results stored as artifacts
- **HTML Reports**: Comprehensive visual reports with graphs and statistics
- **Historical Comparison**: Compare current results with previous test runs
- **GitHub Pages Integration**: Automatically deploy reports to GitHub Pages
- **Test Summary**: Detailed summary in the workflow run page

### Notifications and Integrations

- **Slack Integration**: Optional notifications with test results
- **Status Badges**: Workflow status displayed in the README
- **GitHub Actions Annotations**: Issues and warnings highlighted in the workflow logs

## Using the Workflow

To use the Manual Performance Test workflow:

1. Navigate to the Actions tab in your GitHub repository
2. Select "Manual Performance Test" from the workflows list
3. Click the "Run workflow" button
4. Configure your test parameters:
   - Select the protocol to test
   - Set threads, ramp-up time, and duration
   - (Optional) Provide a target URL
   - (Optional) Specify custom configuration options
   - Choose whether to upload reports to GitHub Pages
   - Enable/disable Slack notifications
5. Click "Run workflow" to start the test

### Workflow Parameters

| Parameter            | Description                                      | Default  | Required |
|----------------------|--------------------------------------------------|----------|----------|
| protocol             | Test protocol to run                             | http     | Yes      |
| threads              | Number of concurrent threads                     | 10       | Yes      |
| ramp_up_seconds      | Ramp-up period in seconds                        | 5        | Yes      |
| duration_seconds     | Test duration in seconds                         | 30       | Yes      |
| iterations           | Number of iterations (0 for infinite)            | 0        | No       |
| target_url           | Target URL or endpoint                           |          | No       |
| custom_config        | Path to custom config file                       |          | No       |
| threshold_assertions | Enable performance threshold assertions          | false    | No       |
| upload_report        | Upload report to GitHub Pages                    | true     | Yes      |
| notify_slack         | Send Slack notification with results             | false    | No       |

## Viewing Test Results

After the workflow completes, you can view the results in several ways:

1. **GitHub Actions Summary**: The workflow run page includes a summary of the test results
2. **Artifacts**: Download the JTL results and HTML reports from the workflow run artifacts
3. **GitHub Pages**: If enabled, the HTML report is published to your repository's GitHub Pages site
4. **Slack Notifications**: If enabled, results are sent to the configured Slack channel

## Historical Comparison

The workflow includes a job to compare the current test results with previous runs. This helps identify performance trends and regressions over time.

## Advanced Configuration

### Using Custom Configuration Files

To use a custom configuration file:

1. Add your YAML configuration file to the repository (e.g., `config/custom-http-test.yml`)
2. When running the workflow, specify the path to your configuration file in the `custom_config` parameter

### Setting Up Slack Notifications

To enable Slack notifications:

1. Create a Slack webhook URL in your Slack workspace
2. Add the webhook URL as a repository secret named `SLACK_WEBHOOK_URL`
3. When running the workflow, set the `notify_slack` parameter to `true`

## Troubleshooting

If you encounter issues with the workflow:

1. Check the workflow logs for error messages
2. Verify that your target URL is accessible from GitHub Actions
3. Ensure any custom configuration files are properly formatted
4. Check that necessary secrets (e.g., SLACK_WEBHOOK_URL) are correctly set up

## Examples

### Running a Quick HTTP Test

```
Protocol: http
Threads: 5
Ramp-up: 3
Duration: 15
Target URL: https://api.example.com/users
```

### Running a Database Test

```
Protocol: jdbc
Threads: 10
Ramp-up: 5
Duration: 60
Custom Config: config/jdbc/production-test.yml
```

### Running a Comprehensive Test with Notifications

```
Protocol: all
Threads: 20
Ramp-up: 10
Duration: 120
Threshold Assertions: true
Notify Slack: true
```

## Further Reading

- [JMeter DSL Documentation](https://abstracta.github.io/jmeter-java-dsl/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Performance Testing Best Practices](https://k6.io/docs/test-types/load-testing/)
