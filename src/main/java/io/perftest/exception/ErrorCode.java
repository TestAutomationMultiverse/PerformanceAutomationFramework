package io.perftest.exception;

/**
 * Enum for error codes
 */
public enum ErrorCode {
    /**
     * General error
     */
    GENERAL_ERROR(1000, "General error"),
    
    /**
     * Validation error
     */
    VALIDATION_ERROR(1001, "Validation error"),
    
    /**
     * Configuration error
     */
    CONFIG_ERROR(2000, "Configuration error"),
    
    /**
     * Configuration file not found
     */
    CONFIG_FILE_NOT_FOUND(2001, "Configuration file not found"),
    
    /**
     * Error parsing configuration
     */
    CONFIG_PARSE_ERROR(2002, "Error parsing configuration"),
    
    /**
     * Configuration validation error
     */
    CONFIG_VALIDATION_ERROR(2003, "Configuration validation error"),
    
    /**
     * IO error
     */
    IO_ERROR(3000, "IO error"),
    
    /**
     * Network error
     */
    NETWORK_ERROR(3001, "Network error"),
    
    /**
     * File system error
     */
    FILE_SYSTEM_ERROR(3002, "File system error"),
    
    /**
     * Test engine error
     */
    TEST_ENGINE_ERROR(4000, "Test engine error"),
    
    /**
     * Test execution error
     */
    TEST_EXECUTION_ERROR(4001, "Test execution error"),
    
    /**
     * Test component error
     */
    TEST_COMPONENT_ERROR(4002, "Test component error"),
    
    /**
     * Test system error
     */
    TEST_SYSTEM_ERROR(4003, "Test system error"),
    
    /**
     * Test setup error
     */
    TEST_SETUP_ERROR(4004, "Test setup error"),
    
    /**
     * Reporting error
     */
    REPORTING_ERROR(4005, "Reporting error"),
    
    /**
     * HTTP error
     */
    HTTP_ERROR(5000, "HTTP error"),
    
    /**
     * GraphQL error
     */
    GRAPHQL_ERROR(5001, "GraphQL error"),
    
    /**
     * SOAP error
     */
    SOAP_ERROR(5002, "SOAP error"),
    
    /**
     * JDBC error
     */
    JDBC_ERROR(5003, "JDBC error"),
    
    /**
     * Template error
     */
    TEMPLATE_ERROR(6000, "Template error"),
    
    /**
     * Data generation error
     */
    DATA_GENERATION_ERROR(6001, "Data generation error"),
    
    /**
     * Unexpected error
     */
    UNEXPECTED_ERROR(9999, "Unexpected error");
    
    private final int code;
    private final String description;
    
    /**
     * Constructor
     * @param code Error code
     * @param description Error description
     */
    ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get the error code
     * @return Error code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Get the error description
     * @return Error description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get a string representation of the error code
     * @return String representation
     */
    @Override
    public String toString() {
        return String.format("[%d] %s", code, description);
    }
}
