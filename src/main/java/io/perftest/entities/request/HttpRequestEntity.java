package io.perftest.entities.request;

/**
 * Entity representing an HTTP request
 */
public class HttpRequestEntity extends RequestEntity {
    private String body;
    private String contentType = "application/json";
    private int expectedStatus = 200;
    
    /**
     * Default constructor
     */
    public HttpRequestEntity() {
        super();
    }
    
    /**
     * Constructor with URL
     * 
     * @param url The URL for the request
     */
    public HttpRequestEntity(String url) {
        super();
        setUrl(url);
    }
    
    /**
     * @return Request body content
     */
    public String getBody() {
        return body;
    }
    
    /**
     * @param body Request body content
     */
    public void setBody(String body) {
        this.body = body;
    }
    
    /**
     * Sets the payload (alias for setBody)
     * 
     * @param payload Request body content
     */
    public void setPayload(String payload) {
        this.body = payload;
    }
    
    /**
     * @return Content type for the request
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * @param contentType Content type for the request
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Set the name of the request
     * 
     * @param name Request name
     */
    public void setName(String name) {
        setProperty("name", name);
    }
    
    /**
     * Get the expected HTTP status code
     * 
     * @return Expected status code
     */
    public int getExpectedStatus() {
        return expectedStatus;
    }
    
    /**
     * Set the expected HTTP status code
     * 
     * @param expectedStatus Expected status code
     */
    public void setExpectedStatus(int expectedStatus) {
        this.expectedStatus = expectedStatus;
    }
}
