package com.goDelivery.goDelivery.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.HashMap;

@Slf4j
public class MomoApiTest {
    
    public static void main(String[] args) {
        // Test different URLs to verify connectivity
        String[] testUrls = {
            "https://momo.ivas.rw",
            "https://momo.ivas.rw/api",
            "https://momo.ivas.rw/api/v1",
            "https://momo.ivas.rw/api/v1/auth/login"
        };
        
        RestTemplate restTemplate = new RestTemplate();
        
        System.out.println("=== MoMo API Connectivity Test ===\n");
        
        for (String url : testUrls) {
            System.out.println("Testing: " + url);
            
            // Test 1: Basic connectivity with HEAD request
            try {
                URI uri = URI.create(url);
                URL testUrl = uri.toURL();
                HttpURLConnection connection = (HttpURLConnection) testUrl.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                System.out.println("  HEAD Request: HTTP " + responseCode);
                
            } catch (Exception e) {
                System.out.println("  HEAD Request: FAILED - " + e.getMessage());
            }
            
            // Test 2: GET request
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                System.out.println("  GET Request: " + response.getStatusCode());
                if (response.getBody() != null && response.getBody().length() < 200) {
                    System.out.println("  Response: " + response.getBody());
                }
            } catch (Exception e) {
                System.out.println("  GET Request: FAILED - " + e.getMessage());
            }
            
            // Test 3: POST to auth endpoint (if it's the auth URL)
            if (url.endsWith("/auth/login")) {
                try {
                    Map<String, String> authRequest = new HashMap<>();
                    authRequest.put("username", "test");
                    authRequest.put("password", "test");
                    
                    ResponseEntity<Map> response = restTemplate.postForEntity(url, authRequest, Map.class);
                    System.out.println("  POST Auth: " + response.getStatusCode());
                } catch (Exception e) {
                    System.out.println("  POST Auth: FAILED - " + e.getMessage());
                    if (e.getCause() != null) {
                        System.out.println("  Caused by: " + e.getCause().getMessage());
                    }
                }
            }
            
            System.out.println();
        }
        
        // Test DNS resolution
        try {
            java.net.InetAddress address = java.net.InetAddress.getByName("momo.ivas.rw");
            System.out.println("DNS Resolution: SUCCESS - " + address.getHostAddress());
        } catch (Exception e) {
            System.out.println("DNS Resolution: FAILED - " + e.getMessage());
        }
        
        System.out.println("\n=== Test Complete ===");
        System.out.println("\nIf all tests failed with connection timeouts, the MoMo API is likely down.");
        System.out.println("If DNS fails, check your network settings.");
        System.out.println("If you get 404 errors, the API endpoints might have changed.");
    }
}
