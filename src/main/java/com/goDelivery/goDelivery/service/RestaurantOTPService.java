package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantOTPService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALID_DURATION_MINUTES = 5;
    
    private final RestaurantUsersRepository userRepository;
    private final EmailService emailService;
    
    @Transactional
    public String generateAndSaveOTP(RestaurantUsers user) {
        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(OTP_VALID_DURATION_MINUTES));
        userRepository.save(user);
        
        // Send OTP via email asynchronously (EmailService.sendOtpEmail is already @Async)
        try {
            log.info("Attempting to send OTP email to restaurant admin: {}", user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
            log.info("OTP email request submitted for restaurant admin: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to submit OTP email request for restaurant admin: {}", user.getEmail(), e);
            // Don't fail the operation if email fails
        }
        
        return otp;
    }
    
    @Transactional
    public boolean verifyOTP(String email, String otp) {
        RestaurantUsers user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Restaurant admin not found"));
                
        if (user.getOtp() == null || user.getOtpExpiryTime() == null) {
            log.warn("No OTP found for restaurant admin: {}", email);
            return false;
        }
        
        boolean isOtpValid = user.getOtp().equals(otp) && 
                           LocalDateTime.now().isBefore(user.getOtpExpiryTime());
        
        if (isOtpValid) {
            // Clear OTP after successful verification
            user.setOtp(null);
            user.setOtpExpiryTime(null);
            
            // Mark user as verified and setup complete
            user.setEmailVerified(true);
            user.setSetupComplete(true);
            user.setActive(true);
            
            userRepository.save(user);
            log.info("Email verified successfully for restaurant admin: {}", user.getEmail());
        } else {
            log.warn("Invalid or expired OTP for restaurant admin: {}", email);
        }
        
        return isOtpValid;
    }
    
    @Transactional
    public void resendOTP(String email) {
        RestaurantUsers user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Restaurant admin not found"));
        
        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email already verified");
        }
        
        generateAndSaveOTP(user);
        log.info("OTP resent to restaurant admin: {}", email);
    }
    
    public boolean isEmailVerified(String email) {
        return userRepository.findByEmail(email)
                .map(RestaurantUsers::isEmailVerified)
                .orElse(false);
    }
    
    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
}
