package io.ecs.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and manages CSV data for test execution
 */
public class CsvDataSource {
    private final List<Map<String, String>> data;
    private final String[] headers;
    
    /**
     * Create a CSV data source from a file
     * 
     * @param filePath the path to the CSV file
     * @throws IOException if file cannot be read
     */
    public CsvDataSource(String filePath) throws IOException {
        data = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("CSV file is empty");
            }
            
            // Parse headers
            headers = parseCsvLine(line);
            
            // Parse data
            while ((line = reader.readLine()) != null) {
                String[] values = parseCsvLine(line);
                Map<String, String> row = new HashMap<>();
                
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i], values[i]);
                }
                
                data.add(row);
            }
        }
    }
    
    /**
     * Parse a CSV line, handling quoted values
     * 
     * @param line the CSV line to parse
     * @return array of values
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
    
    /**
     * Get the total number of rows in the data
     * 
     * @return number of rows
     */
    public int getRowCount() {
        return data.size();
    }
    
    /**
     * Get a specific row of data
     * 
     * @param index the row index
     * @return map of column names to values
     */
    public Map<String, String> getRow(int index) {
        if (index < 0 || index >= data.size()) {
            return null;
        }
        return new HashMap<>(data.get(index));
    }
    
    /**
     * Get all data
     * 
     * @return list of data rows
     */
    public List<Map<String, String>> getAllData() {
        return new ArrayList<>(data);
    }
    
    /**
     * Get the headers
     * 
     * @return array of header names
     */
    public String[] getHeaders() {
        return headers.clone();
    }
}
