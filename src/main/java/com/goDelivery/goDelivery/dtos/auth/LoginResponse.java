package com.goDelivery.goDelivery.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String role;
    private String fullName;
    private Long restaurantId;
    private String restaurantName;
    
    // Constructor without restaurant details (for backward compatibility)
    public LoginResponse(String token, String tokenType, Long id, String email, String role, String fullName) {
        this.token = token;
        this.tokenType = tokenType;
        this.id = id;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
    }
}
