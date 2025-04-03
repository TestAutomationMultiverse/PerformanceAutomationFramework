package io.perftest.core.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processor for handling template-based substitutions in strings
 * Similar to Jinja2 style {{ variable }} templates
 */
public class TemplateProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TemplateProcessor.class);
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*\\}\\}");
    
    /**
     * Process a template string with the provided context variables
     * @param template Template string containing {{ variable }} placeholders
     * @param context Map of variable names to values
     * @return Processed string with substituted values
     */
    public String processTemplate(String template, Map<String, Object> context) {
        if (template == null) {
            return null;
        }
        
        logger.debug("Processing template: {}", template);
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object variableValue = context.get(variableName);
            
            if (variableValue == null) {
                logger.warn("Template variable '{}' not found in context", variableName);
                matcher.appendReplacement(result, "");
            } else {
                String replacement = variableValue.toString().replace("$", "\\$");
                matcher.appendReplacement(result, replacement);
            }
        }
        
        matcher.appendTail(result);
        
        String processed = result.toString();
        logger.debug("Processed template result: {}", processed);
        
        return processed;
    }
    
    /**
     * Process a template string with nested objects access
     * Supports dot notation like {{ user.name }}
     * @param template Template string containing {{ variable.property }} placeholders
     * @param context Map of variable names to values
     * @return Processed string with substituted values
     */
    public String processTemplateAdvanced(String template, Map<String, Object> context) {
        if (template == null) {
            return null;
        }
        
        logger.debug("Processing advanced template: {}", template);
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String variablePath = matcher.group(1).trim();
            String replacement;
            
            if (variablePath.contains(".")) {
                // Handle nested property access with dot notation
                String[] parts = variablePath.split("\\.");
                Object currentValue = context.get(parts[0]);
                
                for (int i = 1; i < parts.length && currentValue != null; i++) {
                    if (currentValue instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) currentValue;
                        currentValue = map.get(parts[i]);
                    } else {
                        // Cannot navigate further, object is not a map
                        currentValue = null;
                        break;
                    }
                }
                
                if (currentValue == null) {
                    logger.warn("Template path '{}' not found in context", variablePath);
                    replacement = "";
                } else {
                    replacement = currentValue.toString().replace("$", "\\$");
                }
            } else {
                // Simple variable access
                Object variableValue = context.get(variablePath);
                
                if (variableValue == null) {
                    logger.warn("Template variable '{}' not found in context", variablePath);
                    replacement = "";
                } else {
                    replacement = variableValue.toString().replace("$", "\\$");
                }
            }
            
            matcher.appendReplacement(result, replacement);
        }
        
        matcher.appendTail(result);
        
        String processed = result.toString();
        logger.debug("Processed advanced template result: {}", processed);
        
        return processed;
    }
}