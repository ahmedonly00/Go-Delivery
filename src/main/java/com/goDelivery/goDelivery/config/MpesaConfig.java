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

/**
 * Configuration for MPESA API client and settings
 */
@Slf4j
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

    @PostConstruct
    public void init() {
        log.info("MPESA API Base URL: {}", apiBaseUrl);
        if (apiKey != null) {
            log.info("MPESA API Key is configured (last 4 chars): {}", 
                    apiKey.length() > 4 ? "****" + apiKey.substring(apiKey.length() - 4) : "****");
        } else {
            log.error("MPESA API Key is not configured!");
        }
    }

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
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
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
