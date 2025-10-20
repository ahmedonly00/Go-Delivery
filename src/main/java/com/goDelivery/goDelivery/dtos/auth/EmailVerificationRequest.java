package com.goDelivery.goDelivery.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}
