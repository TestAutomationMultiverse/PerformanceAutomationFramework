package io.perftest.data;
import java.util.Random;
import java.util.Locale;

import com.github.javafaker.Faker;
import io.perftest.util.logger.TestLogger;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generates random data for test entities and ensures data uniqueness when required.
 */
public class DataGenerator {
    private static final Logger LOGGER = TestLogger.getLogger(DataGenerator.class);
    
    private final Faker faker;
    private final Random random;
    private final Map<String, Set<Object>> uniqueValues = new ConcurrentHashMap<>();
    
    public DataGenerator() {
        this(System.currentTimeMillis());
    }
    
    public DataGenerator(long seed) {
        this.faker = new Faker(new Locale("en-US"), new Random(seed));
        this.random = new Random(seed);
        LOGGER.debug("DataGenerator initialized with seed: {}", seed);
    }
    
    /**
     * Generate a random string with a specified length
     * 
     * @param length The length of the string to generate
     * @return A random alphanumeric string
     */
    public String randomString(int length) {
        return faker.lorem().fixedString(length);
    }
    
    /**
     * Generate a random integer within a range
     * 
     * @param min The minimum value (inclusive)
     * @param max The maximum value (inclusive)
     * @return A random integer
     */
    public int randomInt(int min, int max) {
        return faker.number().numberBetween(min, max);
    }
    
    /**
     * Generate a random double within a range
     * 
     * @param min The minimum value (inclusive)
     * @param max The maximum value (exclusive)
     * @return A random double
     */
    public double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
    
    /**
     * Generate a random email address
     * 
     * @return A random email address
     */
    public String randomEmail() {
        return faker.internet().emailAddress();
    }
    
    /**
     * Generate a random username
     * 
     * @return A random username
     */
    public String randomUsername() {
        return faker.name().username();
    }
    
    /**
     * Generate a random full name
     * 
     * @return A random full name
     */
    public String randomFullName() {
        return faker.name().fullName();
    }
    
    /**
     * Generate a random phone number
     * 
     * @return A random phone number
     */
    public String randomPhoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }
    
    /**
     * Generate a random UUID
     * 
     * @return A random UUID
     */
    public String randomUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generate a random element from a list
     * 
     * @param list The list to choose from
     * @param <T> The type of elements in the list
     * @return A random element from the list
     */
    public <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }
    
    /**
     * Generate a random date between two dates
     * 
     * @param startInclusive The start date (inclusive)
     * @param endExclusive The end date (exclusive)
     * @return A random date between the two dates
     */
    public Date randomDate(Date startInclusive, Date endExclusive) {
        return faker.date().between(startInclusive, endExclusive);
    }
    
    /**
     * Generate a unique value using a supplier function
     * 
     * @param category The category for uniqueness (e.g., "email", "username")
     * @param supplier The supplier function to generate values
     * @param <T> The type of the value
     * @return A unique value
     * @throws IllegalStateException if a unique value cannot be generated after multiple attempts
     */
    public <T> T uniqueValue(String category, Supplier<T> supplier) {
        Set<Object> usedValues = uniqueValues.computeIfAbsent(category, k -> ConcurrentHashMap.newKeySet());
        
        // Try to generate a unique value with a reasonable number of attempts
        int maxAttempts = 100;
        for (int i = 0; i < maxAttempts; i++) {
            T value = supplier.get();
            if (usedValues.add(value)) {
                return value;
            }
        }
        
        LOGGER.error("Failed to generate a unique value for category {} after {} attempts", category, maxAttempts);
        throw new IllegalStateException("Unable to generate a unique value for category: " + category);
    }
    
    /**
     * Generate a list of unique values
     * 
     * @param category The category for uniqueness
     * @param supplier The supplier function to generate values
     * @param count The number of unique values to generate
     * @param <T> The type of the values
     * @return A list of unique values
     */
    public <T> List<T> uniqueValues(String category, Supplier<T> supplier, int count) {
        return random
                .ints(0, Integer.MAX_VALUE)
                .distinct()
                .limit(count)
                .mapToObj(i -> uniqueValue(category, supplier))
                .collect(Collectors.toList());
    }
    
    /**
     * Clear all stored unique values
     */
    public void clearUniqueValues() {
        uniqueValues.clear();
    }
    
    /**
     * Clear stored unique values for a specific category
     * 
     * @param category The category to clear
     */
    public void clearUniqueValues(String category) {
        uniqueValues.remove(category);
    }
    
    /**
     * Get the underlying Faker instance
     * 
     * @return The Faker instance
     */
    public Faker getFaker() {
        return new Faker(new Random(faker.random().nextLong()));
    }
}
