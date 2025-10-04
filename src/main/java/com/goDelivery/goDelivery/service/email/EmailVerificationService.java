package com.goDelivery.goDelivery.service.email;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service interface for email verification related operations
 */
public interface EmailVerificationService {
    
    /**
     * Notifies the restaurant admin that their setup is complete and they can now log in
     * @param email The email of the restaurant admin
     * @param restaurantName The name of the restaurant
     * @return true if the notification was sent successfully, false otherwise
     */
    @Async
    @Transactional
    boolean notifyRestaurantSetupComplete(String email, String restaurantName);
}
