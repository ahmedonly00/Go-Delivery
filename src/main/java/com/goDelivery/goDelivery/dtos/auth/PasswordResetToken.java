package com.goDelivery.goDelivery.dtos.auth;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    
    private static final int EXPIRATION = 24 * 60; // 24 hours in minutes
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private String userEmail;
    
    @Column(nullable = false)
    private Date expiryDate;
    
    public PasswordResetToken() {
        // Default constructor
    }
    
    public PasswordResetToken(String token, String userEmail) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiryDate = calculateExpiryDate();
    }
    
    private Date calculateExpiryDate() {
        long nowInMillis = System.currentTimeMillis();
        return new Date(nowInMillis + EXPIRATION * 60 * 1000); // Convert minutes to milliseconds
    }
    
    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }
}
