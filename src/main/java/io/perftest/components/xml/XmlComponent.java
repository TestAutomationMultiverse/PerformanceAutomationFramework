package io.perftest.components.xml;

import io.perftest.components.core.Component;
import io.perftest.entities.request.XmlRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

import java.time.Duration;
import java.util.Map;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

/**
 * XML Component implementation for processing XML and SOAP request entities.
 * 
 * <p>This component transforms {@link XmlRequestEntity} objects into JMeter HTTP samplers
 * configured to perform XML-based requests. It handles XML-specific aspects such as
 * setting the appropriate content type (text/xml by default) and managing XML payloads.</p>
 * 
 * <p>The component is used directly for XML requests and as a base for the more specialized
 * {@link io.perftest.components.soap.SoapComponent} which adds SOAP-specific behavior.
 * This allows code reuse while maintaining protocol-specific handling.</p>
 * 
 * <p>XML requests are sent as HTTP requests with XML payloads and the appropriate content
 * type headers. For SOAP requests, additional headers like SOAPAction may be added.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * XmlRequestEntity request = new XmlRequestEntity()
 *     .setUrl("https://xml-api.example.com/endpoint")
 *     .setMethod("POST")
 *     .setXmlBody("&lt;request&gt;&lt;parameter&gt;value&lt;/parameter&gt;&lt;/request&gt;")
 *     .setContentType("application/xml");
 *     
 * XmlComponent component = new XmlComponent();
 * DslHttpSampler sampler = component.process(request);
 * </pre>
 * 
 * @since 1.0
 */
public class XmlComponent implements Component<XmlRequestEntity, DslHttpSampler> {
    private static final Logger logger = LoggerFactory.getLogger(XmlComponent.class);
    
    /**
     * Processes an XML request entity and converts it to a JMeter HTTP sampler.
     * 
     * <p>This method extracts all relevant properties from the XML request entity
     * and configures a JMeter HTTP sampler accordingly. It handles URL, method, headers,
     * XML body content, content type, and timeout settings.</p>
     * 
     * <p>The method also handles SOAP-specific features like the SOAPAction header
     * when present, making it suitable for processing both generic XML and SOAP requests.</p>
     * 
     * @param entity The XML request entity to process, must not be null
     * @return A configured JMeter HTTP sampler ready to be added to a test plan
     * @throws io.perftest.exception.ComponentException If processing fails due to invalid entity data
     */
    @Override
    public DslHttpSampler process(XmlRequestEntity entity) {
        logger.info("Processing XML/SOAP request for URL: {}", entity.getUrl());
        
        // Build HTTP sampler with URL and method
        DslHttpSampler sampler = httpSampler(entity.getUrl())
            .method(entity.getMethod());
        
        // Add XML body if available
        if (entity.getXmlBody() != null && !entity.getXmlBody().isEmpty()) {
            sampler = sampler.body(entity.getXmlBody());
            
            // Add content type header (default to text/xml for XML/SOAP if not specified)
            String contentType = entity.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "text/xml";
                logger.debug("Using default content type: text/xml");
            } else {
                logger.debug("Using specified content type: {}", contentType);
            }
            
            // Use header approach for content type as directly using contentType method
            // might not be compatible with all JMeter versions
            sampler = sampler.header("Content-Type", contentType);
        }
        
        // Add all custom headers from the entity
        if (entity.getHeaders() != null && !entity.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : entity.getHeaders().entrySet()) {
                logger.debug("Adding header: {} = {}", header.getKey(), header.getValue());
                sampler = sampler.header(header.getKey(), header.getValue());
            }
        }
        
        // Set connection and response timeouts if specified
        if (entity.getConnectTimeout() > 0) {
            logger.debug("Setting connection timeout: {}ms", entity.getConnectTimeout());
            sampler = sampler.connectionTimeout(Duration.ofMillis(entity.getConnectTimeout()));
        }
        
        if (entity.getResponseTimeout() > 0) {
            logger.debug("Setting response timeout: {}ms", entity.getResponseTimeout());
            sampler = sampler.responseTimeout(Duration.ofMillis(entity.getResponseTimeout()));
        }
        
        // Add SOAP action header if available (for SOAP requests)
        if (entity.getSoapAction() != null && !entity.getSoapAction().isEmpty()) {
            logger.debug("Adding SOAPAction header: {}", entity.getSoapAction());
            sampler = sampler.header("SOAPAction", entity.getSoapAction());
        }
        
        return sampler;
    }
}
