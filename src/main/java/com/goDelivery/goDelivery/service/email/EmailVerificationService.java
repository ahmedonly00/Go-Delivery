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

import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    
    private final RestaurantUsersRepository userRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.email.verification-token-expiration-hours:24}")
    private int verificationTokenExpirationHours;

    @Async
    @Transactional
    public boolean sendVerificationEmail(String email, String name) {
        try {
            RestaurantUsers user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

            String token = generateVerificationToken(email);
            user.setVerificationToken(token);
            user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(verificationTokenExpirationHours));
            userRepository.save(user);

            emailService.sendVerificationEmail(email, name, token);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public boolean verifyEmail(String token) {
        try {
            RestaurantUsers user = userRepository.findByVerificationToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

            if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Verification token expired for user: {}", user.getEmail());
                return false;
            }

            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            userRepository.save(user);

            log.info("Email verified successfully for user: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            log.error("Email verification failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Async
    @Transactional
    public boolean resendVerificationEmail(String email) {
        try {
            RestaurantUsers user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

            if (user.isEmailVerified()) {
                log.info("Email already verified for user: {}", email);
                return true;
            }

            String token = generateVerificationToken(email);
            user.setVerificationToken(token);
            user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(verificationTokenExpirationHours));
            userRepository.save(user);

            emailService.sendVerificationEmail(email, user.getFullName(), token);
            return true;
        } catch (Exception e) {
            log.error("Failed to resend verification email to {}: {}", email, e.getMessage(), e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        return userRepository.findByEmail(email)
                .map(RestaurantUsers::isEmailVerified)
                .orElse(false);
    }

    public String generateVerificationToken(String email) {
        // Generate a unique token using UUID and email hash
        return UUID.randomUUID().toString() + "_" + Integer.toHexString(email.hashCode());
    }
}
