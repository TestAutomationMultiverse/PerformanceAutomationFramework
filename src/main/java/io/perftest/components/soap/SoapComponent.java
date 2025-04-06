package io.perftest.components.soap;

import io.perftest.components.core.Component;
import io.perftest.components.xml.XmlComponent;
import io.perftest.entities.request.SoapRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * SOAP Component implementation for processing SOAP request entities.
 * 
 * <p>This component transforms {@link SoapRequestEntity} objects into JMeter HTTP samplers
 * configured to perform SOAP web service requests. It handles SOAP-specific aspects
 * such as selecting the appropriate content type based on the SOAP version and
 * formatting the request according to SOAP standards.</p>
 * 
 * <p>The component delegates most of the processing to the {@link XmlComponent} since
 * SOAP is based on XML, but adds SOAP-specific handling like content type selection
 * based on the SOAP version (1.1 or 1.2).</p>
 * 
 * <p>SOAP 1.1 requests use "text/xml" content type, while SOAP 1.2 requests use
 * "application/soap+xml" content type, following the respective SOAP specifications.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * SoapRequestEntity request = new SoapRequestEntity()
 *     .setUrl("https://soap.example.com/service")
 *     .setBody("&lt;soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"&gt;...")
 *     .setSoapVersion("1.2")
 *     .addHeader("SOAPAction", "GetData");
 *     
 * SoapComponent component = new SoapComponent();
 * DslHttpSampler sampler = component.process(request);
 * </pre>
 * 
 * @since 1.0
 */
public class SoapComponent implements Component<SoapRequestEntity, DslHttpSampler> {
    private static final Logger logger = LoggerFactory.getLogger(SoapComponent.class);
    
    /** The XML component used for processing the underlying XML structure of SOAP requests */
    private final XmlComponent xmlComponent = new XmlComponent();
    
    /**
     * Processes a SOAP request entity and converts it to a JMeter HTTP sampler.
     * 
     * <p>This method sets the appropriate content type based on the SOAP version
     * specified in the entity (1.1 or 1.2), then delegates the actual processing
     * to the XML component since SOAP messages are effectively XML messages with
     * specific structure and content types.</p>
     * 
     * <p>For SOAP 1.1, the content type is set to "text/xml", and for SOAP 1.2,
     * it is set to "application/soap+xml", as per the respective specifications.</p>
     * 
     * @param entity The SOAP request entity to process, must not be null
     * @return A configured JMeter HTTP sampler ready to be added to a test plan
     * @throws io.perftest.exception.ComponentException If the XML component processing fails
     */
    @Override
    public DslHttpSampler process(SoapRequestEntity entity) {
        logger.info("Processing SOAP request for URL: {}", entity.getUrl());
        
        // Update the content-type based on SOAP version
        if ("1.2".equals(entity.getSoapVersion())) {
            // SOAP 1.2 uses application/soap+xml content type
            logger.debug("Using SOAP 1.2 content type: application/soap+xml");
            entity.setContentType("application/soap+xml");
        } else {
            // Default to SOAP 1.1 content type (text/xml)
            logger.debug("Using SOAP 1.1 content type: text/xml");
            entity.setContentType("text/xml");
        }
        
        // Delegate processing to the XML component since SOAP is based on XML
        DslHttpSampler sampler = xmlComponent.process(entity);
        
        return sampler;
    }
}
