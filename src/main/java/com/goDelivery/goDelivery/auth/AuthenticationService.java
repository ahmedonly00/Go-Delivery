package com.goDelivery.goDelivery.auth;

import com.goDelivery.goDelivery.configSecurity.JwtService;
import com.goDelivery.goDelivery.dtos.auth.LoginRequest;
import com.goDelivery.goDelivery.dtos.auth.LoginResponse;
import com.goDelivery.goDelivery.model.CustomUserDetails;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RestaurantUsersRepository restaurantUsersRepository;
    private final SuperAdminRepository superAdminRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginResponse authenticate(LoginRequest request) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            // Fetch user implementing CustomUserDetails
            CustomUserDetails user = findUserByEmail(request.getEmail());

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
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private CustomUserDetails findUserByEmail(String email) {
        return restaurantUsersRepository.findByEmail(email)
                .map(user -> (CustomUserDetails) user)
                .orElseGet(() -> superAdminRepository.findByEmail(email)
                        .map(admin -> (CustomUserDetails) admin)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email))
                );
    }
}
