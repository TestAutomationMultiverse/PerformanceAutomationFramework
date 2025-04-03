package io.perftest.examples;

import io.perftest.core.engine.TestEngine;
import io.perftest.entities.request.SoapRequestEntity;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example of SOAP/XML API test using the framework with Country Info Service
 */
public class SoapApiTest {

    /**
     * Test to get country name by country code using the CountryInfoService
     */
    @Test
    public void testCountryName() throws Exception {
        TestEngine testEngine = new TestEngine();
        
        // Create SOAP Request Entity with payload
        SoapRequestEntity soapRequestEntity = new SoapRequestEntity(
            "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso" // SOAP endpoint
        );
        
        // Set XML body
        soapRequestEntity.setXmlBody(
            "<CountryName xmlns=\"http://www.oorsprong.org/websamples.countryinfo\">" +
            "  <sCountryISOCode>US</sCountryISOCode>" +
            "</CountryName>"
        );
        
        // Set SOAP action
        soapRequestEntity.setSoapAction("http://www.oorsprong.org/websamples.countryinfo/CountryName");
        
        // Add XPath assertions
        soapRequestEntity.addAssertion("//*[local-name()='CountryNameResult']", "United States");
        
        // Set expected status code
        soapRequestEntity.setExpectedStatus(200);
        
        // Execute the test with 1 thread and 1 iteration
        TestPlanStats stats = testEngine.executeSoapTest(soapRequestEntity, 1, 1);
        
        // Print statistics and detailed error information
        System.out.println("CountryName SOAP test completed with: " + stats.overall().sampleTime().perc90().toMillis() + "ms");
        System.out.println("Error count: " + stats.overall().errorsCount());
        
        if (stats.overall().errorsCount() > 0) {
            System.out.println("Detailed errors: " + stats.overall().errors());
            double errorPercent = 100.0 * stats.overall().errorsCount() / stats.overall().samplesCount();
            System.out.println("Error percentage: " + errorPercent + "%");
            System.out.println("Sample count: " + stats.overall().samplesCount());
        }
        
        // Verify no errors occurred
        assertThat(stats.overall().errorsCount())
            .withFailMessage("Test had %d errors", stats.overall().errorsCount())
            .isEqualTo(0);
    }
    
    /**
     * Test to get country currency using templates for dynamic country code
     */
    @Test
    public void testCountryCurrencyWithTemplates() throws Exception {
        TestEngine testEngine = new TestEngine();
        
        // Create SOAP Request Entity with templated payload
        SoapRequestEntity soapRequestEntity = new SoapRequestEntity(
            "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso"
        );
        
        // Set SOAP action
        soapRequestEntity.setSoapAction("http://www.oorsprong.org/websamples.countryinfo/CountryCurrency");
        
        // Add variables for template processing
        soapRequestEntity.addVariable("countryCode", "GB");
        
        // Set payload with templates
        soapRequestEntity.setXmlBody(
            "<CountryCurrency xmlns=\"http://www.oorsprong.org/websamples.countryinfo\">" +
            "  <sCountryISOCode>{{ countryCode }}</sCountryISOCode>" +
            "</CountryCurrency>"
        );
        
        // Add XPath assertions
        soapRequestEntity.addAssertion("//*[local-name()='sISOCode']", "GBP");
        soapRequestEntity.addAssertion("//*[local-name()='sName']", "Pound");
        
        // Set expected status code
        soapRequestEntity.setExpectedStatus(200);
        
        // Execute the test with 1 thread and 1 iteration
        TestPlanStats stats = testEngine.executeSoapTest(soapRequestEntity, 1, 1);
        
        // Print statistics and detailed error information
        System.out.println("CountryCurrency templated SOAP test completed with: " + stats.overall().sampleTime().perc90().toMillis() + "ms");
        System.out.println("Error count: " + stats.overall().errorsCount());
        
        if (stats.overall().errorsCount() > 0) {
            System.out.println("Detailed errors: " + stats.overall().errors());
            double errorPercent = 100.0 * stats.overall().errorsCount() / stats.overall().samplesCount();
            System.out.println("Error percentage: " + errorPercent + "%");
            System.out.println("Sample count: " + stats.overall().samplesCount());
        }
        
        // Verify no errors occurred
        assertThat(stats.overall().errorsCount())
            .withFailMessage("Test had %d errors", stats.overall().errorsCount())
            .isEqualTo(0);
    }
    
    /**
     * Test to get a country's capital city
     */
    @Test
    public void testCapitalCity() throws Exception {
        TestEngine testEngine = new TestEngine();
        
        // Create SOAP Request Entity with payload
        SoapRequestEntity soapRequestEntity = new SoapRequestEntity(
            "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso"
        );
        
        // Set XML body
        soapRequestEntity.setXmlBody(
            "<CapitalCity xmlns=\"http://www.oorsprong.org/websamples.countryinfo\">" +
            "  <sCountryISOCode>FR</sCountryISOCode>" +
            "</CapitalCity>"
        );
        
        // Set SOAP action
        soapRequestEntity.setSoapAction("http://www.oorsprong.org/websamples.countryinfo/CapitalCity");
        
        // Add XPath assertions
        soapRequestEntity.addAssertion("//*[local-name()='CapitalCityResult']", "Paris");
        
        // Set expected status code
        soapRequestEntity.setExpectedStatus(200);
        
        // Execute the test with 1 thread and 1 iteration
        TestPlanStats stats = testEngine.executeSoapTest(soapRequestEntity, 1, 1);
        
        // Print statistics and detailed error information
        System.out.println("CapitalCity SOAP test completed with: " + stats.overall().sampleTime().perc90().toMillis() + "ms");
        System.out.println("Error count: " + stats.overall().errorsCount());
        
        if (stats.overall().errorsCount() > 0) {
            System.out.println("Detailed errors: " + stats.overall().errors());
            double errorPercent = 100.0 * stats.overall().errorsCount() / stats.overall().samplesCount();
            System.out.println("Error percentage: " + errorPercent + "%");
            System.out.println("Sample count: " + stats.overall().samplesCount());
        }
        
        // Verify no errors occurred
        assertThat(stats.overall().errorsCount())
            .withFailMessage("Test had %d errors", stats.overall().errorsCount())
            .isEqualTo(0);
    }
}