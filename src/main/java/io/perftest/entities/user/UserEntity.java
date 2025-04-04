package io.perftest.entities.user;

import io.perftest.data.DataGenerator;
import io.perftest.entities.core.Entity;
import io.perftest.util.logger.TestLogger;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a virtual user in a performance test.
 * Contains user attributes and credentials for authentication.
 */
public class UserEntity extends Entity {
    private static final Logger LOGGER = TestLogger.getLogger(UserEntity.class);
    
    private final String userId;
    private final String username;
    private final String email;
    private String password;
    private final Map<String, String> attributes = new HashMap<>();
    private String authToken;
    private boolean authenticated = false;
    
    /**
     * Create a new user entity with generated data
     * 
     * @param dataGenerator The data generator
     */
    public UserEntity(DataGenerator dataGenerator) {
        this.userId = dataGenerator.uniqueValue("userId", () -> UUID.randomUUID().toString());
        this.username = dataGenerator.uniqueValue("username", dataGenerator::randomUsername);
        this.email = dataGenerator.uniqueValue("email", dataGenerator::randomEmail);
        this.password = dataGenerator.randomString(12);
        
        // Add some random attributes
        attributes.put("firstName", dataGenerator.getFaker().name().firstName());
        attributes.put("lastName", dataGenerator.getFaker().name().lastName());
        attributes.put("country", dataGenerator.getFaker().address().country());
        attributes.put("city", dataGenerator.getFaker().address().city());
        attributes.put("phone", dataGenerator.getFaker().phoneNumber().phoneNumber());
        
        LOGGER.debug("Created user entity: id={}, username={}, email={}", userId, username, email);
    }
    
    /**
     * Create a new user entity with specific values
     * 
     * @param userId The user ID
     * @param username The username
     * @param email The email address
     * @param password The password
     */
    public UserEntity(String userId, String username, String email, String password) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        
        LOGGER.debug("Created user entity with specific values: id={}, username={}, email={}", userId, username, email);
    }
    
    /**
     * Get the user ID
     * 
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Get the username
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Get the email address
     * 
     * @return The email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Get the password
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Set the password
     * 
     * @param password The password
     * @return This entity for chaining
     */
    public UserEntity setPassword(String password) {
        this.password = password;
        return this;
    }
    
    /**
     * Get the authentication token
     * 
     * @return The authentication token
     */
    public String getAuthToken() {
        return authToken;
    }
    
    /**
     * Set the authentication token
     * 
     * @param authToken The authentication token
     * @return This entity for chaining
     */
    public UserEntity setAuthToken(String authToken) {
        this.authToken = authToken;
        this.authenticated = true;
        return this;
    }
    
    /**
     * Check if the user is authenticated
     * 
     * @return True if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    /**
     * Set the authenticated status
     * 
     * @param authenticated The authenticated status
     * @return This entity for chaining
     */
    public UserEntity setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        return this;
    }
    
    /**
     * Get a user attribute
     * 
     * @param key The attribute key
     * @return The attribute value
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * Set a user attribute
     * 
     * @param key The attribute key
     * @param value The attribute value
     * @return This entity for chaining
     */
    public UserEntity setAttribute(String key, String value) {
        attributes.put(key, value);
        return this;
    }
    
    /**
     * Get all user attributes
     * 
     * @return The attributes map
     */
    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }
    
    /**
     * Get the user data as a map (for templates)
     * 
     * @return The user data map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("username", username);
        map.put("email", email);
        map.put("password", password);
        map.put("authenticated", authenticated);
        
        if (authenticated && authToken != null) {
            map.put("authToken", authToken);
        }
        
        map.put("attributes", attributes);
        
        return map;
    }
}
