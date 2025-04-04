package io.perftest.exception;

import io.perftest.config.ConfigManager;
import io.perftest.config.YamlConfigUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for YAML configuration loading
 */
@DisplayName("YAML Configuration Tests")
public class YamlConfigTest {
    
    /**
     * Test configuration class
     */
    public static class TestConfig {
        public String name;
        public int value;
    }
    
    /**
     * Test loading YAML configuration
     */
    @Test
    @DisplayName("Test loading YAML configuration")
    void testLoadYamlConfig() throws IOException {
        TestConfig config = YamlConfigUtil.load("test-config.yml", TestConfig.class);
        
        assertNotNull(config);
        assertEquals("Test Configuration", config.name);
        assertEquals(42, config.value);
    }
    
    /**
     * Test loading YAML configuration with error handling
     */
    @Test
    @DisplayName("Test loading YAML configuration with error handling")
    void testSafeLoadYamlConfig() {
        Result<TestConfig> result = YamlConfigUtil.safeLoad("test-config.yml", TestConfig.class);
        
        assertTrue(result.isSuccess());
        TestConfig config = result.getValue();
        assertEquals("Test Configuration", config.name);
        assertEquals(42, config.value);
        
        Result<TestConfig> notFoundResult = YamlConfigUtil.safeLoad("non-existent.yml", TestConfig.class);
        assertTrue(notFoundResult.isFailure());
        assertEquals(ErrorCode.CONFIG_FILE_NOT_FOUND, notFoundResult.getErrorCode());
    }
    
    /**
     * Test loading YAML configuration through ConfigManager
     */
    @Test
    @DisplayName("Test loading YAML configuration through ConfigManager")
    void testConfigManagerYamlLoading() {
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.clearConfig();
        
        TestConfig config = configManager.loadFromYaml("test-config.yml", TestConfig.class);
        
        assertNotNull(config);
        assertEquals("Test Configuration", config.name);
        assertEquals(42, config.value);
        
        // Verify it was stored in the config map
        assertTrue(configManager.hasConfig("test-config"));
        TestConfig storedConfig = configManager.getConfig("test-config");
        assertEquals("Test Configuration", storedConfig.name);
        assertEquals(42, storedConfig.value);
    }
    
    /**
     * Test loading YAML configuration through ConfigManager with error handling
     */
    @Test
    @DisplayName("Test loading YAML configuration through ConfigManager with error handling")
    void testConfigManagerSafeYamlLoading() {
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.clearConfig();
        
        Result<TestConfig> result = configManager.safeLoadFromYaml("test-config.yml", TestConfig.class);
        
        assertTrue(result.isSuccess());
        TestConfig config = result.getValue();
        assertEquals("Test Configuration", config.name);
        assertEquals(42, config.value);
        
        // Verify it was stored in the config map
        assertTrue(configManager.hasConfig("test-config"));
        
        Result<TestConfig> notFoundResult = configManager.safeLoadFromYaml("non-existent.yml", TestConfig.class);
        assertTrue(notFoundResult.isFailure());
        assertEquals(ErrorCode.CONFIG_FILE_NOT_FOUND, notFoundResult.getErrorCode());
    }
    
    /**
     * Test error cases for YAML loading
     */
    @Test
    @DisplayName("Test error cases for YAML loading")
    void testYamlLoadingErrors() {
        // Test null path
        PerfTestException nullPathException = assertThrows(PerfTestException.class, () ->
                YamlConfigUtil.load(null, TestConfig.class));
        assertEquals(ErrorCode.CONFIG_FILE_NOT_FOUND, nullPathException.getErrorCode());
        
        // Test null class
        PerfTestException nullClassException = assertThrows(PerfTestException.class, () ->
                YamlConfigUtil.load("test-config.yml", null));
        assertEquals(ErrorCode.CONFIG_PARSE_ERROR, nullClassException.getErrorCode());
        
        // Test non-existent file
        PerfTestException notFoundException = assertThrows(PerfTestException.class, () ->
                YamlConfigUtil.load("non-existent.yml", TestConfig.class));
        assertEquals(ErrorCode.CONFIG_FILE_NOT_FOUND, notFoundException.getErrorCode());
    }
}
