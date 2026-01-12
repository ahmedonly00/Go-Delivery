import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SimpleMomoApiTest {
    
    public static void main(String[] args) {
        // Your MoMo configuration from .env
        String baseUrl = "https://momo.ivas.rw";
        String username = "mozfoodapp";
        String password = "aF90iEp7sRfYJNC9";
        
        System.out.println("=== MoMo API Connectivity Test ===\n");
        System.out.println("Base URL: " + baseUrl);
        System.out.println("Username: " + username);
        System.out.println();
        
        // Test 1: Basic connectivity
        System.out.println("1. Testing basic connectivity...");
        testBasicConnection(baseUrl);
        
        // Test 2: Test authentication endpoint
        System.out.println("\n2. Testing authentication endpoint...");
        testAuthEndpoint(baseUrl, username, password);
        
        // Test 3: DNS resolution
        System.out.println("\n3. Testing DNS resolution...");
        testDnsResolution();
        
        System.out.println("\n=== Summary ===");
        System.out.println("If tests 1 and 2 failed with connection timeouts,");
        System.out.println("the MoMo API at momo.ivas.rw is definitely DOWN.");
    }
    
    private static void testBasicConnection(String baseUrl) {
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            System.out.println("   ✓ Basic connectivity: HTTP " + responseCode);
            
        } catch (Exception e) {
            System.out.println("   ✗ Basic connectivity failed: " + e.getMessage());
            System.out.println("   This suggests the MoMo API is down or unreachable!");
        }
    }
    
    private static void testAuthEndpoint(String baseUrl, String username, String password) {
        try {
            String authUrl = baseUrl + "/api/v1/auth/login";
            URL url = new URL(authUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set up the request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            
            // Create the JSON request body
            String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", 
                                          username, password);
            
            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Get the response
            int responseCode = connection.getResponseCode();
            System.out.println("   ✓ Auth endpoint responded: HTTP " + responseCode);
            
            // Read response body
            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            String responseBody = response.toString();
            if (!responseBody.isEmpty() && responseBody.length() < 500) {
                System.out.println("   Response: " + responseBody);
            }
            
            if (responseCode == 200 && responseBody.contains("token")) {
                System.out.println("   ✓ Authentication successful!");
            }
            
        } catch (ConnectException e) {
            System.out.println("   ✗ Connection refused: " + e.getMessage());
            System.out.println("   → This confirms the MoMo API is down!");
        } catch (SocketTimeoutException e) {
            System.out.println("   ✗ Connection timeout: " + e.getMessage());
            System.out.println("   → This confirms the MoMo API is not responding!");
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
        }
    }
    
    private static void testDnsResolution() {
        try {
            InetAddress address = InetAddress.getByName("momo.ivas.rw");
            System.out.println("   ✓ DNS resolved to: " + address.getHostAddress());
        } catch (Exception e) {
            System.out.println("   ✗ DNS failed: " + e.getMessage());
        }
    }
}
