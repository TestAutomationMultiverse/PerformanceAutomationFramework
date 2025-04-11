package io.ecs;

import io.ecs.runner.JMeterTreeRunner;

/**
 * Compatibility class for JMeterTreeRunner
 * 
 * This class exists to maintain backward compatibility with code that
 * may be using the old package structure. It simply forwards to the
 * new implementation in the io.ecs.runner package.
 * 
 * @deprecated Use io.ecs.runner.JMeterTreeRunner instead
 */
@Deprecated
public class JMeterTreeRunner {
    
    /**
     * Forward to the new implementation
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        io.ecs.runner.JMeterTreeRunner.main(args);
    }
}