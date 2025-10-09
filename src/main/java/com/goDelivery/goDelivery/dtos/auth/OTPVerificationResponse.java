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
    
    public static OTPVerificationResponse success(String message, String token) {
        return OTPVerificationResponse.builder()
                .success(true)
                .message(message)
                .token(token)
                .build();
    }
    
    public static OTPVerificationResponse error(String message) {
        return OTPVerificationResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
