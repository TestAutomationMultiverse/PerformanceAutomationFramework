package io.ecs.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for reading CSV files
 */
public class CsvReader {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvReader.class);
    private static final String DEFAULT_DELIMITER = ",";
    
    /**
     * Read a CSV file and return a list of maps, where each map represents a row
     * with column names as keys
     * 
     * @param filePath Path to the CSV file
     * @return List of maps representing CSV rows
     */
    public List<Map<String, String>> readCsv(String filePath) {
        return readCsv(filePath, DEFAULT_DELIMITER);
    }
    
    /**
     * Read a CSV file with a custom delimiter and return a list of maps
     * 
     * @param filePath Path to the CSV file
     * @param delimiter Delimiter used in the CSV file
     * @return List of maps representing CSV rows
     */
    public List<Map<String, String>> readCsv(String filePath, String delimiter) {
        List<Map<String, String>> result = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Read header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                logger.warn("CSV file is empty: {}", filePath);
                return result;
            }
            
            // Split header to get column names
            String[] headers = headerLine.split(delimiter);
            
            // Trim headers to remove whitespace
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }
            
            // Read data lines
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] values = line.split(delimiter, -1); // -1 to keep trailing empty fields
                Map<String, String> row = new HashMap<>();
                
                // Map column values to column names
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i], values[i].trim());
                }
                
                // Add any missing columns as empty strings
                for (String header : headers) {
                    if (!row.containsKey(header)) {
                        row.put(header, "");
                    }
                }
                
                result.add(row);
            }
            
            logger.info("Read {} rows from CSV file: {}", result.size(), filePath);
            
        } catch (IOException e) {
            logger.error("Error reading CSV file {}: {}", filePath, e.getMessage());
        }
        
        return result;
    }
}