package com.goDelivery.goDelivery.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Configuration class to set the default timezone for the application.
 * Sets timezone to Central Africa Time (CAT/UTC+2) used by Rwanda and
 * Mozambique.
 */
@Slf4j
@Configuration
public class TimezoneConfig {

    @PostConstruct
    public void init() {
        // Set default timezone to Central Africa Time (Rwanda)
        // Africa/Kigali (Rwanda) and Africa/Maputo (Mozambique) are both UTC+2
        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Kigali"));

        log.info("Application timezone set to: {}", TimeZone.getDefault().getID());
        log.info("Current time in CAT: {}", java.time.ZonedDateTime.now());
    }
}
