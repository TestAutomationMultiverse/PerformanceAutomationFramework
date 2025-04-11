package io.ecs;

import io.ecs.runner.JMeterDSLRunner;

/**
 * Compatibility class for JMeterDSLRunner
 * 
 * This class exists to maintain backward compatibility with code that
 * may be using the old package structure. It simply forwards to the
 * new implementation in the io.ecs.runner package.
 * 
 * @deprecated Use io.ecs.runner.JMeterDSLRunner instead
 */
@Deprecated
public class JMeterDSLRunner {
    
    /**
     * Forward to the new implementation
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        io.ecs.runner.JMeterDSLRunner.main(args);
    }
}