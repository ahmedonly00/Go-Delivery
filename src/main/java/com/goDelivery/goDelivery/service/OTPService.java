package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.repository.CustomerRepository;
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
public class OTPService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALID_DURATION_MINUTES = 5;
    
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    
    @Transactional
    public String generateAndSaveOTP(Customer customer) {
        String otp = generateOTP();
        customer.setOtp(otp);
        customer.setOtpExpiryTime(LocalDateTime.now().plusMinutes(OTP_VALID_DURATION_MINUTES));
        customerRepository.save(customer);
        
        // Send OTP via email asynchronously (EmailService.sendOtpEmail is already @Async)
        try {
            log.info("Attempting to send OTP email to: {}", customer.getEmail());
            emailService.sendOtpEmail(customer.getEmail(), customer.getFullNames(), otp);
            log.info("OTP email request submitted for: {}", customer.getEmail());
        } catch (Exception e) {
            log.error("Failed to submit OTP email request for: {}", customer.getEmail(), e);
        }
        
        return otp;
    }
    
    @Transactional
    public boolean verifyOTP(String email, String otp) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }
        
        log.info("Verifying OTP for email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Customer not found for email: {}", email);
                    return new RuntimeException("Customer not found");
                });
                
        if (customer.getOtp() == null || customer.getOtpExpiryTime() == null) {
            log.warn("No OTP found or OTP expired for email: {}", email);
            return false;
        }
        
        boolean isOtpValid = customer.getOtp().equals(otp) && 
                           LocalDateTime.now().isBefore(customer.getOtpExpiryTime());
        
        if (isOtpValid) {
            log.info("OTP is valid for email: {}. Updating verification status...", email);
            
            customer.setOtp(null);
            customer.setOtpExpiryTime(null);
            
            customer.setVerified(true);  
            customer.setEmailVerified(true);
            
            customer = customerRepository.saveAndFlush(customer);
            
            log.info("Successfully verified customer. Email: {}, Verified: {}, Email Verified: {}, Active: {}", 
                    email, customer.getIsVerified(), customer.isEmailVerified(), customer.getIsActive());
        } else {
            log.warn("Invalid OTP for email: {}", email);
        }
        
        return isOtpValid;
    }
    
    public boolean isEmailVerified(String email) {
        return customerRepository.findByEmail(email)
                .map(Customer::getIsVerified)
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
