package com.goDelivery.goDelivery.configSecurity;

import com.goDelivery.goDelivery.model.CustomUserDetails;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationFilter extends OncePerRequestFilter {

    private final RestaurantUsersRepository userRepository;

    // Endpoints that don't require email verification
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/api/emails/verify",
        "/api/auth/",
        "/api/customers/",
        "/api/bikers/",
        "/api/cashier/",
        "/api/super-admin/",
        "/api/orders/",
        "/api/menu-items/getAll",
        "/api/menu-items/getMenuItemsByRestaurant",
        "/api/menu-items/getMenuItemById",
        "/api/menu-items/getMenuItemByName",
        "/swagger-ui",
        "/v3/api-docs",
        "/error",
        "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        
        // Skip verification for excluded paths
        if (shouldSkipVerification(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Only check for authenticated restaurant admin users
        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof CustomUserDetails) {
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // Check if user is a restaurant admin
            if (userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_RESTAURANT_ADMIN"))) {
                
                // Load fresh user data to check email verification status
                RestaurantUsers user = userRepository.findByEmail(userDetails.getUsername())
                        .orElse(null);
                
                if (user != null && !user.isEmailVerified()) {
                    log.warn("Access denied for unverified user: {}", user.getEmail());
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write(
                        "{\"status\": 403, \"error\": \"Email Not Verified\", " +
                        "\"message\": \"Please verify your email before accessing this resource. Check your inbox for the verification link.\"}"
                    );
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipVerification(String requestPath) {
        return EXCLUDED_PATHS.stream().anyMatch(requestPath::startsWith);
    }
}
