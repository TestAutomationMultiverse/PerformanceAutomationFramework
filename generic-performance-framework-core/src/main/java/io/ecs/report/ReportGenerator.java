package io.ecs.report;

import io.ecs.model.Scenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Generates HTML reports for performance test results that look like JMeter reports
 */
public class ReportGenerator {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Generate a performance report in HTML format styled like JMeter reports
     * 
     * @param reportPath Path where to save the report
     * @param scenario Test scenario that was executed
     * @param resultsList List of metrics from each request
     * @return Path to the generated report file
     * @throws IOException If an error occurs during report generation
     */
    public String createTestReport(String reportPath, Scenario scenario, List<Map<String, Object>> resultsList) throws IOException {
        // Create target/reports directory if it doesn't exist
        File reportFile = new File(reportPath);
        File reportDir = reportFile.getParentFile();
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }
        
        // Create JMeter-like HTML report with all CSS and JS embedded
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>JMeter Performance Report: " + scenario.getName() + "</title>\n");
        
        // Embed JMeter-style CSS
        html.append("    <style>\n");
        html.append("        :root {\n");
        html.append("            --jmeter-blue: #234090;\n");
        html.append("            --jmeter-light-blue: #5b9bd5;\n");
        html.append("            --jmeter-dark-blue: #182b61;\n");
        html.append("            --jmeter-green: #8dc63f;\n");
        html.append("            --jmeter-red: #f08080;\n");
        html.append("            --jmeter-orange: #ff9933;\n");
        html.append("            --jmeter-yellow: #ffde66;\n");
        html.append("            --jmeter-gray: #f3f3f4;\n");
        html.append("            --jmeter-border: #d1d1d1;\n");
        html.append("        }\n");
        html.append("        body {\n");
        html.append("            font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\n");
        html.append("            margin: 0;\n");
        html.append("            padding: 0;\n");
        html.append("            color: #333;\n");
        html.append("            background-color: #fff;\n");
        html.append("        }\n");
        html.append("        #page-header {\n");
        html.append("            background-color: var(--jmeter-blue);\n");
        html.append("            color: white;\n");
        html.append("            padding: 10px 20px;\n");
        html.append("            box-shadow: 0 2px 5px rgba(0,0,0,0.2);\n");
        html.append("            position: relative;\n");
        html.append("        }\n");
        html.append("        .logo {\n");
        html.append("            display: flex;\n");
        html.append("            align-items: center;\n");
        html.append("        }\n");
        html.append("        .logo-text {\n");
        html.append("            margin-left: 10px;\n");
        html.append("            font-size: 20px;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            max-width: 1200px;\n");
        html.append("            margin: 0 auto;\n");
        html.append("            padding: 20px;\n");
        html.append("        }\n");
        html.append("        .test-details {\n");
        html.append("            display: flex;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("        }\n");
        html.append("        .test-details-left {\n");
        html.append("            flex: 2;\n");
        html.append("        }\n");
        html.append("        .test-details-right {\n");
        html.append("            flex: 1;\n");
        html.append("            text-align: right;\n");
        html.append("        }\n");
        html.append("        .panel {\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            border-radius: 4px;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            background-color: white;\n");
        html.append("            box-shadow: 0 1px 3px rgba(0,0,0,0.1);\n");
        html.append("        }\n");
        html.append("        .panel-heading {\n");
        html.append("            background-color: var(--jmeter-gray);\n");
        html.append("            padding: 10px 15px;\n");
        html.append("            border-bottom: 1px solid var(--jmeter-border);\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: var(--jmeter-blue);\n");
        html.append("            border-top-left-radius: 3px;\n");
        html.append("            border-top-right-radius: 3px;\n");
        html.append("        }\n");
        html.append("        .panel-body {\n");
        html.append("            padding: 15px;\n");
        html.append("        }\n");
        html.append("        .statistics-table {\n");
        html.append("            width: 100%;\n");
        html.append("            border-collapse: collapse;\n");
        html.append("        }\n");
        html.append("        .statistics-table th {\n");
        html.append("            background-color: var(--jmeter-gray);\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            padding: 8px;\n");
        html.append("            text-align: left;\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: var(--jmeter-blue);\n");
        html.append("        }\n");
        html.append("        .statistics-table td {\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            padding: 8px;\n");
        html.append("        }\n");
        html.append("        .statistics-table tr:nth-child(even) {\n");
        html.append("            background-color: #f9f9f9;\n");
        html.append("        }\n");
        html.append("        .statistics-table tr:hover {\n");
        html.append("            background-color: #f1f1f1;\n");
        html.append("        }\n");
        html.append("        .metric-panels {\n");
        html.append("            display: flex;\n");
        html.append("            flex-wrap: wrap;\n");
        html.append("            gap: 20px;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("        }\n");
        html.append("        .metric-panel {\n");
        html.append("            flex: 1;\n");
        html.append("            min-width: 200px;\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            border-radius: 4px;\n");
        html.append("            background-color: white;\n");
        html.append("        }\n");
        html.append("        .metric-header {\n");
        html.append("            background-color: var(--jmeter-gray);\n");
        html.append("            padding: 10px;\n");
        html.append("            text-align: center;\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: var(--jmeter-blue);\n");
        html.append("            border-bottom: 1px solid var(--jmeter-border);\n");
        html.append("        }\n");
        html.append("        .metric-value {\n");
        html.append("            padding: 15px;\n");
        html.append("            text-align: center;\n");
        html.append("            font-size: 24px;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .good-metric {\n");
        html.append("            color: var(--jmeter-green);\n");
        html.append("        }\n");
        html.append("        .warning-metric {\n");
        html.append("            color: var(--jmeter-orange);\n");
        html.append("        }\n");
        html.append("        .bad-metric {\n");
        html.append("            color: var(--jmeter-red);\n");
        html.append("        }\n");
        html.append("        .chart-container {\n");
        html.append("            height: 300px;\n");
        html.append("            margin-top: 20px;\n");
        html.append("        }\n");
        html.append("        .tab-panel {\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            border-radius: 4px;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("        }\n");
        html.append("        .tab-header {\n");
        html.append("            display: flex;\n");
        html.append("            background-color: var(--jmeter-gray);\n");
        html.append("            border-bottom: 1px solid var(--jmeter-border);\n");
        html.append("        }\n");
        html.append("        .tab {\n");
        html.append("            padding: 10px 15px;\n");
        html.append("            cursor: pointer;\n");
        html.append("            border-right: 1px solid var(--jmeter-border);\n");
        html.append("        }\n");
        html.append("        .tab.active {\n");
        html.append("            background-color: white;\n");
        html.append("            border-bottom: 2px solid var(--jmeter-blue);\n");
        html.append("            font-weight: 600;\n");
        html.append("        }\n");
        html.append("        .tab-content {\n");
        html.append("            padding: 15px;\n");
        html.append("        }\n");
        html.append("        .page-footer {\n");
        html.append("            text-align: center;\n");
        html.append("            padding: 10px;\n");
        html.append("            margin-top: 20px;\n");
        html.append("            font-size: 12px;\n");
        html.append("            color: #777;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        
        // Embedded JavaScript functions
        html.append("    <script>\n");
        html.append("        // Function to show/hide tab content\n");
        html.append("        function showTab(tabId) {\n");
        html.append("            // Hide all tab content\n");
        html.append("            const tabContents = document.querySelectorAll('.tab-content-panel');\n");
        html.append("            tabContents.forEach(content => {\n");
        html.append("                content.style.display = 'none';\n");
        html.append("            });\n");
        html.append("            \n");
        html.append("            // Remove active class from all tabs\n");
        html.append("            const tabs = document.querySelectorAll('.tab');\n");
        html.append("            tabs.forEach(tab => {\n");
        html.append("                tab.classList.remove('active');\n");
        html.append("            });\n");
        html.append("            \n");
        html.append("            // Show selected tab content\n");
        html.append("            document.getElementById(tabId).style.display = 'block';\n");
        html.append("            \n");
        html.append("            // Add active class to selected tab\n");
        html.append("            document.querySelector(`[data-tab=\"${tabId}\"]`).classList.add('active');\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        // Initialize after page load\n");
        html.append("        document.addEventListener('DOMContentLoaded', function() {\n");
        html.append("            // Show the first tab by default\n");
        html.append("            const firstTab = document.querySelector('.tab-content-panel');\n");
        html.append("            if (firstTab) {\n");
        html.append("                showTab(firstTab.id);\n");
        html.append("            }\n");
        html.append("        });\n");
        html.append("    </script>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // JMeter-style header
        html.append("    <div id=\"page-header\">\n");
        html.append("        <div class=\"logo\">\n");
        html.append("            <div class=\"logo-text\">JMeter Performance Report</div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        html.append("    <div class=\"container\">\n");
        
        // Test details section
        html.append("        <div class=\"test-details\">\n");
        html.append("            <div class=\"test-details-left\">\n");
        html.append("                <h2>" + scenario.getName() + "</h2>\n");
        html.append("                <p>Test started: " + new Date() + "</p>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"test-details-right\">\n");
        html.append("                <p><strong>Test duration:</strong> " + (scenario.getRampUp() + scenario.getHold()) + " seconds</p>\n");
        html.append("                <p><strong>Threads:</strong> " + scenario.getThreads() + "</p>\n");
        html.append("                <p><strong>Engine:</strong> " + (scenario.getEngine() != null ? scenario.getEngine() : "jmdsl") + "</p>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        // Test configuration panel
        html.append("        <div class=\"panel\">\n");
        html.append("            <div class=\"panel-heading\">Test Configuration</div>\n");
        html.append("            <div class=\"panel-body\">\n");
        html.append("                <table class=\"statistics-table\">\n");
        html.append("                    <tr><th>Parameter</th><th>Value</th></tr>\n");
        html.append("                    <tr><td>Threads</td><td>" + scenario.getThreads() + "</td></tr>\n");
        html.append("                    <tr><td>Iterations</td><td>" + scenario.getIterations() + "</td></tr>\n");
        html.append("                    <tr><td>Ramp-up Period</td><td>" + scenario.getRampUp() + " seconds</td></tr>\n");
        html.append("                    <tr><td>Hold Time</td><td>" + scenario.getHold() + " seconds</td></tr>\n");
        html.append("                    <tr><td>Engine</td><td>" + (scenario.getEngine() != null ? scenario.getEngine() : "jmdsl") + "</td></tr>\n");
        html.append("                </table>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        // Tab panel for request results
        html.append("        <div class=\"tab-panel\">\n");
        html.append("            <div class=\"tab-header\">\n");
        
        // Generate tabs for each request
        for (int i = 0; i < resultsList.size(); i++) {
            String requestName = "Request " + (i + 1);
            if (i < scenario.getRequests().size()) {
                requestName = scenario.getRequests().get(i).getName();
            }
            String tabId = "tab-" + i;
            boolean isActive = (i == 0); // First tab active by default
            
            html.append("                <div class=\"tab" + (isActive ? " active" : "") + "\" data-tab=\"" + tabId + "\" onclick=\"showTab('" + tabId + "')\">" + requestName + "</div>\n");
        }
        
        html.append("            </div>\n");
        
        // Tab content panels
        html.append("            <div class=\"tab-content\">\n");
        
        for (int i = 0; i < resultsList.size(); i++) {
            Map<String, Object> metrics = resultsList.get(i);
            String requestName = "Request " + (i + 1);
            if (i < scenario.getRequests().size()) {
                requestName = scenario.getRequests().get(i).getName();
            }
            String tabId = "tab-" + i;
            boolean isVisible = (i == 0); // First tab visible by default
            
            html.append("                <div id=\"" + tabId + "\" class=\"tab-content-panel\" style=\"display: " + (isVisible ? "block" : "none") + ";\">\n");
            
            // Summary metrics panels in a grid
            html.append("                    <div class=\"metric-panels\">\n");
            
            // Important metrics to highlight (customize based on available metrics)
            String[] keyMetrics = {"avgResponseTime", "minResponseTime", "maxResponseTime", "90thPercentile", "95thPercentile", "successRate", "totalRequests"};
            for (String metricKey : keyMetrics) {
                for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                    String key = entry.getKey();
                    if (!key.equalsIgnoreCase(metricKey) && !key.replace("_", "").equalsIgnoreCase(metricKey)) {
                        continue;
                    }
                    
                    Object value = entry.getValue();
                    if (value == null) continue;
                    
                    // Format the metric name for display
                    String metricName = key.replaceAll("([A-Z])", " $1").toLowerCase();
                    metricName = Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1);
                    
                    // Format value based on type
                    String formattedValue;
                    String metricClass = ""; // CSS class for coloring
                    
                    if (value instanceof Double) {
                        double doubleValue = (double) value;
                        
                        // Determine metric class based on type and value
                        if (key.toLowerCase().contains("rate")) {
                            formattedValue = String.format("%.2f%%", doubleValue);
                            if (doubleValue > 95) metricClass = "good-metric";
                            else if (doubleValue > 80) metricClass = "warning-metric";
                            else metricClass = "bad-metric";
                        } else if (key.toLowerCase().contains("time")) {
                            formattedValue = String.format("%.2f ms", doubleValue);
                            // For response times, lower is better
                            if (doubleValue < 100) metricClass = "good-metric";
                            else if (doubleValue < 500) metricClass = "warning-metric";
                            else metricClass = "bad-metric";
                        } else {
                            formattedValue = String.format("%.2f", doubleValue);
                        }
                    } else {
                        formattedValue = value.toString();
                        if (key.toLowerCase().contains("time") && formattedValue.matches("\\d+")) {
                            formattedValue += " ms";
                            // For response times, lower is better
                            int timeValue = Integer.parseInt(formattedValue.replace(" ms", ""));
                            if (timeValue < 100) metricClass = "good-metric";
                            else if (timeValue < 500) metricClass = "warning-metric";
                            else metricClass = "bad-metric";
                        }
                    }
                    
                    html.append("                        <div class=\"metric-panel\">\n");
                    html.append("                            <div class=\"metric-header\">" + metricName + "</div>\n");
                    html.append("                            <div class=\"metric-value " + metricClass + "\">" + formattedValue + "</div>\n");
                    html.append("                        </div>\n");
                    
                    break; // Found the metric, move to next key
                }
            }
            
            html.append("                    </div>\n");
            
            // Detailed metrics table
            html.append("                    <div class=\"panel\">\n");
            html.append("                        <div class=\"panel-heading\">Detailed Statistics</div>\n");
            html.append("                        <div class=\"panel-body\">\n");
            html.append("                            <table class=\"statistics-table\">\n");
            html.append("                                <tr><th>Metric</th><th>Value</th></tr>\n");
            
            // Add all metrics to the table
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // Skip internal metrics
                if (key.startsWith("_")) {
                    continue;
                }
                
                // Format the metric name for display
                String metricName = key.replaceAll("([A-Z])", " $1").toLowerCase();
                metricName = Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1);
                
                // Format value based on type
                String formattedValue;
                if (value instanceof Double) {
                    double doubleValue = (double) value;
                    if (key.toLowerCase().contains("rate")) {
                        formattedValue = String.format("%.2f%%", doubleValue);
                    } else if (key.toLowerCase().contains("time")) {
                        formattedValue = String.format("%.2f ms", doubleValue);
                    } else {
                        formattedValue = String.format("%.2f", doubleValue);
                    }
                } else {
                    formattedValue = value.toString();
                    if (key.toLowerCase().contains("time") && formattedValue.matches("\\d+")) {
                        formattedValue += " ms";
                    }
                }
                
                html.append("                                <tr><td>" + metricName + "</td><td>" + formattedValue + "</td></tr>\n");
            }
            
            html.append("                            </table>\n");
            html.append("                        </div>\n");
            html.append("                    </div>\n");
            
            html.append("                </div>\n");
        }
        
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        // Footer
        html.append("        <div class=\"page-footer\">\n");
        html.append("            Generated by Performance Automation Framework - " + new Date() + "\n");
        html.append("        </div>\n");
        
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        // Write to file
        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(html.toString());
        }
        
        return reportPath;
    }
    
    /**
     * Legacy method for backward compatibility
     * 
     * @param scenarioName Name of the test scenario
     * @param metrics Performance metrics for the report
     * @return Path to the generated report file
     * @throws IOException If an error occurs during report generation
     */
    public static String generateReport(String scenarioName, Map<String, Object> metrics) throws IOException {
        // Create target/reports directory if it doesn't exist
        File reportsDir = new File("target/reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        
        // Generate filename with timestamp
        String timestamp = DATE_FORMAT.format(new Date());
        String reportFile = "target/reports/performance_report_" + timestamp + ".html";
        
        // Create JMeter-style HTML report
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>JMeter Performance Report</title>\n");
        html.append("    <style>\n");
        html.append("        :root {\n");
        html.append("            --jmeter-blue: #234090;\n");
        html.append("            --jmeter-light-blue: #5b9bd5;\n");
        html.append("            --jmeter-dark-blue: #182b61;\n");
        html.append("            --jmeter-green: #8dc63f;\n");
        html.append("            --jmeter-red: #f08080;\n");
        html.append("            --jmeter-orange: #ff9933;\n");
        html.append("            --jmeter-yellow: #ffde66;\n");
        html.append("            --jmeter-gray: #f3f3f4;\n");
        html.append("            --jmeter-border: #d1d1d1;\n");
        html.append("        }\n");
        html.append("        body {\n");
        html.append("            font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\n");
        html.append("            margin: 0;\n");
        html.append("            padding: 0;\n");
        html.append("            color: #333;\n");
        html.append("            background-color: #fff;\n");
        html.append("        }\n");
        html.append("        #page-header {\n");
        html.append("            background-color: var(--jmeter-blue);\n");
        html.append("            color: white;\n");
        html.append("            padding: 10px 20px;\n");
        html.append("            box-shadow: 0 2px 5px rgba(0,0,0,0.2);\n");
        html.append("            position: relative;\n");
        html.append("        }\n");
        html.append("        .logo {\n");
        html.append("            display: flex;\n");
        html.append("            align-items: center;\n");
        html.append("        }\n");
        html.append("        .logo-text {\n");
        html.append("            margin-left: 10px;\n");
        html.append("            font-size: 20px;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            max-width: 1200px;\n");
        html.append("            margin: 0 auto;\n");
        html.append("            padding: 20px;\n");
        html.append("        }\n");
        html.append("        .test-details {\n");
        html.append("            display: flex;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("        }\n");
        html.append("        .test-details-left {\n");
        html.append("            flex: 2;\n");
        html.append("        }\n");
        html.append("        .panel {\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            border-radius: 4px;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            background-color: white;\n");
        html.append("            box-shadow: 0 1px 3px rgba(0,0,0,0.1);\n");
        html.append("        }\n");
        html.append("        .panel-heading {\n");
        html.append("            background-color: var(--jmeter-gray);\n");
        html.append("            padding: 10px 15px;\n");
        html.append("            border-bottom: 1px solid var(--jmeter-border);\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: var(--jmeter-blue);\n");
        html.append("            border-top-left-radius: 3px;\n");
        html.append("            border-top-right-radius: 3px;\n");
        html.append("        }\n");
        html.append("        .panel-body {\n");
        html.append("            padding: 15px;\n");
        html.append("        }\n");
        html.append("        .statistics-table {\n");
        html.append("            width: 100%;\n");
        html.append("            border-collapse: collapse;\n");
        html.append("        }\n");
        html.append("        .statistics-table th {\n");
        html.append("            background-color: var(--jmeter-gray);\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            padding: 8px;\n");
        html.append("            text-align: left;\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: var(--jmeter-blue);\n");
        html.append("        }\n");
        html.append("        .statistics-table td {\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            padding: 8px;\n");
        html.append("        }\n");
        html.append("        .statistics-table tr:nth-child(even) {\n");
        html.append("            background-color: #f9f9f9;\n");
        html.append("        }\n");
        html.append("        .metric-panels {\n");
        html.append("            display: flex;\n");
        html.append("            flex-wrap: wrap;\n");
        html.append("            gap: 20px;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("        }\n");
        html.append("        .metric-panel {\n");
        html.append("            flex: 1;\n");
        html.append("            min-width: 200px;\n");
        html.append("            border: 1px solid var(--jmeter-border);\n");
        html.append("            border-radius: 4px;\n");
        html.append("            background-color: white;\n");
        html.append("        }\n");
        html.append("        .metric-header {\n");
        html.append("            background-color: var(--jmeter-gray);\n");
        html.append("            padding: 10px;\n");
        html.append("            text-align: center;\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: var(--jmeter-blue);\n");
        html.append("            border-bottom: 1px solid var(--jmeter-border);\n");
        html.append("        }\n");
        html.append("        .metric-value {\n");
        html.append("            padding: 15px;\n");
        html.append("            text-align: center;\n");
        html.append("            font-size: 24px;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .good-metric {\n");
        html.append("            color: var(--jmeter-green);\n");
        html.append("        }\n");
        html.append("        .warning-metric {\n");
        html.append("            color: var(--jmeter-orange);\n");
        html.append("        }\n");
        html.append("        .bad-metric {\n");
        html.append("            color: var(--jmeter-red);\n");
        html.append("        }\n");
        html.append("        .page-footer {\n");
        html.append("            text-align: center;\n");
        html.append("            padding: 10px;\n");
        html.append("            margin-top: 20px;\n");
        html.append("            font-size: 12px;\n");
        html.append("            color: #777;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // JMeter-like header
        html.append("    <div id=\"page-header\">\n");
        html.append("        <div class=\"logo\">\n");
        html.append("            <div class=\"logo-text\">JMeter Performance Report</div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        html.append("    <div class=\"container\">\n");
        
        // Test details section
        html.append("        <div class=\"test-details\">\n");
        html.append("            <div class=\"test-details-left\">\n");
        html.append("                <h2>" + scenarioName + "</h2>\n");
        html.append("                <p>Test executed: " + new Date() + "</p>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        // Metrics summary panel
        html.append("        <div class=\"panel\">\n");
        html.append("            <div class=\"panel-heading\">Performance Metrics Summary</div>\n");
        html.append("            <div class=\"panel-body\">\n");
        
        // Key metrics in panels
        html.append("                <div class=\"metric-panels\">\n");
        
        // Extract and highlight key metrics first
        String[] keyMetrics = {"avgResponseTime", "minResponseTime", "maxResponseTime", "90thPercentile", "95thPercentile", "successRate", "totalRequests"};
        for (String metricKey : keyMetrics) {
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                String key = entry.getKey();
                if (!key.equalsIgnoreCase(metricKey) && !key.replace("_", "").equalsIgnoreCase(metricKey)) {
                    continue;
                }
                
                Object value = entry.getValue();
                if (value == null) continue;
                
                // Format the metric name for display
                String metricName = key.replaceAll("([A-Z])", " $1").toLowerCase();
                metricName = Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1);
                
                // Format value based on type and determine CSS class
                String formattedValue;
                String metricClass = "";
                
                if (value instanceof Double) {
                    double doubleValue = (double) value;
                    
                    if (key.toLowerCase().contains("rate")) {
                        formattedValue = String.format("%.2f%%", doubleValue);
                        if (doubleValue > 95) metricClass = "good-metric";
                        else if (doubleValue > 80) metricClass = "warning-metric";
                        else metricClass = "bad-metric";
                    } else if (key.toLowerCase().contains("time")) {
                        formattedValue = String.format("%.2f ms", doubleValue);
                        // Lower response times are better
                        if (doubleValue < 100) metricClass = "good-metric";
                        else if (doubleValue < 500) metricClass = "warning-metric";
                        else metricClass = "bad-metric";
                    } else {
                        formattedValue = String.format("%.2f", doubleValue);
                    }
                } else {
                    formattedValue = value.toString();
                    if (key.toLowerCase().contains("time") && formattedValue.matches("\\d+")) {
                        formattedValue += " ms";
                        // For response times, lower is better
                        int timeValue = Integer.parseInt(formattedValue.replace(" ms", ""));
                        if (timeValue < 100) metricClass = "good-metric";
                        else if (timeValue < 500) metricClass = "warning-metric";
                        else metricClass = "bad-metric";
                    }
                }
                
                html.append("                    <div class=\"metric-panel\">\n");
                html.append("                        <div class=\"metric-header\">" + metricName + "</div>\n");
                html.append("                        <div class=\"metric-value " + metricClass + "\">" + formattedValue + "</div>\n");
                html.append("                    </div>\n");
                
                break; // Found the metric, move to next key
            }
        }
        
        html.append("                </div>\n");
        
        // All metrics in a table
        html.append("                <table class=\"statistics-table\">\n");
        html.append("                    <tr><th>Metric</th><th>Value</th></tr>\n");
        
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Format the metric name for display
            String metricName = key.replaceAll("([A-Z])", " $1").toLowerCase();
            metricName = Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1);
            
            // Format value based on type
            String formattedValue;
            if (value instanceof Double) {
                double doubleValue = (double) value;
                if (key.toLowerCase().contains("rate")) {
                    formattedValue = String.format("%.2f%%", doubleValue);
                } else if (key.toLowerCase().contains("time")) {
                    formattedValue = String.format("%.2f ms", doubleValue);
                } else {
                    formattedValue = String.format("%.2f", doubleValue);
                }
            } else {
                formattedValue = value.toString();
                if (key.toLowerCase().contains("time") && formattedValue.matches("\\d+")) {
                    formattedValue += " ms";
                }
            }
            
            html.append("                    <tr><td>" + metricName + "</td><td>" + formattedValue + "</td></tr>\n");
        }
        
        html.append("                </table>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        // Footer
        html.append("        <div class=\"page-footer\">\n");
        html.append("            Generated by Performance Automation Framework - " + new Date() + "\n");
        html.append("        </div>\n");
        
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        // Write to file
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(html.toString());
        }
        
        return reportFile;
    }
}