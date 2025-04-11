package io.ecs.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes template files with variable substitution
 */
public class TemplateProcessor {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private final ObjectMapper objectMapper;
    
    public TemplateProcessor() {
        objectMapper = new ObjectMapper();
    }
    
    /**
     * Process a template file with variable substitution
     * 
     * @param templatePath the path to the template file
     * @param variables the variables to substitute
     * @return the processed template
     * @throws IOException if template cannot be read
     */
    public String process(String templatePath, Map<String, String> variables) throws IOException {
        String template = FileUtils.readFileAsString(templatePath);
        return processTemplate(template, variables);
    }
    
    /**
     * Process a template string with variable substitution
     * 
     * @param template the template string
     * @param variables the variables to substitute
     * @return the processed template
     */
    public String processTemplate(String template, Map<String, String> variables) {
        if (template == null || variables == null) {
            return template;
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = variables.containsKey(varName) ? variables.get(varName) : "";
            
            // Escape $ and \ for string replacement
            replacement = replacement.replace("\\", "\\\\").replace("$", "\\$");
            
            matcher.appendReplacement(result, replacement);
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Process a parameters template file
     * 
     * @param templatePath the path to the parameters template
     * @param variables the variables to substitute
     * @return map of parameter name to value
     * @throws IOException if template cannot be read
     */
    public Map<String, String> processParamsTemplate(String templatePath, Map<String, String> variables) throws IOException {
        Map<String, String> params = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(templatePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines and comments
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse key=value pairs
                int equalIndex = line.indexOf('=');
                if (equalIndex > 0) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();
                    
                    // Process the value for variables
                    value = processTemplate(value, variables);
                    
                    params.put(key, value);
                }
            }
        }
        
        return params;
    }
    
    /**
     * Parse a JSON string to a map
     * 
     * @param json the JSON string
     * @return map of key-value pairs
     * @throws IOException if JSON parsing fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> parseJsonToMap(String json) throws IOException {
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        Map<String, String> result = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString());
        }
        
        return result;
    }
}
