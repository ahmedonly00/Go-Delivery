package com.goDelivery.goDelivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.order")
public class OrderConfig {
   
    private int defaultPreparationTimeMinutes = 30;
}
