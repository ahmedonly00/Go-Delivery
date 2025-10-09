package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    
    /**
     * Generate a random OTP, save it to the customer's account, and send it via email
     * @param customer The customer to generate OTP for
     * @return The generated OTP
     */
    @Transactional
    public String generateAndSaveOTP(Customer customer) {
        String otp = generateOTP();
        customer.setOtp(otp);
        customer.setOtpExpiryTime(LocalDateTime.now().plusMinutes(OTP_VALID_DURATION_MINUTES));
        customerRepository.save(customer);
        
        // Send OTP via email asynchronously
        sendOtpEmail(customer.getEmail(), customer.getFullNames(), otp);
        
        return otp;
    }
    
    /**
     * Send OTP email asynchronously
     */
    @Async
    protected void sendOtpEmail(String email, String name, String otp) {
        try {
            log.info("Sending OTP email to: {}", email);
            emailService.sendOtpEmail(email, name, otp);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", email, e);
            // We don't throw the exception here to not break the main flow
            // The user can still verify with the OTP if they receive it through other means
        }
    }
    
    /**
     * Verify the OTP for a customer
     * @param email The customer's email
     * @param otp The OTP to verify
     * @return true if OTP is valid, false otherwise
     */
    public boolean verifyOTP(String email, String otp) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
                
        if (customer.getOtp() == null || customer.getOtpExpiryTime() == null) {
            return false;
        }
        
        boolean isOtpValid = customer.getOtp().equals(otp) && 
                           LocalDateTime.now().isBefore(customer.getOtpExpiryTime());
        
        if (isOtpValid) {
            customer.setOtp(null);
            customer.setOtpExpiryTime(null);
            customer.setIsVerified(true);
            customerRepository.save(customer);
        }
        
        return isOtpValid;
    }
    
    /**
     * Check if a customer's email is verified
     * @param email The customer's email
     * @return true if verified, false otherwise
     */
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
