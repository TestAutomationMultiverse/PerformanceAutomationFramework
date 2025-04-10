package io.perftest.protocol;

import io.perftest.model.Response;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * HTTPS Protocol implementation
 */
public class HttpsProtocol implements Protocol {
    
    @Override
    public String getName() {
        return "https";
    }
    
    @Override
    public Response execute(String endpoint, String method, String body, 
                         Map<String, String> headers, Map<String, String> params) throws Exception {
        
        Response response = new Response();
        
        try {
            // Build URL with parameters
            StringBuilder urlBuilder = new StringBuilder();
            if (!endpoint.toLowerCase().startsWith("https")) {
                urlBuilder.append("https://");
            }
            urlBuilder.append(endpoint);
            
            if (params != null && !params.isEmpty()) {
                urlBuilder.append(endpoint.contains("?") ? "&" : "?");
                boolean first = true;
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (!first) {
                        urlBuilder.append("&");
                    }
                    urlBuilder.append(param.getKey()).append("=").append(param.getValue());
                    first = false;
                }
            }
            
            URL url = new URL(urlBuilder.toString());
            
            // Configure SSL context for HTTPS
            SSLContext sslContext = SSLContext.getInstance("TLS");
            
            // Trust all certificates - Note: This should be used only for testing
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            
            // Set headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            
            // Set content type if not specified and body is present
            if (body != null && connection.getRequestProperty("Content-Type") == null) {
                connection.setRequestProperty("Content-Type", "application/json");
            }
            
            // Set body for POST, PUT, etc.
            if (body != null && !method.equals("GET") && !method.equals("DELETE")) {
                connection.setDoOutput(true);
                connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                connection.getOutputStream().flush();
                connection.getOutputStream().close();
            }
            
            // Get response
            int statusCode = connection.getResponseCode();
            response.setStatusCode(statusCode);
            
            // Read response body
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            
            try {
                while ((length = connection.getInputStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                response.setBody(result.toString(StandardCharsets.UTF_8.name()));
            } catch (Exception e) {
                // Handle error stream if available
                if (connection.getErrorStream() != null) {
                    result = new ByteArrayOutputStream();
                    while ((length = connection.getErrorStream().read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    response.setBody(result.toString(StandardCharsets.UTF_8.name()));
                } else {
                    response.setBody("Error: " + e.getMessage());
                }
            }
            
            // Get response headers
            for (String key : connection.getHeaderFields().keySet()) {
                if (key != null) {
                    response.addHeader(key, connection.getHeaderField(key));
                }
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            throw new Exception("HTTPS Protocol Error: " + e.getMessage(), e);
        }
        
        return response;
    }
}
