package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.Roles;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "restaurant_users")
public class RestaurantUsers implements CustomUserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(name = "permissions", nullable = false)
    private String permissions;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "is_active")
    private boolean isActive;

    @Builder.Default
    @Column(name = "setup_complete")
    private boolean setupComplete = false;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "last_login")
    private LocalDate lastLogin;

    @Column(name = "otp")
    private String otp;

    @Column(name = "otp_expiry_time")
    private LocalDateTime otpExpiryTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurant restaurant;

    @OneToMany(mappedBy = "restaurantUser", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ReviewResponse> reviewResponses;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role-based authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        // Add permission-based authorities if permissions exist
        if (permissions != null && !permissions.trim().isEmpty()) {
            // Split permissions by comma or semicolon
            String[] permissionArray = permissions.split("[,;]");
            for (String permission : permissionArray) {
                String trimmedPermission = permission.trim();
                if (!trimmedPermission.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(trimmedPermission));
                }
            }
        }

        return authorities;
    }

    public String getPassword() {

        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
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
        return true;
    }

    @Override
    public Long getId() {
        return this.userId;
    }

    @Override
    public String getFullName() {
        return this.fullName;
    }

}
