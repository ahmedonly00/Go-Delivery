package com.goDelivery.goDelivery.modules.notification.service;

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
    com.goDelivery.goDelivery.modules.restaurant.model.Restaurant getRestaurantByEmail(String email);
}
