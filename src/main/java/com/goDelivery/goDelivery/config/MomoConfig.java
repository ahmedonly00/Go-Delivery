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
    
    @NotBlank(message = "MoMo username is required")
    private String username;
    
    @NotBlank(message = "MoMo password is required")
    private String password;
    
    @NotBlank(message = "MoMo subscription key is required")
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
        
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
    }
    
    public String getCollectionBaseUrl() {
        return baseUrl + "collection/";
    }
    
    public String getDisbursementBaseUrl() {
        return baseUrl + "disbursement/";
    }
    
    public String getAuthUrl() {
        // The auth endpoint is typically at /collection/token/ for both sandbox and production
        return baseUrl + "collection/";
    }

    // private void validateBaseUrl() {
    //     if (baseUrl == null || baseUrl.isBlank()) {
    //         throw new IllegalStateException("MoMo base URL is not configured");
    //     }
    // }
}
