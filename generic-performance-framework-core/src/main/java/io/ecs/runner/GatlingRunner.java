package io.ecs.runner;

/**
 * GatlingRunner - Convenience class for running Gatling performance tests
 * 
 * This class provides a simple way to run Gatling tests using the ECS framework
 * from the command line.
 * 
 * Usage:
 *   mvn exec:java -Dexec.mainClass="io.ecs.runner.GatlingRunner" [-Dexec.args="path/to/config.yaml"]
 */
public class GatlingRunner extends AbstractRunner {
    
    private static final String DEFAULT_CONFIG = "src/test/resources/configs/gatling_config.yaml";
    private static final String ENGINE_TYPE = "gatling";
    private static final String REPORT_DIR = "target/reports/gatling-test";
    
    /**
     * Create a new Gatling runner
     */
    public GatlingRunner() {
        super(GatlingRunner.class, ENGINE_TYPE, DEFAULT_CONFIG, REPORT_DIR);
    }
    
    /**
     * Main entry point for the application
     * 
     * @param args Command line arguments, optionally specifying the YAML config file
     */
    public static void main(String[] args) {
        runMain(args, new GatlingRunner());
    }
}