package io.perftest.entities.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a SOAP request
 */
public class SoapRequestEntity extends XmlRequestEntity {
    private String soapVersion = "1.1";
    private Map<String, Object> variables = new HashMap<>();
    private String payload;

    /**
     * Default constructor
     */
    public SoapRequestEntity() {
        super();
        setMethod("POST");
    }

    /**
     * Constructor with URL
     * 
     * @param url The SOAP endpoint URL
     */
    public SoapRequestEntity(String url) {
        super();
        setUrl(url);
        setMethod("POST");
    }

    /**
     * Constructor with URL and payload
     * 
     * @param url The SOAP endpoint URL
     * @param payload The SOAP XML payload
     */
    public SoapRequestEntity(String url, String payload) {
        super();
        setUrl(url);
        setMethod("POST");
        setPayload(payload);
    }

    /**
     * Set the SOAP XML payload
     * 
     * @param payload SOAP XML payload
     */
    public void setPayload(String payload) {
        this.payload = payload;
        setBody(payload);
    }

    /**
     * Get the SOAP XML payload
     * 
     * @return SOAP XML payload
     */
    public String getPayload() {
        return payload != null ? payload : getBody();
    }

    /**
     * @return SOAP version (1.1 or 1.2)
     */
    public String getSoapVersion() {
        return soapVersion;
    }

    /**
     * @param soapVersion SOAP version (1.1 or 1.2)
     */
    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;

        // Update content type based on SOAP version
        if ("1.2".equals(soapVersion)) {
            setContentType("application/soap+xml");
        } else {
            setContentType("text/xml");
        }
    }

    /**
     * @return Variables for template substitution
     */
    public Map<String, Object> getVariables() {
        if (variables == null)
            return null;
        return Collections.unmodifiableMap(variables);
    }

    /**
     * @param variables Variables for template substitution
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    /**
     * Add a variable for template substitution
     * 
     * @param name Variable name
     * @param value Variable value
     */
    public void addVariable(String name, Object value) {
        variables.put(name, value);
    }
}
