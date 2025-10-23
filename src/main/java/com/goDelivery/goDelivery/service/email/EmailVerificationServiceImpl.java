package com.goDelivery.goDelivery.service.email;

import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.service.RestaurantOTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    
    private final RestaurantUsersRepository userRepository;
    private final EmailService emailService;
    private final RestaurantOTPService restaurantOTPService;

    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Async
    @Transactional
    public void notifyRestaurantSetupComplete(String email, String restaurantName) {
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
        } catch (Exception e) {
            log.error("Failed to send setup completion email to {}: {}", email, e.getMessage(), e);
        }
    }
    
    @Override
    @Async
    @Transactional
    public void sendVerificationEmail(String email, String restaurantName, Long restaurantId) {
        try {
            RestaurantUsers user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            
            // Generate and send OTP instead of token
            restaurantOTPService.generateAndSaveOTP(user);
            
            log.info("OTP verification email sent to: {} for restaurant: {}", email, restaurantName);
        } catch (Exception e) {
            log.error("Failed to send OTP verification email to {}: {}", email, e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public boolean verifyRestaurantEmail(String otp, String email) {
        try {
            // Use OTP service to verify
            boolean verified = restaurantOTPService.verifyOTP(email, otp);
            
            if (verified) {
                log.info("Email verified successfully for restaurant admin: {}", email);
            } else {
                log.warn("Invalid or expired OTP for restaurant admin: {}", email);
            }
            
            return verified;
        } catch (Exception e) {
            log.error("Failed to verify email with OTP for {}: {}", email, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public com.goDelivery.goDelivery.model.Restaurant getRestaurantByEmail(String email) {
        RestaurantUsers user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getRestaurant();
    }
}
