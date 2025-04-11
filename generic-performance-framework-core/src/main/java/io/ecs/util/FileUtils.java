package io.ecs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for file operations
 */
public class FileUtils {
    
    // Base paths for different resource types
    private static final String TEMPLATES_BASE_PATH = "src/test/resources/templates/";
    private static final String BODIES_BASE_PATH = TEMPLATES_BASE_PATH + "http/body/";
    private static final String HEADERS_BASE_PATH = TEMPLATES_BASE_PATH + "http/headers/";
    private static final String PARAMS_BASE_PATH = TEMPLATES_BASE_PATH + "http/params/";
    private static final String SCHEMA_BASE_PATH = TEMPLATES_BASE_PATH + "http/schema/";
    private static final String DATA_BASE_PATH = "src/test/resources/data/";
    
    /**
     * Read a file as a string
     * 
     * @param filePath the path to the file
     * @return the file content as a string
     * @throws IOException if file cannot be read
     */
    public static String readFileAsString(String filePath) throws IOException {
        // Resolve path if it's a relative path that should use base paths
        String resolvedPath = resolveFilePath(filePath);
        return new String(Files.readAllBytes(Paths.get(resolvedPath)));
    }
    
    /**
     * Resolves a potentially relative file path to the appropriate absolute path
     * For files specified in YAML configs, allow using just the filename without full path
     * 
     * @param filePath the original file path
     * @return the resolved file path
     */
    public static String resolveFilePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        
        // If it's already a full path that exists, return it
        if (fileExists(filePath)) {
            return filePath;
        }
        
        // Extract filename if it's a full path that doesn't exist
        // (this allows handling paths like src/test/resources/templates/http/headers/default_headers.json)
        String fileName = new File(filePath).getName();
        
        // Check in appropriate base directories based on file type and extension
        if (fileName.endsWith(".json") || fileName.endsWith(".jsonc")) {
            // Could be a header, body, or schema file
            if (fileName.contains("header") || fileName.contains("Header")) {
                return checkAndReturnPath(HEADERS_BASE_PATH + fileName);
            } else if (fileName.contains("body") || fileName.contains("Body")) {
                return checkAndReturnPath(BODIES_BASE_PATH + fileName);
            } else if (fileName.contains("schema") || fileName.contains("Schema")) {
                return checkAndReturnPath(SCHEMA_BASE_PATH + fileName);
            }
        } else if (fileName.endsWith(".template")) {
            // Likely a params template
            return checkAndReturnPath(PARAMS_BASE_PATH + fileName);
        } else if (fileName.endsWith(".csv")) {
            // Likely a data file
            return checkAndReturnPath(DATA_BASE_PATH + fileName);
        }
        
        // Check in all potential locations if we couldn't determine the type
        String[] basePaths = {
            HEADERS_BASE_PATH,
            BODIES_BASE_PATH,
            PARAMS_BASE_PATH,
            SCHEMA_BASE_PATH,
            DATA_BASE_PATH,
            TEMPLATES_BASE_PATH,
            "src/test/resources/configs/",
            "src/test/resources/"
        };
        
        for (String basePath : basePaths) {
            String potentialPath = basePath + fileName;
            if (fileExists(potentialPath)) {
                return potentialPath;
            }
        }
        
        // If we couldn't find it, return the original path
        return filePath;
    }
    
    /**
     * Check if a path exists and return it, otherwise return null
     */
    private static String checkAndReturnPath(String path) {
        if (fileExists(path)) {
            return path;
        }
        return null;
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
        // Resolve path if it's a relative path that should use base paths
        String resolvedPath = resolveFilePath(filePath);
        BufferedReader reader = new BufferedReader(new FileReader(resolvedPath));
        
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