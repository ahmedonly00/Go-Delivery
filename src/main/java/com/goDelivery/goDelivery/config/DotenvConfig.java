package com.goDelivery.goDelivery.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;


public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Load .env file from project root
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> envMap = new HashMap<>();

            // Add all .env variables to Spring environment
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
                // Also set as system property for backward compatibility
                System.setProperty(entry.getKey(), entry.getValue());
            });

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("dotenvProperties", envMap));

            System.out.println("✓ Environment variables loaded from .env file");

        } catch (Exception e) {
            System.err.println("⚠ Warning: Could not load .env file: " + e.getMessage());
            System.err.println("  Application will use default values from application.properties");
        }
    }
}
