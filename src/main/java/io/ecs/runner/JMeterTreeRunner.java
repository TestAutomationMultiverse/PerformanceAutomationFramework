package io.ecs.runner;

/**
 * JMeterTreeRunner - Convenience class for running JMeter TreeBuilder performance tests
 * 
 * This class provides a simple way to run JMeter TreeBuilder tests using the ECS framework
 * from the command line.
 * 
 * Usage:
 *   mvn exec:java -Dexec.mainClass="io.ecs.runner.JMeterTreeRunner" [-Dexec.args="path/to/config.yaml"]
 */
public class JMeterTreeRunner extends AbstractRunner {
    
    private static final String DEFAULT_CONFIG = "src/test/resources/configs/jmeter_tree_config.yaml";
    private static final String ENGINE_TYPE = "jmtree";
    private static final String REPORT_DIR = "target/reports/jmeter-treebuilder-test";
    
    /**
     * Create a new JMeter TreeBuilder runner
     */
    public JMeterTreeRunner() {
        super(JMeterTreeRunner.class, ENGINE_TYPE, DEFAULT_CONFIG, REPORT_DIR);
    }
    
    /**
     * Main entry point for the application
     * 
     * @param args Command line arguments, optionally specifying the YAML config file
     */
    public static void main(String[] args) {
        runMain(args, new JMeterTreeRunner());
    }
}