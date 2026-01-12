package com.goDelivery.goDelivery;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.HashMap;

public class MomoApiChecker {
    
    public static void main(String[] args) {
        // Your MoMo configuration from .env
        String baseUrl = "https://momo.ivas.rw";
        String username = "mozfoodapp";
        String password = "aF90iEp7sRfYJNC9";
        
        RestTemplate restTemplate = new RestTemplate();
        
        System.out.println("=== MoMo API Connectivity Test ===\n");
        System.out.println("Base URL: " + baseUrl);
        System.out.println("Username: " + username);
        System.out.println();
        
        // Test 1: Basic connectivity
        System.out.println("1. Testing basic connectivity...");
        try {
            URI uri = URI.create(baseUrl);
            URL testUrl = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) testUrl.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            System.out.println("   ✓ Basic connectivity: HTTP " + responseCode);
            
        } catch (Exception e) {
            System.out.println("   ✗ Basic connectivity failed: " + e.getMessage());
            System.out.println("   This suggests the MoMo API is down or unreachable!");
        }
        
        // Test 2: Test authentication endpoint
        System.out.println("\n2. Testing authentication endpoint...");
        String authUrl = baseUrl + "/api/v1/auth/login";
        
        try {
            Map<String, String> authRequest = new HashMap<>();
            authRequest.put("username", username);
            authRequest.put("password", password);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(authRequest, headers);
            
            System.out.println("   Sending POST to: " + authUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, entity, Map.class);
            
            System.out.println("   ✓ Auth endpoint responded: " + response.getStatusCode());
            
            if (response.getBody() != null) {
                Object token = response.getBody().get("token");
                if (token != null) {
                    System.out.println("   ✓ Authentication successful! Token received.");
                } else {
                    System.out.println("   ⚠ No token in response. Response: " + response.getBody());
                }
            }
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            System.out.println("   ✗ Connection failed: " + e.getMessage());
            if (e.getCause() instanceof java.net.ConnectException) {
                System.out.println("   → This confirms the MoMo API is down!");
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error: " + e.getMessage());
        }
        
        // Test 3: DNS resolution
        System.out.println("\n3. Testing DNS resolution...");
        try {
            java.net.InetAddress address = java.net.InetAddress.getByName("momo.ivas.rw");
            System.out.println("   ✓ DNS resolved to: " + address.getHostAddress());
        } catch (Exception e) {
            System.out.println("   ✗ DNS failed: " + e.getMessage());
        }
        
        System.out.println("\n=== Summary ===");
        System.out.println("If tests 1 and 2 failed with connection timeouts,");
        System.out.println("the MoMo API at momo.ivas.rw is definitely DOWN.");
        System.out.println("\nNext steps:");
        System.out.println("1. Contact MoMo/IVAS support");
        System.out.println("2. Check if the API URL has changed");
        System.out.println("3. Use cash payment as fallback");
    }
}
