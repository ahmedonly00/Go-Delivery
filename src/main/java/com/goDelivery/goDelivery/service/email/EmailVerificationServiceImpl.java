package com.goDelivery.goDelivery.service.email;

import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link EmailVerificationService} for handling email verification
 * and notification related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    
    private final RestaurantUsersRepository userRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Async
    @Transactional
    public boolean notifyRestaurantSetupComplete(String email, String restaurantName) {
        try {
            RestaurantUsers user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            
            // Mark user as active and verified since we're not doing email verification anymore
            user.setActive(true);
            user.setEmailVerified(true);
            userRepository.save(user);
            
            // Send setup completion email
            emailService.sendSetupCompletionEmail(
                email,
                user.getFullName(),
                restaurantName
            );
            
            log.info("Restaurant setup completion email sent to: {}", email);
            return true;
        } catch (Exception e) {
            log.error("Failed to send setup completion email to {}: {}", email, e.getMessage(), e);
            return false;
        }
    }
}
