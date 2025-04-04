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
 * Component for processing XML/SOAP requests
 */
public class XmlComponent implements Component<XmlRequestEntity, DslHttpSampler> {
    private static final Logger logger = LoggerFactory.getLogger(XmlComponent.class);
    
    @Override
    public DslHttpSampler process(XmlRequestEntity entity) {
        logger.info("Processing XML/SOAP request for URL: {}", entity.getUrl());
        
        // Build HTTP sampler with URL and method
        DslHttpSampler sampler = httpSampler(entity.getUrl())
            .method(entity.getMethod());
        
        // Add XML body if available
        if (entity.getXmlBody() != null && !entity.getXmlBody().isEmpty()) {
            sampler = sampler.body(entity.getXmlBody());
            
            // Add content type header (default to text/xml for SOAP)
            String contentType = entity.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "text/xml";
            }
            // Use header approach for content type as directly using contentType method might not be compatible
            sampler = sampler.header("Content-Type", contentType);
        }
        
        // Add all headers
        if (entity.getHeaders() != null && !entity.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : entity.getHeaders().entrySet()) {
                sampler = sampler.header(header.getKey(), header.getValue());
            }
        }
        
        // Set timeouts
        if (entity.getConnectTimeout() > 0) {
            sampler = sampler.connectionTimeout(Duration.ofMillis(entity.getConnectTimeout()));
        }
        
        if (entity.getResponseTimeout() > 0) {
            sampler = sampler.responseTimeout(Duration.ofMillis(entity.getResponseTimeout()));
        }
        
        // Add SOAP action header if available
        if (entity.getSoapAction() != null && !entity.getSoapAction().isEmpty()) {
            sampler = sampler.header("SOAPAction", entity.getSoapAction());
        }
        
        return sampler;
    }
}
