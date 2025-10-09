package com.goDelivery.goDelivery.service.email;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

public interface EmailVerificationService {
    
    @Async
    @Transactional
    boolean notifyRestaurantSetupComplete(String email, String restaurantName);
}
