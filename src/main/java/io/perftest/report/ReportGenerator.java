package io.perftest.report;

import io.perftest.model.Scenario;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Generates HTML reports for performance test results
 */
public class ReportGenerator {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Generate a performance report in HTML format
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
        
        // Create basic HTML report
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Performance Test Report: " + scenario.getName() + "</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("        h1 { color: #2c3e50; }\n");
        html.append("        h2 { color: #3498db; }\n");
        html.append("        h3 { color: #34495e; }\n");
        html.append("        .container { max-width: 1200px; margin: 0 auto; }\n");
        html.append("        .scenario-info { background-color: #f5f7fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n");
        html.append("        .metrics { display: flex; flex-wrap: wrap; gap: 20px; margin-top: 20px; margin-bottom: 30px; }\n");
        html.append("        .metric-card { flex: 1; min-width: 200px; border: 1px solid #ddd; border-radius: 5px; padding: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .metric-value { font-size: 24px; font-weight: bold; color: #2c3e50; margin-top: 10px; }\n");
        html.append("        .metric-label { font-size: 14px; color: #7f8c8d; }\n");
        html.append("        .request-section { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n");
        html.append("        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("        th { background-color: #f2f2f2; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>Performance Test Report</h1>\n");
        html.append("        <p>Generated: " + new Date() + "</p>\n");
        
        // Scenario information
        html.append("        <div class=\"scenario-info\">\n");
        html.append("            <h2>Scenario: " + scenario.getName() + "</h2>\n");
        html.append("            <table>\n");
        html.append("                <tr><th>Parameter</th><th>Value</th></tr>\n");
        html.append("                <tr><td>Threads</td><td>" + scenario.getThreads() + "</td></tr>\n");
        html.append("                <tr><td>Iterations</td><td>" + scenario.getIterations() + "</td></tr>\n");
        html.append("                <tr><td>Ramp-up Period</td><td>" + scenario.getRampUp() + " seconds</td></tr>\n");
        html.append("                <tr><td>Hold Time</td><td>" + scenario.getHold() + " seconds</td></tr>\n");
        html.append("                <tr><td>Engine</td><td>" + (scenario.getEngine() != null ? scenario.getEngine() : "jmdsl") + "</td></tr>\n");
        html.append("            </table>\n");
        html.append("        </div>\n");
        
        // Results for each request
        for (int i = 0; i < resultsList.size(); i++) {
            Map<String, Object> metrics = resultsList.get(i);
            String requestName = "Request " + (i + 1);
            if (i < scenario.getRequests().size()) {
                requestName = scenario.getRequests().get(i).getName();
            }
            
            html.append("        <div class=\"request-section\">\n");
            html.append("            <h3>" + requestName + "</h3>\n");
            html.append("            <div class=\"metrics\">\n");
            
            // Add metrics for this request
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
                
                html.append("                <div class=\"metric-card\">\n");
                html.append("                    <div class=\"metric-label\">" + metricName + "</div>\n");
                html.append("                    <div class=\"metric-value\">" + formattedValue + "</div>\n");
                html.append("                </div>\n");
            }
            
            html.append("            </div>\n");
            html.append("        </div>\n");
        }
        
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
        
        // Create basic HTML report
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Performance Test Report</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("        h1 { color: #2c3e50; }\n");
        html.append("        h2 { color: #3498db; }\n");
        html.append("        .container { max-width: 1200px; margin: 0 auto; }\n");
        html.append("        .metrics { display: flex; flex-wrap: wrap; gap: 20px; margin-top: 20px; }\n");
        html.append("        .metric-card { flex: 1; min-width: 200px; border: 1px solid #ddd; border-radius: 5px; padding: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .metric-value { font-size: 24px; font-weight: bold; color: #2c3e50; margin-top: 10px; }\n");
        html.append("        .metric-label { font-size: 14px; color: #7f8c8d; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>Performance Test Report</h1>\n");
        html.append("        <p>Generated: " + new Date() + "</p>\n");
        html.append("        <h2>Scenario: " + scenarioName + "</h2>\n");
        html.append("        <div class=\"metrics\">\n");
        
        // Add metrics
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Format the metric name for display
            String metricName = key.replaceAll("([A-Z])", " $1").toLowerCase();
            metricName = Character.toUpperCase(metricName.charAt(0)) + metricName.substring(1);
            
            // Format value based on type
            String formattedValue;
            if (value instanceof Double || key.toLowerCase().contains("rate")) {
                formattedValue = String.format("%.2f", value);
                if (key.toLowerCase().contains("rate")) {
                    formattedValue += "%";
                } else if (key.toLowerCase().contains("time")) {
                    formattedValue += " ms";
                }
            } else {
                formattedValue = value.toString();
            }
            
            html.append("            <div class=\"metric-card\">\n");
            html.append("                <div class=\"metric-label\">" + metricName + "</div>\n");
            html.append("                <div class=\"metric-value\">" + formattedValue + "</div>\n");
            html.append("            </div>\n");
        }
        
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