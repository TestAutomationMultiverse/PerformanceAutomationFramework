package io.ecs;

import io.ecs.util.FileUtils;
import java.io.File;
import java.io.IOException;

/**
 * A simple standalone test class to verify the file path resolution functionality
 */
public class FilePathResolutionTest {
    
    public static void main(String[] args) {
        System.out.println("Testing file path resolution functionality");
        
        // Test full paths
        testPath("src/test/resources/configs/sample_config.yaml", true);
        
        // Test simplified paths
        testPath("sample_config.yaml", true);
        testPath("create_user_headers.json", false); // This file might exist or not
        
        // Test templates base paths
        if (new File("src/test/resources/templates/http/headers").exists()) {
            System.out.println("Headers directory exists");
        } else {
            System.out.println("Headers directory doesn't exist, creating it");
            new File("src/test/resources/templates/http/headers").mkdirs();
        }
        
        // Create a test header file
        try {
            File testFile = new File("src/test/resources/templates/http/headers/test_headers.json");
            if (!testFile.exists()) {
                testFile.createNewFile();
                System.out.println("Created test header file: " + testFile.getAbsolutePath());
            }
            
            // Now test the resolution of just the filename
            testPath("test_headers.json", true);
        } catch (IOException e) {
            System.out.println("Error creating test file: " + e.getMessage());
        }
        
        System.out.println("File path resolution test completed");
    }
    
    /**
     * Test a path and print the results
     * 
     * @param filePath Path to test
     * @param shouldExist Whether the file should exist
     */
    private static void testPath(String filePath, boolean shouldExist) {
        System.out.println("\nTesting path: " + filePath);
        
        // Check if the file exists as is
        boolean exists = new File(filePath).exists();
        System.out.println("File exists as is: " + exists);
        
        // Try to resolve the path
        String resolvedPath = FileUtils.resolveFilePath(filePath);
        System.out.println("Resolved path: " + resolvedPath);
        
        // Check if the resolved path exists
        boolean resolvedExists = new File(resolvedPath).exists();
        System.out.println("Resolved file exists: " + resolvedExists);
        
        // Report test status
        if (shouldExist && resolvedExists) {
            System.out.println("✓ PASS: File resolution worked as expected");
        } else if (!shouldExist && !resolvedExists) {
            System.out.println("✓ PASS: File correctly not found");
        } else if (shouldExist && !resolvedExists) {
            System.out.println("✗ FAIL: File should exist but wasn't found");
        } else {
            System.out.println("✗ FAIL: File shouldn't exist but was found");
        }
    }
}