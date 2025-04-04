package io.perftest.components.soap;

import io.perftest.components.core.Component;
import io.perftest.components.xml.XmlComponent;
import io.perftest.entities.request.SoapRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * Component for processing SOAP requests
 */
public class SoapComponent implements Component<SoapRequestEntity, DslHttpSampler> {
    private static final Logger logger = LoggerFactory.getLogger(SoapComponent.class);
    private final XmlComponent xmlComponent = new XmlComponent();
    
    @Override
    public DslHttpSampler process(SoapRequestEntity entity) {
        logger.info("Processing SOAP request for URL: {}", entity.getUrl());
        
        // Update the content-type based on SOAP version if needed
        if ("1.2".equals(entity.getSoapVersion())) {
            entity.setContentType("application/soap+xml");
        } else {
            // Default to SOAP 1.1 content type
            entity.setContentType("text/xml");
        }
        
        // Utilize the existing XML component for processing
        DslHttpSampler sampler = xmlComponent.process(entity);
        
        return sampler;
    }
}
