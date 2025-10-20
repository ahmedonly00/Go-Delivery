package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {
    
    private static final int EXPIRATION_HOURS = 24;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private String userEmail;
    
    @Column(nullable = false)
    private Long restaurantId;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiryDate == null) {
            expiryDate = LocalDateTime.now().plusHours(EXPIRATION_HOURS);
        }
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }
}
