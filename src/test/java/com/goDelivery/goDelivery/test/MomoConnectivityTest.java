package com.goDelivery.goDelivery.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Slf4j
public class MomoConnectivityTest {
    
    public static void main(String[] args) {
        String momoUrl = "https://momo.ivas.rw";
        String authUrl = "https://momo.ivas.rw/api/v1/auth/login";
        
        // Test 1: DNS Resolution
        try {
            InetAddress address = InetAddress.getByName("momo.ivas.rw");
            log.info("DNS Resolution successful: {}", address.getHostAddress());
        } catch (Exception e) {
            log.error("DNS Resolution failed: {}", e.getMessage());
        }
        
        // Test 2: Basic Connectivity
        try {
            URI uri = URI.create(momoUrl);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            
            int responseCode = connection.getResponseCode();
            log.info("HTTP Response Code: {}", responseCode);
            
            if (responseCode == 200) {
                log.info("Basic connectivity successful!");
            }
        } catch (Exception e) {
            log.error("Basic connectivity failed: {}", e.getMessage());
        }
        
        // Test 3: API Endpoint Test
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(momoUrl, String.class);
            log.info("API Response Status: {}", response.getStatusCode());
            log.info("API Response Body: {}", response.getBody());
        } catch (Exception e) {
            log.error("API test failed: {}", e.getMessage());
        }
        
        // Test 4: Auth Endpoint Test
        try {
            RestTemplate restTemplate = new RestTemplate();
            String testPayload = "{\"username\":\"test\",\"password\":\"test\"}";
            ResponseEntity<String> response = restTemplate.postForEntity(authUrl, testPayload, String.class);
            log.info("Auth Endpoint Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Auth endpoint test failed: {}", e.getMessage());
        }
    }
}
