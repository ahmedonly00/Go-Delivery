package com.goDelivery.goDelivery.service.email;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

public interface EmailVerificationService {
    
    @Async
    @Transactional
    void notifyRestaurantSetupComplete(String email, String restaurantName);
    
    @Async
    @Transactional
    void sendVerificationEmail(String email, String restaurantName, Long restaurantId);
    
    @Transactional
    boolean verifyRestaurantEmail(String verificationToken, String email);
    
    @Transactional
    com.goDelivery.goDelivery.model.Restaurant getRestaurantByEmail(String email);
}
