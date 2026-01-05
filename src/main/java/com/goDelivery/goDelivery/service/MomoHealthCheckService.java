package com.goDelivery.goDelivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MomoHealthCheckService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${momo.base-url:https://momo.ivas.rw}")
    private String momoBaseUrl;
    
    private boolean isMomoAvailable = true;
    private long lastCheckTime = 0;
    private final long CHECK_INTERVAL = 60000; // Check every minute
    
    public boolean isMomoServiceAvailable() {
        long currentTime = System.currentTimeMillis();
        
        // Check only if interval has passed
        if (currentTime - lastCheckTime > CHECK_INTERVAL) {
            checkMomoAvailability();
            lastCheckTime = currentTime;
        }
        
        return isMomoAvailable;
    }
    
    private void checkMomoAvailability() {
        try {
            // Quick HEAD request to check availability
            restTemplate.headForHeaders(momoBaseUrl);
            if (!isMomoAvailable) {
                log.info("MoMo service is back online!");
                isMomoAvailable = true;
            }
        } catch (Exception e) {
            if (isMomoAvailable) {
                log.error("MoMo service is unavailable: {}", e.getMessage());
                isMomoAvailable = false;
            }
        }
    }
    
    public void setMomoAvailable(boolean available) {
        this.isMomoAvailable = available;
    }
}
