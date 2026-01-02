package com.goDelivery.goDelivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.commission")
public class CommissionConfig {
    
    // Default commission rate (as decimal, e.g., 0.15 for 15%)
    private double defaultRate = 0.15;
    
    // Minimum commission amount
    private double minimumAmount = 0.0;
    
    // Maximum commission amount
    private double maximumAmount = Double.MAX_VALUE;
    
    // Tiered rates (optional)
    private TieredRate[] tieredRates;
    
    @Data
    public static class TieredRate {
        private double minAmount;
        private double maxAmount;
        private double rate;
    }
}
