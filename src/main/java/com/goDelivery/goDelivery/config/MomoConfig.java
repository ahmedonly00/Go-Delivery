package com.goDelivery.goDelivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "momo")
@Data
public class MomoConfig {
    private String baseUrl;
    private String username;
    private String password;
    private String subscriptionKey;
    private String environment; // sandbox or production
    private String callbackHost;
    
    public String getCollectionBaseUrl() {
        return baseUrl + "/collection/v1_0";
    }
    
    public String getDisbursementBaseUrl() {
        return baseUrl + "/disbursement/v1_0";
    }
    
    public String getAuthUrl() {
        return baseUrl + "/auth/token";
    }
}
