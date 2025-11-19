package com.goDelivery.goDelivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for MPESA API client and settings
 */
@Data
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

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        // Configure exchange strategies with increased buffer size for large responses
        final int size = 16 * 1024 * 1024;
        
        return webClientBuilder
                .baseUrl(apiBaseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                    .build())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Cache-Control", "no-cache")
                .build();
    }
}
