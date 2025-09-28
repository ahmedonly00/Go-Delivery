package com.goDelivery.goDelivery.auth;

import com.goDelivery.goDelivery.configSecurity.JwtService;
import com.goDelivery.goDelivery.dtos.auth.LoginRequest;
import com.goDelivery.goDelivery.dtos.auth.LoginResponse;
import com.goDelivery.goDelivery.model.CustomUserDetails;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.model.SuperAdmin;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public LoginResponse authenticate(LoginRequest request) {
        if (request == null) {
            log.error("Login request is null");
            throw new IllegalArgumentException("Login request cannot be null");
        }
        
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            log.error("Email is null or empty in login request");
            throw new IllegalArgumentException("Email is required");
        }
        
        try {
            log.info("Attempting to authenticate user with email: {}", email);
            
            // Check if user exists first
            CustomUserDetails user = findUserByEmail(email);
            log.debug("User found: {}", user.getUsername());

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("Invalid password for user: {}", request.getEmail());
                throw new BadCredentialsException("Invalid email or password");
            }

            // Check if account is active
            if (!user.isAccountNonLocked() || !user.isEnabled()) {
                log.warn("Account is locked or disabled for user: {}", request.getEmail());
                throw new RuntimeException("Account is locked or disabled");
            }

            try {
                // Generate token
                String jwtToken = jwtService.generateToken(user);
                log.debug("JWT token generated successfully for user: {}", user.getUsername());

                // Extract role
                String role = user.getAuthorities().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No roles found for user"))
                    .getAuthority();

                log.info("User {} authenticated successfully with role: {}", user.getUsername(), role);

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
                log.error("Error during token generation or response creation", e);
                throw new RuntimeException("Authentication processing failed", e);
            }

        } catch (UsernameNotFoundException e) {
            log.warn("User not found: {}", e.getMessage());
            throw e;
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {}", request.getEmail());
            throw e;
        } catch (RuntimeException e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private CustomUserDetails findUserByEmail(String email) {
        if (email == null) {
            log.error("Email parameter is null in findUserByEmail");
            throw new UsernameNotFoundException("Email cannot be null");
        }
        
        log.debug("Looking up user with email: {}", email);
        
        try {
            // Check Restaurant Users (includes ADMIN, CASHIER, BIKER, RESTAURANT_ADMIN roles)
            Optional<RestaurantUsers> restaurantUser = restaurantUsersRepository.findByEmail(email);
            if (restaurantUser.isPresent()) {
                log.debug("Found restaurant user: {}", email);
                return restaurantUser.get();
            }

            // Check Super Admin
            Optional<SuperAdmin> superAdmin = superAdminRepository.findByEmail(email);
            if (superAdmin.isPresent()) {
                log.debug("Found super admin: {}", email);
                return superAdmin.get();
            }

            // Check Customer
            Optional<Customer> customer = customerRepository.findByEmail(email);
            if (customer.isPresent()) {
                log.debug("Found customer: {}", email);
                return customer.get();
            }

            // If no user found with the given email
            log.warn("No user found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
            
        } catch (Exception e) {
            log.error("Error finding user by email: {}", email, e);
            throw new UsernameNotFoundException("Error finding user with email: " + email, e);
        }
    }

}
