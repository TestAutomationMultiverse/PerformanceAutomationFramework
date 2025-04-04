package io.perftest.exception;

/**
 * Exception thrown for GraphQL-related errors.
 */
public class GraphQLException extends PerfTestException {
    
    public GraphQLException(String message) {
        super(ErrorCode.GRAPHQL_ERROR, message);
    }
    
    public GraphQLException(String message, Throwable cause) {
        super(ErrorCode.GRAPHQL_ERROR, message, cause);
    }
    
    public GraphQLException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public GraphQLException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
