package com.goDelivery.goDelivery.config;

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
    
    private String username;
    
    private String password;  
    
    private String callbackHost;

    public String getAuthUrl() {
        return baseUrl + "/api/v1/auth/login";
    }
    
    public String getCollectionUrl() {
        return baseUrl + "/api/v1/collection/request-payment";
    }
    
    public String getCollectionStatusUrl(String referenceId) {
        return baseUrl + "/api/v1/collection/status/" + referenceId;
    }
    
    public String getDisbursementUrl() {
        return baseUrl + "/api/v1/disbursement/single";
    }

    public String getCollectionDisbursementUrl() {
        return baseUrl + "/api/v1/disbursement/collection-disbursement";
    }
    
    public String getDisbursementStatusUrl(String referenceId) {
        return baseUrl + "/api/v1/disbursement/status/" + referenceId;
    } 
    
    public String getTransactionsUrl() {
        return baseUrl + "/api/v1/transactions";
    }
    
    public String getTransactionSearchUrl() {
        return baseUrl + "/api/v1/transactions/search";
    }
    
    public String getTransactionStatusUrl(String referenceId) {
        return baseUrl + "/api/v1/transactions/status/" + referenceId;
    }

}
