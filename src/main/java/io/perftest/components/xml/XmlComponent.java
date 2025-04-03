package io.perftest.components.xml;

import io.perftest.core.data.TemplateProcessor;
import io.perftest.entities.request.SoapRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.util.Map;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;

/**
 * Component for XML and SOAP protocol operations
 */
public class XmlComponent {
    private static final Logger logger = LoggerFactory.getLogger(XmlComponent.class);
    private final TemplateProcessor templateProcessor;
    
    /**
     * Creates a new XML component
     */
    public XmlComponent() {
        this.templateProcessor = new TemplateProcessor();
    }
    
    /**
     * Creates a JMeter DSL HTTP sampler configured for a SOAP request
     * @param requestEntity SOAP request entity
     * @return Configured HTTP sampler for the SOAP request
     */
    public DslHttpSampler createSoapSampler(SoapRequestEntity requestEntity) {
        logger.info("Creating SOAP sampler for URL: {}", requestEntity.getUrl());
        
        // Process XML if templating is involved
        String xmlBody = requestEntity.getXmlBody();
        if (requestEntity.getVariables() != null && !requestEntity.getVariables().isEmpty()) {
            xmlBody = templateProcessor.processTemplate(xmlBody, requestEntity.getVariables());
        }
        
        // Get SOAP version
        String soapVersion = requestEntity.getSoapVersion();
        if (soapVersion == null) {
            // Get default SOAP version from config or use 1.1
            Map<String, Object> defaults = SoapRequestEntity.getDefaults();
            soapVersion = (String) defaults.getOrDefault("default_soap_version", "1.1");
        }
        
        // Wrap in SOAP envelope if not already wrapped
        if (!xmlBody.contains("<soap:Envelope") && !xmlBody.contains("<Envelope")) {
            // Get envelope template from entity, which pulls from config
            String envelopeTemplate = requestEntity.getSoapEnvelopeTemplate();
            xmlBody = envelopeTemplate.replace("$PAYLOAD$", xmlBody);
        }
        
        // Create HTTP sampler with debug logging
        logger.info("Creating SOAP request with body: {}", xmlBody);
        
        // Get the appropriate Content-Type header from the entity's headers
        String contentType = null;
        for (Map.Entry<String, String> header : requestEntity.getHeaders().entrySet()) {
            if ("Content-Type".equalsIgnoreCase(header.getKey())) {
                contentType = header.getValue();
                break;
            }
        }
        
        // Fallback to default Content-Type if not found in headers
        if (contentType == null) {
            Map<String, Object> defaults = SoapRequestEntity.getDefaults();
            contentType = "1.2".equals(soapVersion) ? 
                (String) defaults.getOrDefault("content_type_12", "application/soap+xml;charset=UTF-8") : 
                (String) defaults.getOrDefault("content_type_11", "text/xml;charset=UTF-8");
        }

        // Get HTTP method from entity
        String method = requestEntity.getMethod();
        if (method == null) {
            Map<String, Object> defaults = SoapRequestEntity.getDefaults();
            method = (String) defaults.getOrDefault("http_method", "POST");
        }
        
        // Create the HTTP sampler
        DslHttpSampler sampler = httpSampler(requestEntity.getUrl())
            .method(method)
            .header("Content-Type", contentType)
            .body(xmlBody)
            .followRedirects(requestEntity.getFollowRedirects());
        
        // Add SOAP Action header if present
        if (requestEntity.getSoapAction() != null && !requestEntity.getSoapAction().isEmpty()) {
            // For SOAP 1.2, the action goes in the Content-Type header
            if ("1.2".equals(soapVersion)) {
                sampler = sampler.header("Content-Type", contentType + ";action=\"" + requestEntity.getSoapAction() + "\"");
            } else {
                // For SOAP 1.1, use the SOAPAction header
                sampler = sampler.header("SOAPAction", requestEntity.getSoapAction());
            }
        }
        
        // Add custom headers
        for (Map.Entry<String, String> header : requestEntity.getHeaders().entrySet()) {
            // Skip Content-Type which we've already handled
            if (!"Content-Type".equalsIgnoreCase(header.getKey())) {
                sampler = sampler.header(header.getKey(), header.getValue());
            }
        }
        
        return sampler;
    }
}