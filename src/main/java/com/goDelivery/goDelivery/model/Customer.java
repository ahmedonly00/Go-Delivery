package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.Roles;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customer")
public class Customer implements CustomUserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "full_names", nullable = false)
    private String fullNames;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password",nullable = false)
    private String password;

    @Column(name = "confirm_password", nullable = false)
    private String confirmPassword;

    @Column(name = "roles", nullable = false)
    @Enumerated(EnumType.STRING)
    private Roles roles;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_verified")
    private Boolean isVerified = true;
    
    private String otp;
    
    @Column(name = "otp_expiry_time")
    private LocalDateTime otpExpiryTime;

    @Column(name = "email_verified")
    private boolean emailVerified = true;

    @Column(name = "phone_verified", nullable = true)
    private boolean phoneVerified = true;

    @Column(name = "last_login", nullable = true)
    private LocalDate lastLogin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerAddress> customerAddresses;

    @PrePersist
    protected void onCreate() {
        LocalDate now = LocalDate.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roles.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public Long getId() {
        return this.customerId;
    }

    @Override
    public String getFullName() {
        return this.fullNames;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive != null && isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
        if (isVerified) {
            this.isActive = true;
        }
    }
    
    public String getOtp() {
        return otp;
    }
    
    public void setOtp(String otp) {
        this.otp = otp;
    }
    
    public LocalDateTime getOtpExpiryTime() {
        return otpExpiryTime;
    }
    
    public void setOtpExpiryTime(LocalDateTime otpExpiryTime) {
        this.otpExpiryTime = otpExpiryTime;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
