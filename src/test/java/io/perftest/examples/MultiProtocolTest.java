package io.perftest.examples;

import io.perftest.core.engine.TestEngine;
import io.perftest.entities.request.GraphQLRequestEntity;
import io.perftest.entities.request.HttpRequestEntity;
import io.perftest.entities.request.SoapRequestEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class that demonstrates testing multiple protocols in a single test class
 */
public class MultiProtocolTest {
    private static final Logger logger = LoggerFactory.getLogger(MultiProtocolTest.class);
    private static TestEngine testEngine;
    
    @BeforeAll
    public static void setup() {
        testEngine = new TestEngine();
    }
    
    @Test
    public void testMultiProtocol() throws IOException {
        logger.info("Starting testMultiProtocol test");
        
        // 1. HTTP API test
        logger.info("Testing HTTP API");
        HttpRequestEntity httpRequest = new HttpRequestEntity("https://test-api.k6.io/public/crocodiles/1/");
        httpRequest.setMethod("GET");
        httpRequest.setName("K6 API - Get Crocodile");
        httpRequest.addAssertion("$.name", "*");
        
        TestPlanStats httpStats = testEngine.executeHttpTest(httpRequest, 1, 1);
        logger.info("HTTP test completed with average time: {}ms", httpStats.overall().sampleTime().mean());
        
        // 2. GraphQL API test
        logger.info("Testing GraphQL API");
        GraphQLRequestEntity graphqlRequest = new GraphQLRequestEntity("https://countries.trevorblades.com");
        graphqlRequest.setName("Countries GraphQL API");
        
        String query = "query { continent(code: \"EU\") { name } }";
        graphqlRequest.setQuery(query);
        graphqlRequest.addAssertion("$.data.continent.name", "Europe");
        
        TestPlanStats graphqlStats = testEngine.executeGraphQLTest(graphqlRequest, 1, 1);
        logger.info("GraphQL test completed with average time: {}ms", graphqlStats.overall().sampleTime().mean());
        
        // 3. SOAP API test
        logger.info("Testing SOAP API");
        SoapRequestEntity soapRequest = new SoapRequestEntity("http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso");
        soapRequest.setName("Country Info SOAP Service");
        
        String soapBody = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:web=\"http://www.oorsprong.org/websamples.countryinfo\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:ListOfContinentsByName/>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        
        soapRequest.setBody(soapBody);
        soapRequest.setSoapAction("ListOfContinentsByName");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/xml;charset=UTF-8");
        soapRequest.setHeaders(headers);
        
        soapRequest.addAssertion("//m:ListOfContinentsByNameResult", "*");
        
        TestPlanStats soapStats = testEngine.executeSoapTest(soapRequest, 1, 1);
        logger.info("SOAP test completed with average time: {}ms", soapStats.overall().sampleTime().mean());
        
        // Summary
        logger.info("All tests completed successfully");
        logger.info("HTTP average time: {}ms", httpStats.overall().sampleTime().mean());
        logger.info("GraphQL average time: {}ms", graphqlStats.overall().sampleTime().mean());
        logger.info("SOAP average time: {}ms", soapStats.overall().sampleTime().mean());
    }
}