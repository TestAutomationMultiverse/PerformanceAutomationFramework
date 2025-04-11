package io.ecs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility methods for file operations
 */
public class FileUtils {
    
    /**
     * Read a file as a string
     * 
     * @param filePath the path to the file
     * @return the file content as a string
     * @throws IOException if file cannot be read
     */
    public static String readFileAsString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
    
    /**
     * Check if a file exists
     * 
     * @param filePath the path to the file
     * @return true if the file exists
     */
    public static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    
    /**
     * Read a file as lines
     * 
     * @param filePath the path to the file
     * @return array of lines
     * @throws IOException if file cannot be read
     */
    public static String[] readFileAsLines(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        
        return reader.lines().toArray(String[]::new);
    }
    
    /**
     * Get the extension of a file
     * 
     * @param filePath the path to the file
     * @return the file extension
     */
    public static String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
            return filePath.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}
