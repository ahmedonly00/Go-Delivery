package com.goDelivery.goDelivery.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OTPVerificationResponse {
    private boolean success;
    private String message;
    private String token;
    private String redirectUrl;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    
    public static OTPVerificationResponse success(String message, String token) {
        return OTPVerificationResponse.builder()
                .success(true)
                .message(message)
                .token(token)
                .build();
    }
    
    public static OTPVerificationResponse successWithRedirect(String message, String token, 
            String redirectUrl, Long customerId, String customerName, String customerEmail) {
        return OTPVerificationResponse.builder()
                .success(true)
                .message(message)
                .token(token)
                .redirectUrl(redirectUrl)
                .customerId(customerId)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .build();
    }
    
    public static OTPVerificationResponse error(String message) {
        return OTPVerificationResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
