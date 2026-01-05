package com.goDelivery.goDelivery.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final String MOMO_BASE_URL = "https://momo.ivas.rw";
    private final String MOMO_AUTH_URL = "https://momo.ivas.rw/api/v1/auth/login";
    
    @GetMapping("/momo")
    public ResponseEntity<Map<String, Object>> checkMomoHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Test DNS Resolution
        try {
            InetAddress address = InetAddress.getByName("momo.ivas.rw");
            health.put("dns", "OK");
            health.put("ip_address", address.getHostAddress());
        } catch (Exception e) {
            health.put("dns", "FAILED: " + e.getMessage());
        }
        
        // Test Basic Connectivity
        try {
            URI uri = URI.create(MOMO_BASE_URL);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            
            int responseCode = connection.getResponseCode();
            health.put("basic_connectivity", "HTTP " + responseCode);
            
            if (responseCode == 200) {
                health.put("status", "UP");
            } else {
                health.put("status", "DEGRADED");
            }
        } catch (Exception e) {
            health.put("basic_connectivity", "FAILED: " + e.getMessage());
            health.put("status", "DOWN");
        }
        
        // Test API Endpoint
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(MOMO_BASE_URL, String.class);
            health.put("api_endpoint", "OK - " + response.getStatusCode());
        } catch (Exception e) {
            health.put("api_endpoint", "FAILED: " + e.getMessage());
        }
        
        // Test Auth Endpoint
        try {
            String testPayload = "{\"username\":\"test\",\"password\":\"test\"}";
            ResponseEntity<String> response = restTemplate.postForEntity(MOMO_AUTH_URL, testPayload, String.class);
            health.put("auth_endpoint", "OK - " + response.getStatusCode());
        } catch (Exception e) {
            health.put("auth_endpoint", "FAILED: " + e.getMessage());
        }
        
        // Add timestamp
        health.put("checked_at", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
