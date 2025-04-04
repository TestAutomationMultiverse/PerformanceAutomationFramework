package io.perftest.entities.request;

/**
 * Entity representing a XML/SOAP request
 */
public class XmlRequestEntity extends RequestEntity {
    private String body;
    private String soapAction;
    private String contentType;
    
    /**
     * Default constructor
     */
    public XmlRequestEntity() {
        super();
        setContentType("text/xml");
    }
    
    /**
     * @return XML request body content
     */
    public String getBody() {
        return body;
    }
    
    /**
     * @param body XML request body content
     */
    public void setBody(String body) {
        this.body = body;
    }
    
    /**
     * Alias for getBody()
     * @return XML request body content
     */
    public String getXmlBody() {
        return getBody();
    }
    
    /**
     * Alias for setBody()
     * @param xmlBody XML request body content
     */
    public void setXmlBody(String xmlBody) {
        setBody(xmlBody);
    }
    
    /**
     * @return SOAP action for the request
     */
    public String getSoapAction() {
        return soapAction;
    }
    
    /**
     * @param soapAction SOAP action for the request
     */
    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }
    
    /**
     * @return Content type of the request
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * @param contentType Content type of the request
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
