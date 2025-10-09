package com.goDelivery.goDelivery.auth;

import com.goDelivery.goDelivery.dtos.auth.*;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.repository.PasswordResetTokenRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import com.goDelivery.goDelivery.configSecurity.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final RestaurantUsersRepository restaurantUsersRepository;
    private final SuperAdminRepository superAdminRepository;
    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public LoginResponse authenticate(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }
        
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        try {
            // Check if user exists first
            CustomUserDetails user = findUserByEmail(email);

            // For customers, check if email is verified
            if (user instanceof Customer) {
                Customer customer = (Customer) user;
                if (!customer.getIsVerified()) {
                    throw new RuntimeException("Email not verified. Please verify your email first.");
                }
            }

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid email or password");
            }

            // Check if account is active
            if (!user.isAccountNonLocked() || !user.isEnabled()) {
                throw new RuntimeException("Account is locked or disabled");
            }

            try {
                // Generate token
                String jwtToken = jwtService.generateToken(user);
                // Extract role
                String role = user.getAuthorities().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No roles found for user"))
                    .getAuthority();

                // Return response with full details
                return new LoginResponse(
                    jwtToken,
                    "Bearer",
                    user.getId(),
                    user.getUsername(),
                    role,
                    user.getFullName()
                );

            } catch (Exception e) {
                throw new RuntimeException("Authentication processing failed", e);
            }

        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (BadCredentialsException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private CustomUserDetails findUserByEmail(String email) {
        if (email == null) {
            throw new UsernameNotFoundException("Email cannot be null");
        }
        
        try {
            // Check Restaurant Users (includes ADMIN, CASHIER, BIKER, RESTAURANT_ADMIN roles)
            Optional<RestaurantUsers> restaurantUser = restaurantUsersRepository.findByEmail(email);
            if (restaurantUser.isPresent()) {
                return restaurantUser.get();
            }

            // Check Super Admin
            Optional<SuperAdmin> superAdmin = superAdminRepository.findByEmail(email);
            if (superAdmin.isPresent()) {
                return superAdmin.get();
            }

            // Check Customer
            Optional<Customer> customer = customerRepository.findByEmail(email);
            if (customer.isPresent()) {
                return customer.get();
            }

            // If no user found with the given email
            throw new UsernameNotFoundException("User not found with email: " + email);
            
        } catch (Exception e) {
            throw new UsernameNotFoundException("Error finding user with email: " + email, e);
        }
    }

    public void logout(@RequestParam String token) {

    }

    public void refreshToken(@RequestParam String token) {
        // Implementation for token refresh
    }

    public void resetPassword(ResetPasswordRequest request) {
        // Validate token
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new RuntimeException("Invalid or expired token"));
            
        // Check if token is expired
        if (token.getExpiryDate().before(new Date())) {
            passwordResetTokenRepository.delete(token);
            throw new RuntimeException("Token has expired");
        }
        
        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        try {
            // Find user and update password
            CustomUserDetails userDetails = findUserByEmail(token.getUserEmail());
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            
            // Update and save the user based on their type
            if (userDetails instanceof Customer) {
                Customer customer = (Customer) userDetails;
                customer.setPassword(encodedPassword);
                customerRepository.save(customer);
            } else if (userDetails instanceof RestaurantUsers) {
                RestaurantUsers restaurantUser = (RestaurantUsers) userDetails;
                restaurantUser.setPassword(encodedPassword);
                restaurantUsersRepository.save(restaurantUser);
            } else if (userDetails instanceof SuperAdmin) {
                SuperAdmin superAdmin = (SuperAdmin) userDetails;
                superAdmin.setPassword(encodedPassword);
                superAdminRepository.save(superAdmin);
            }
            
            // Delete the used token
            passwordResetTokenRepository.delete(token);
            
        } catch (Exception e) {
            throw new RuntimeException("Error resetting password: " + e.getMessage());
        }
    }

}