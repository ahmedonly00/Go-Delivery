package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.configSecurity.JwtService;
import com.goDelivery.goDelivery.dtos.auth.OTPVerificationRequest;
import com.goDelivery.goDelivery.dtos.auth.OTPVerificationResponse;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.service.OTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OTPController {

    private final OTPService otpService;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/verify-otp")
    public ResponseEntity<OTPVerificationResponse> verifyOTP(@RequestBody OTPVerificationRequest request) {
        try {
            boolean isVerified = otpService.verifyOTP(request.getEmail(), request.getOtp());
            
            if (isVerified) {
                // Get customer details
                Customer customer = customerRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                
                // Generate JWT token for auto-login
                String jwtToken = jwtService.generateToken(customer);
                
                // Build redirect URL to customer dashboard
                String dashboardUrl = frontendUrl + "/customer/dashboard";
                
                log.info("OTP verified successfully for customer: {}", customer.getEmail());
                
                return ResponseEntity.ok(OTPVerificationResponse.successWithRedirect(
                    "Email verified successfully! Redirecting to your dashboard...",
                    jwtToken,
                    dashboardUrl,
                    customer.getCustomerId(),
                    customer.getFullNames(),
                    customer.getEmail()
                ));
            } else {
                return ResponseEntity.badRequest().body(
                    OTPVerificationResponse.error("Invalid or expired OTP")
                );
            }
        } catch (Exception e) {
            log.error("OTP verification failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                OTPVerificationResponse.error(e.getMessage())
            );
        }
    }

    @PostMapping("/resend-otp/{email}")
    public ResponseEntity<OTPVerificationResponse> resendOTP(@PathVariable String email) {
        try {
            log.info("Resending OTP to email: {}", email);
            
            Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
            
            // Generate and save new OTP (this will also send the email)
            otpService.generateAndSaveOTP(customer);
            
            return ResponseEntity.ok(OTPVerificationResponse.success(
                "A new OTP has been sent to your email address. Please check your inbox.",
                null
            ));
        } catch (Exception e) {
            log.error("Failed to resend OTP to email: {}", email, e);
            return ResponseEntity.badRequest().body(
                OTPVerificationResponse.error("Failed to resend OTP: " + e.getMessage())
            );
        }
    }
}
