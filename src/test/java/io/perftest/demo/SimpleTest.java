package io.perftest.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTest {

    @Test
    public void simpleTest() {
        System.out.println("Running a simple test");
        assertTrue(true, "This test should always pass");
    }
}
