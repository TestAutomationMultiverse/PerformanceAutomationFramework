package io.perftest.protocol;

import io.perftest.model.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

/**
 * JDBC Protocol implementation for database operations
 */
public class JdbcProtocol implements Protocol {
    
    @Override
    public String getName() {
        return "jdbc";
    }
    
    @Override
    public Response execute(String endpoint, String method, String body, 
                         Map<String, String> headers, Map<String, String> params) throws Exception {
        
        Response response = new Response();
        Connection connection = null;
        
        try {
            // Extract connection properties
            String url = endpoint;
            String username = headers != null ? headers.get("username") : null;
            String password = headers != null ? headers.get("password") : null;
            
            // Connect to the database
            connection = DriverManager.getConnection(url, username, password);
            
            // Create statement
            Statement statement = connection.createStatement();
            
            // Execute SQL query
            String sql = body;
            StringBuilder responseBody = new StringBuilder();
            
            // Handle query vs update
            if (sql.trim().toLowerCase().startsWith("select")) {
                ResultSet resultSet = statement.executeQuery(sql);
                
                // Convert ResultSet to JSON
                int columnCount = resultSet.getMetaData().getColumnCount();
                responseBody.append("[");
                boolean firstRow = true;
                
                while (resultSet.next()) {
                    if (!firstRow) {
                        responseBody.append(",");
                    }
                    
                    responseBody.append("{");
                    for (int i = 1; i <= columnCount; i++) {
                        if (i > 1) {
                            responseBody.append(",");
                        }
                        
                        String columnName = resultSet.getMetaData().getColumnName(i);
                        String value = resultSet.getString(i);
                        responseBody.append("\"").append(columnName).append("\":");
                        
                        if (value == null) {
                            responseBody.append("null");
                        } else {
                            responseBody.append("\"").append(value.replace("\"", "\\\"")).append("\"");
                        }
                    }
                    responseBody.append("}");
                    firstRow = false;
                }
                responseBody.append("]");
                
                resultSet.close();
                response.setStatusCode(200);
            } else {
                // Handle UPDATE, INSERT, DELETE, etc.
                int updateCount = statement.executeUpdate(sql);
                responseBody.append("{\"rowsAffected\":").append(updateCount).append("}");
                response.setStatusCode(200);
            }
            
            statement.close();
            response.setBody(responseBody.toString());
            
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            throw new Exception("JDBC Protocol Error: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        return response;
    }
}
