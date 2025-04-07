package io.perftest.demo.compat;

import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

/**
 * Demo for HTML report generation that is compatible with JMeter DSL 1.29.1. This demo creates a
 * simple HTTP test and generates an HTML report.
 */
public class CompatHtmlReportDemo {

    @Test
    public void compatHtmlReportTest() throws IOException {
        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String protocol = "http";
        Path reportPath = Paths.get("target", "html-reports", protocol + "_" + timestamp);

        System.out.println("Running compatible HTTP test with HTML report...");

        TestPlanStats stats = testPlan(threadGroup(1, 5,
                // Note: In JMeter DSL 1.29.1, don't use name() on httpSampler
                httpSampler("https://example.com")), htmlReporter(reportPath.toString())).run();

        // Note: In JMeter DSL 1.29.1, use samplesCount() instead of sampleCount()
        System.out.println("Test completed with " + stats.overall().samplesCount() + " samples");
        System.out.println("HTML report generated at: " + reportPath.toAbsolutePath());
    }
}
