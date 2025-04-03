package io.perftest.core.templating;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import io.perftest.core.logger.TestLogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Template engine for generating complex JSON and XML payloads using Jinja2-like syntax.
 */
public class TemplateEngine {
    private static final Logger LOGGER = TestLogger.getLogger(TemplateEngine.class);
    
    private final Jinjava jinjava;
    private final Map<String, Object> globalContext = new HashMap<>();
    
    /**
     * Create a new template engine
     */
    public TemplateEngine() {
        JinjavaConfig config = JinjavaConfig.newBuilder()
                .withFailOnUnknownTokens(false)
                .withNestedInterpretationEnabled(true)
                .withEnableRecursiveMacroCalls(true)
                .build();
        
        this.jinjava = new Jinjava(config);
        LOGGER.debug("Initialized TemplateEngine");
    }
    
    /**
     * Add a value to the global context
     * 
     * @param key The context key
     * @param value The context value
     */
    public void addGlobalContext(String key, Object value) {
        globalContext.put(key, value);
    }
    
    /**
     * Add multiple values to the global context
     * 
     * @param context The context map to add
     */
    public void addGlobalContext(Map<String, Object> context) {
        globalContext.putAll(context);
    }
    
    /**
     * Render a template string with context
     * 
     * @param template The template string
     * @param context The context map
     * @return The rendered template
     */
    public String render(String template, Map<String, Object> context) {
        try {
            Map<String, Object> combinedContext = new HashMap<>(globalContext);
            if (context != null) {
                combinedContext.putAll(context);
            }
            
            return jinjava.render(template, combinedContext);
        } catch (FatalTemplateErrorsException e) {
            LOGGER.error("Failed to render template", e);
            throw new RuntimeException("Template rendering failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Render a template string with the global context
     * 
     * @param template The template string
     * @return The rendered template
     */
    public String render(String template) {
        return render(template, null);
    }
    
    /**
     * Render a template file with context
     * 
     * @param templatePath The path to the template file
     * @param context The context map
     * @return The rendered template
     * @throws IOException If the template file cannot be read
     */
    public String renderFile(String templatePath, Map<String, Object> context) throws IOException {
        Path path = Paths.get(templatePath);
        String template = Files.readString(path, StandardCharsets.UTF_8);
        return render(template, context);
    }
    
    /**
     * Render a template file with the global context
     * 
     * @param templatePath The path to the template file
     * @return The rendered template
     * @throws IOException If the template file cannot be read
     */
    public String renderFile(String templatePath) throws IOException {
        return renderFile(templatePath, null);
    }
    
    /**
     * Get the underlying Jinjava instance
     * 
     * @return The Jinjava instance
     */
    public Jinjava getJinjava() {
        return jinjava;
    }
    
    /**
     * Get the global context
     * 
     * @return The global context map
     */
    public Map<String, Object> getGlobalContext() {
        return globalContext;
    }
}
