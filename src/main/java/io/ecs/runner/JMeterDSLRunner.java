package io.ecs.runner;

/**
 * JMeterDSLRunner - Convenience class for running JMeter DSL performance tests
 * 
 * This class provides a simple way to run JMeter DSL tests using the ECS framework
 * from the command line.
 * 
 * Usage:
 *   mvn exec:java -Dexec.mainClass="io.ecs.runner.JMeterDSLRunner" [-Dexec.args="path/to/config.yaml"]
 */
public class JMeterDSLRunner extends AbstractRunner {
    
    private static final String DEFAULT_CONFIG = "src/test/resources/configs/sample_config.yaml";
    private static final String ENGINE_TYPE = "jmdsl";
    private static final String REPORT_DIR = "target/reports/jmeter-dsl-test";
    
    /**
     * Create a new JMeter DSL runner
     */
    public JMeterDSLRunner() {
        super(JMeterDSLRunner.class, ENGINE_TYPE, DEFAULT_CONFIG, REPORT_DIR);
    }
    
    /**
     * Main entry point for the application
     * 
     * @param args Command line arguments, optionally specifying the YAML config file
     */
    public static void main(String[] args) {
        runMain(args, new JMeterDSLRunner());
    }
}