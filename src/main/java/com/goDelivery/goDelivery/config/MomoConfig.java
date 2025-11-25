package com.goDelivery.goDelivery.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.extern.slf4j.Slf4j;
import lombok.Data;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "momo")
@Data
@Validated
public class MomoConfig {
    @NotBlank(message = "MoMo base URL is required")
    private String baseUrl;
    
    private String username = "";
    private String password = "";
    
    // Subscription key is optional when using username/password authentication
    private String subscriptionKey;
    
    @NotBlank(message = "MoMo environment is required")
    private String environment; // sandbox or production
    
    @NotBlank(message = "Callback host is required")
    private String callbackHost;
    
    @PostConstruct
    public void validateConfig() {
        log.info("Initializing MoMo Configuration:");
        log.info("Base URL: {}", baseUrl);
        log.info("Environment: {}", environment);
        log.info("Callback Host: {}", callbackHost);
        
        // Only validate required fields if username is provided
        if (username != null && !username.isBlank()) {
            log.info("Using username/password authentication");
            if (password == null || password.isBlank()) {
                log.warn("Username is provided but password is empty");
            }
        } else {
            log.warn("No MoMo username provided. MoMo payment features will be disabled.");
        }
        
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
    }
    
    public String getCollectionBaseUrl() {
        return baseUrl.endsWith("/") ? baseUrl + "collection/" : baseUrl + "/collection/";
    }
    
    public String getDisbursementBaseUrl() {
        return baseUrl.endsWith("/") ? baseUrl + "disbursement/" : baseUrl + "/disbursement/";
    }
    
    public String getAuthUrl() {
        // The auth endpoint is typically at /collection/token/ for both sandbox and production
        return (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + "collection/token/";
    }

    // private void validateBaseUrl() {
    //     if (baseUrl == null || baseUrl.isBlank()) {
    //         throw new IllegalStateException("MoMo base URL is not configured");
    //     }
    // }
}
