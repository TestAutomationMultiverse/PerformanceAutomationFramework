package io.perftest.entities.request;

import io.perftest.core.config.ConfigManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity representing a SOAP request
 */
public class SoapRequestEntity extends RequestEntity {
    private static final Logger logger = LoggerFactory.getLogger(SoapRequestEntity.class);
    
    private String xmlBody;
    private String soapAction;
    private String soapVersion;
    private Map<String, Object> variables;  // Template variables
    private static Map<String, Object> defaults;
    
    // Static initializer to load default values from configuration
    static {
        try {
            ConfigManager configManager = new ConfigManager();
            configManager.loadConfigFromResource("soap-config.yml");
            Map<String, Object> soapConfig = configManager.getConfigForProtocol("soap");
            if (soapConfig != null && soapConfig.containsKey("defaults")) {
                defaults = (Map<String, Object>) soapConfig.get("defaults");
            } else {
                defaults = new HashMap<>();
                defaults.put("http_method", "POST");
                defaults.put("default_soap_version", "1.1");
                defaults.put("content_type_11", "text/xml;charset=UTF-8");
                defaults.put("content_type_12", "application/soap+xml;charset=UTF-8");
                defaults.put("follow_redirects", true);
                
                // Default SOAP 1.1 envelope
                defaults.put("envelope_11", 
                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "  <soap:Body>\n" +
                    "    $PAYLOAD$\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>");
                
                // Default SOAP 1.2 envelope
                defaults.put("envelope_12", 
                    "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                    "  <soap:Body>\n" +
                    "    $PAYLOAD$\n" +
                    "  </soap:Body>\n" +
                    "</soap:Envelope>");
            }
        } catch (IOException e) {
            logger.warn("Failed to load SOAP configuration defaults", e);
            defaults = new HashMap<>();
            defaults.put("http_method", "POST");
            defaults.put("default_soap_version", "1.1");
            defaults.put("content_type_11", "text/xml;charset=UTF-8");
            defaults.put("content_type_12", "application/soap+xml;charset=UTF-8");
            defaults.put("follow_redirects", true);
        }
    }
    
    /**
     * Creates a new SOAP request entity with the specified URL
     * @param url URL for the SOAP endpoint
     */
    public SoapRequestEntity(String url) {
        super(url);
        
        // Get HTTP method from config or use default POST
        String method = (String) defaults.getOrDefault("http_method", "POST");
        setMethod(method);
        
        // Get default SOAP version from config or use 1.1
        this.soapVersion = (String) defaults.getOrDefault("default_soap_version", "1.1");
        
        // Set default Content-Type header based on SOAP version
        String contentType = "1.2".equals(this.soapVersion) ? 
            (String) defaults.getOrDefault("content_type_12", "application/soap+xml;charset=UTF-8") : 
            (String) defaults.getOrDefault("content_type_11", "text/xml;charset=UTF-8");
        
        addHeader("Content-Type", contentType);
        
        // Initialize variables map
        this.variables = new HashMap<>();
    }
    
    /**
     * Get the XML body
     * @return XML/SOAP body
     */
    public String getXmlBody() {
        return xmlBody;
    }
    
    /**
     * Set the XML body
     * @param xmlBody XML/SOAP body
     * @return this instance for chaining
     */
    public SoapRequestEntity setXmlBody(String xmlBody) {
        this.xmlBody = xmlBody;
        return this;
    }
    
    /**
     * Get the SOAP action
     * @return SOAP action
     */
    public String getSoapAction() {
        return soapAction;
    }
    
    /**
     * Set the SOAP action
     * @param soapAction SOAP action
     * @return this instance for chaining
     */
    public SoapRequestEntity setSoapAction(String soapAction) {
        this.soapAction = soapAction;
        return this;
    }
    
    /**
     * Get the SOAP version
     * @return SOAP version (1.1 or 1.2)
     */
    public String getSoapVersion() {
        return soapVersion;
    }
    
    /**
     * Set the SOAP version
     * @param soapVersion SOAP version (1.1 or 1.2)
     * @return this instance for chaining
     */
    public SoapRequestEntity setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
        
        // Update Content-Type header based on new SOAP version
        String contentType = "1.2".equals(soapVersion) ? 
            (String) defaults.getOrDefault("content_type_12", "application/soap+xml;charset=UTF-8") : 
            (String) defaults.getOrDefault("content_type_11", "text/xml;charset=UTF-8");
        
        // Replace existing Content-Type header
        for (String key : getHeaders().keySet()) {
            if ("Content-Type".equalsIgnoreCase(key)) {
                getHeaders().remove(key);
                break;
            }
        }
        addHeader("Content-Type", contentType);
        
        return this;
    }
    
    /**
     * Get template variables
     * @return Map of template variables
     */
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    /**
     * Set all template variables
     * @param variables Map of template variables
     * @return this instance for chaining
     */
    public SoapRequestEntity setVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }
    
    /**
     * Add a template variable
     * @param name Variable name
     * @param value Variable value
     * @return this instance for chaining
     */
    public SoapRequestEntity addVariable(String name, Object value) {
        this.variables.put(name, value);
        return this;
    }
    
    /**
     * Get the appropriate SOAP envelope template based on SOAP version
     * @return SOAP envelope template
     */
    public String getSoapEnvelopeTemplate() {
        return "1.2".equals(soapVersion) ? 
            (String) defaults.getOrDefault("envelope_12", 
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                "  <soap:Body>\n" +
                "    $PAYLOAD$\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>") : 
            (String) defaults.getOrDefault("envelope_11", 
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    $PAYLOAD$\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>");
    }
    
    /**
     * Get the follow redirects setting from configuration
     * @return true if redirects should be followed, false otherwise
     */
    public boolean getFollowRedirects() {
        return Boolean.TRUE.equals(defaults.getOrDefault("follow_redirects", Boolean.TRUE));
    }
    
    /**
     * Get all defaults from configuration
     * @return Map of default configuration values
     */
    public static Map<String, Object> getDefaults() {
        return defaults;
    }
}