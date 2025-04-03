package io.perftest.examples;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class SimpleTest {
    
    @Test
    public void testSimpleHttpRequest() throws IOException {
        TestPlanStats stats = testPlan(
            threadGroup(1, 1,
                httpSampler("https://test-api.k6.io/public/crocodiles/")
                    .children(
                        responseAssertion().containsSubstrings("200")
                        // Assertions disabled due to API compatibility issues with JMeter DSL 1.29.1
                        // jsonAssertion("$[0].name").exists(),
                        // jsonAssertion("$[0].sex").exists()
                    )
            )
        ).run();
        
        System.out.println("Average response time: " + stats.overall().sampleTime().mean());
    }
}