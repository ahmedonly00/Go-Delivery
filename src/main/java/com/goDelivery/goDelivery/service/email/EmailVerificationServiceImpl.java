package com.goDelivery.goDelivery.service.email;

import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.EmailVerificationToken;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.EmailVerificationTokenRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    
    private final RestaurantUsersRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository verificationTokenRepository;

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
            
            // Generate unique verification token
            String token = UUID.randomUUID().toString();
            
            // Delete any existing tokens for this user
            verificationTokenRepository.findByUserEmail(email)
                    .ifPresent(existingToken -> verificationTokenRepository.delete(existingToken));
            
            // Create and save new verification token
            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .token(token)
                    .userEmail(email)
                    .restaurantId(restaurantId)
                    .build();
            verificationTokenRepository.save(verificationToken);
            
            // Send verification email
            emailService.sendVerificationEmail(email, user.getFullName(), token);
            
            log.info("Verification email sent to: {} for restaurant: {}", email, restaurantName);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public boolean verifyRestaurantEmail(String token, String email) {
        try {
            EmailVerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));
            
            // Check if token is valid
            if (!verificationToken.isValid()) {
                log.warn("Verification token is expired or already used: {}", token);
                return false;
            }
            
            // Validate that the provided email matches the token's email
            if (!verificationToken.getUserEmail().equalsIgnoreCase(email)) {
                log.warn("Email mismatch for token verification. Expected: {}, Provided: {}", 
                        verificationToken.getUserEmail(), email);
                return false;
            }
            
            // Find and update user
            RestaurantUsers user = userRepository.findByEmail(verificationToken.getUserEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + verificationToken.getUserEmail()));
            
            // Mark user as verified and setup complete
            user.setEmailVerified(true);
            user.setSetupComplete(true);
            user.setActive(true);
            userRepository.save(user);
            
            // Mark token as used
            verificationToken.setUsed(true);
            verificationTokenRepository.save(verificationToken);
            
            log.info("Email verified successfully for user: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            log.error("Failed to verify email with token {}: {}", token, e.getMessage(), e);
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
