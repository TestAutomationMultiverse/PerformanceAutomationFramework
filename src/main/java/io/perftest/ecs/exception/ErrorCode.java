package io.perftest.ecs.exception;

/**
 * Enum for error codes within the ECS architecture.
 * Organizes error codes by their related component in the ECS pattern.
 */
public enum ErrorCode {
    // Entity-related error codes (1xxx)
    ENTITY_GENERAL_ERROR(1000, "Entity general error"),
    ENTITY_VALIDATION_ERROR(1001, "Entity validation error"),
    ENTITY_CREATION_ERROR(1002, "Entity creation error"),
    
    // Entity protocol-specific error codes (11xx-14xx)
    HTTP_ENTITY_ERROR(1100, "HTTP entity error"),
    GRAPHQL_ENTITY_ERROR(1200, "GraphQL entity error"),
    SOAP_ENTITY_ERROR(1300, "SOAP entity error"),
    JDBC_ENTITY_ERROR(1400, "JDBC entity error"),
    
    // Component-related error codes (2xxx)
    COMPONENT_GENERAL_ERROR(2000, "Component general error"),
    COMPONENT_INITIALIZATION_ERROR(2001, "Component initialization error"),
    COMPONENT_PROCESSING_ERROR(2002, "Component processing error"),
    
    // Component protocol-specific error codes (21xx-24xx)
    HTTP_COMPONENT_ERROR(2100, "HTTP component error"),
    GRAPHQL_COMPONENT_ERROR(2200, "GraphQL component error"),
    SOAP_COMPONENT_ERROR(2300, "SOAP component error"),
    JDBC_COMPONENT_ERROR(2400, "JDBC component error"),
    
    // System-related error codes (3xxx)
    SYSTEM_GENERAL_ERROR(3000, "System general error"),
    SYSTEM_INITIALIZATION_ERROR(3001, "System initialization error"),
    SYSTEM_EXECUTION_ERROR(3002, "System execution error"),
    
    // Test system-specific error codes (31xx)
    TEST_SYSTEM_ERROR(3100, "Test system error"),
    TEST_ENGINE_ERROR(3101, "Test engine error"),
    TEST_EXECUTION_ERROR(3102, "Test execution error"),
    TEST_REPORTING_ERROR(3103, "Test reporting error"),
    
    // Configuration-related error codes (4xxx)
    CONFIG_GENERAL_ERROR(4000, "Configuration general error"),
    CONFIG_FILE_NOT_FOUND(4001, "Configuration file not found"),
    CONFIG_PARSE_ERROR(4002, "Error parsing configuration"),
    CONFIG_VALIDATION_ERROR(4003, "Configuration validation error"),
    
    // IO and resource-related error codes (5xxx)
    IO_ERROR(5000, "IO error"),
    NETWORK_ERROR(5001, "Network error"),
    FILE_SYSTEM_ERROR(5002, "File system error"),
    
    // Template-related error codes (6xxx)
    TEMPLATE_ERROR(6000, "Template error"),
    TEMPLATE_RENDERING_ERROR(6001, "Template rendering error"),
    TEMPLATE_PARSING_ERROR(6002, "Template parsing error"),
    
    // Validation-related error codes (7xxx)
    VALIDATION_ERROR(7000, "Validation error"),
    SCHEMA_VALIDATION_ERROR(7001, "Schema validation error"),
    DATA_VALIDATION_ERROR(7002, "Data validation error"),
    
    // Unexpected errors (9xxx)
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
