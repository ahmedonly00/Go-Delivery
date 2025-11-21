package com.goDelivery.goDelivery.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.Getter;


@Slf4j
@Data
@Getter
@Configuration
@ConfigurationProperties(prefix = "mpesa")
public class MpesaConfig {
    private String apiKey;
    private String apiBaseUrl;
    private String webhookSecret;
    private int maxRetries = 3;
    private long retryDelay = 1000;
    private int connectionTimeout = 5000;
    private int readTimeout = 30000;
    private String paymentEndpoint;
    private String statusEndpoint;
    
    // Webhook configuration
    private boolean webhookSignatureRequired = true;
    private String webhookSignatureAlgorithm = "HmacSHA256";
    private int webhookSignatureExpiryMinutes = 5;

    @PostConstruct
    public void init() {
        log.info("MPESA API Base URL: {}", apiBaseUrl);
        log.info("MPESA Payment Endpoint: {}", paymentEndpoint);
        log.info("MPESA Status Endpoint: {}", statusEndpoint);
        if (apiKey != null) {
            log.info("MPESA API Key is configured (last 4 chars): {}", 
                    apiKey.length() > 4 ? "****" + apiKey.substring(apiKey.length() - 4) : "****");
            log.debug("Full API Key: {}", apiKey);  // Debug log of the full key
            
            if (apiKey.trim().isEmpty()) {
                log.error("MPESA API Key is empty or contains only whitespace!");
            }
        } else {
            log.error("MPESA API Key is not configured!");
        }
        
        // Log environment variables for debugging
        log.debug("Environment MPESA_API_KEY: {}", System.getenv("MPESA_API_KEY"));
        log.debug("System property mpesa.api-key: {}", System.getProperty("mpesa.api-key"));
    }

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        // Configure exchange strategies with increased buffer size for large responses
        final int size = 16 * 1024 * 1024;
        
        String maskedKey = apiKey != null ? 
            "****" + (apiKey.length() > 4 ? apiKey.substring(apiKey.length() - 4) : "") : 
            "[NULL]";
            
        log.info("Creating WebClient with API Key: {}", maskedKey);
        log.debug("Full API Key being used: {}", apiKey);
        
        return webClientBuilder
                .baseUrl(apiBaseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                    .build())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("Cache-Control", "no-cache")
                .filter((request, next) -> {
                    log.debug("Sending request to {} {}", request.method(), request.url());
                    request.headers().forEach((name, values) -> 
                        values.forEach(value -> {
                            if ("authorization".equalsIgnoreCase(name)) {
                                log.trace("{}: {}", name, "****" + (value.length() > 8 ? value.substring(value.length() - 8) : "****"));
                            } else {
                                log.trace("{}: {}", name, value);
                            }
                        })
                    );
                    return next.exchange(request);
                })
                .build();
    }
}
