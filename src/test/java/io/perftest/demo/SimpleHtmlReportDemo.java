package io.perftest.demo;

import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.listeners.HtmlReporter;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class SimpleHtmlReportDemo {

    @Test
    public void simpleHtmlReportTest() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String protocol = "http";
        Path reportPath = Paths.get("target", "html-reports", protocol + "_" + timestamp);
        
        System.out.println("Running simple HTTP test with HTML report...");
        
        TestPlanStats stats = testPlan(
            threadGroup(1, 5,
                // Note: In JMeter DSL 1.29.1, don't use name() on httpSampler
                httpSampler("https://example.com")
            ),
            htmlReporter(reportPath.toString())
        ).run();
        
        System.out.println("Test completed with " + stats.overall().samplesCount() + " samples");
        System.out.println("HTML report generated at: " + reportPath.toAbsolutePath());
    }
}
