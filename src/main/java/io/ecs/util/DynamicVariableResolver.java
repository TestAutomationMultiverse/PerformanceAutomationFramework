package io.ecs.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for resolving dynamic variables in templates
 * Handles special variables like ${iteration}, ${threadNum}, ${timestamp}, etc.
 */
public class DynamicVariableResolver {
    
    // Pattern to match ${variable} in templates
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    // Special dynamic variables that are resolved at runtime
    private static final String ITERATION_VAR = "iteration";
    private static final String THREAD_NUM_VAR = "threadNum";
    private static final String TIMESTAMP_VAR = "timestamp";
    private static final String RANDOM_INT_VAR = "randomInt";
    private static final String RANDOM_STRING_VAR = "randomString";
    private static final String UUID_VAR = "uuid";
    
    /**
     * Process a template with variable substitution
     * 
     * @param template the template to process
     * @param variables the variables to substitute
     * @param dynamicContext runtime context with dynamic values (iteration, thread number, etc.)
     * @return the processed template
     */
    public static String processTemplate(String template, Map<String, String> variables, Map<String, Object> dynamicContext) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        
        // Combine static and dynamic variables
        Map<String, String> allVariables = new HashMap<>();
        if (variables != null) {
            allVariables.putAll(variables);
        }
        
        // Add dynamic values from context
        if (dynamicContext != null) {
            for (Map.Entry<String, Object> entry : dynamicContext.entrySet()) {
                allVariables.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        
        // Replace variables in template
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = resolveDynamicVariable(variableName, allVariables, dynamicContext);
            
            // Escape Matcher special chars in replacement
            replacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(result, replacement);
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Resolve a dynamic variable value
     * 
     * @param variableName the variable name
     * @param variables the static variables
     * @param dynamicContext the dynamic context
     * @return the resolved value
     */
    private static String resolveDynamicVariable(String variableName, Map<String, String> variables, Map<String, Object> dynamicContext) {
        // First check if it's a special dynamic variable
        if (variableName.equals(ITERATION_VAR)) {
            return getIterationValue(dynamicContext);
        } else if (variableName.equals(THREAD_NUM_VAR)) {
            return getThreadNumValue(dynamicContext);
        } else if (variableName.equals(TIMESTAMP_VAR)) {
            return String.valueOf(System.currentTimeMillis());
        } else if (variableName.startsWith(RANDOM_INT_VAR)) {
            return getRandomIntValue(variableName);
        } else if (variableName.startsWith(RANDOM_STRING_VAR)) {
            return getRandomStringValue(variableName);
        } else if (variableName.equals(UUID_VAR)) {
            return java.util.UUID.randomUUID().toString();
        }
        
        // Then check static variables
        if (variables.containsKey(variableName)) {
            return variables.get(variableName);
        }
        
        // Default: return the original template
        return "${" + variableName + "}";
    }
    
    /**
     * Get the current iteration value from context
     * 
     * @param context the dynamic context
     * @return the iteration value as string
     */
    private static String getIterationValue(Map<String, Object> context) {
        if (context != null && context.containsKey(ITERATION_VAR)) {
            return String.valueOf(context.get(ITERATION_VAR));
        }
        return "1"; // Default iteration
    }
    
    /**
     * Get the current thread number value from context
     * 
     * @param context the dynamic context
     * @return the thread number value as string
     */
    private static String getThreadNumValue(Map<String, Object> context) {
        if (context != null && context.containsKey(THREAD_NUM_VAR)) {
            return String.valueOf(context.get(THREAD_NUM_VAR));
        }
        return "1"; // Default thread number
    }
    
    /**
     * Get a random integer value (supports ranges via syntax: ${randomInt(min,max)})
     * 
     * @param variableName the variable name with optional range
     * @return a random integer as string
     */
    private static String getRandomIntValue(String variableName) {
        int min = 1;
        int max = 1000;
        
        // Check if pattern is randomInt(min,max)
        if (variableName.contains("(") && variableName.contains(")")) {
            String rangeStr = variableName.substring(
                variableName.indexOf("(") + 1, 
                variableName.indexOf(")")
            );
            
            String[] parts = rangeStr.split(",");
            if (parts.length == 2) {
                try {
                    min = Integer.parseInt(parts[0].trim());
                    max = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    // Use defaults if parsing fails
                }
            }
        }
        
        return String.valueOf(min + (int)(Math.random() * ((max - min) + 1)));
    }
    
    /**
     * Get a random string value (supports length via syntax: ${randomString(length)})
     * 
     * @param variableName the variable name with optional length
     * @return a random string
     */
    private static String getRandomStringValue(String variableName) {
        int length = 10; // Default length
        
        // Check if pattern is randomString(length)
        if (variableName.contains("(") && variableName.contains(")")) {
            String lengthStr = variableName.substring(
                variableName.indexOf("(") + 1, 
                variableName.indexOf(")")
            );
            
            try {
                length = Integer.parseInt(lengthStr.trim());
            } catch (NumberFormatException e) {
                // Use default if parsing fails
            }
        }
        
        // Generate random string
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int)(Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
}