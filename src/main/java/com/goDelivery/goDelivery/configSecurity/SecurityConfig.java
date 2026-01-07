package com.goDelivery.goDelivery.configSecurity;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final EmailVerificationFilter emailVerificationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write(
                            String.format("{\"status\": %d, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "Authentication failed: " + authException.getMessage())
                        );
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write(
                            String.format("{\"status\": %d, \"error\": \"Forbidden\", \"message\": \"%s\"}",
                                HttpServletResponse.SC_FORBIDDEN,
                                "Access Denied: " + accessDeniedException.getMessage())
                        );
                    })
            )
            .authorizeHttpRequests(authorize -> 
                authorize
                    // Public endpoints - no authentication required
                    .requestMatchers(
                        // Auth and documentation
                        "/api/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/error",
                        "/favicon.ico",
                        
                        // Webhooks
                        "/api/webhooks/**",
                        "/api/v1/payments/mpesa/webhook",
                        "/api/v1/payments/momo/webhook",
                        
                        // Public API endpoints
                        "/api/files/**",
                        "/uploads/**",
                        "/api/super-admin/register",
                        "/api/emails/**",
                        "/api/customers/register",
                        "/api/bikers/registerBiker",
                        "/api/restaurants/registerAdmin",
                        "/api/v1/branch-users/register",
                        "/api/v1/branch-registration/register",
                        "/api/restaurants/getAllActiveRestaurants",
                        "/api/restaurants/getRestaurantById/**",
                        "/api/menu-items/getAllMenuItem",
                        "/api/menu-items/getMenuItemById/**",
                        "/api/menu-items/getMenuItemByName/**",
                        "/api/menu-items/getMenuItemsByRestaurant/**",
                        "/api/restaurants/approved",
                        
                        // Public order tracking
                        "/api/orders/*/track",
                        "/api/bikers/tracking/*",
                        
                        // Options requests
                        "/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    
                    // Customer endpoints
                    .requestMatchers(
                        "/api/customers/**",
                        "/api/cart/**",
                        "/api/orders/createOrder",
                        "/api/orders/getOrdersByCustomer/**",
                        "/api/orders/getOrderById/**",
                        "/api/orders/cancelOrder/**",
                        "/api/payments/process",
                        "/api/payments/customer/**",
                        "/api/locations/**"
                    ).hasRole("CUSTOMER")
                    
                    // MPESA and MoMo endpoints
                    .requestMatchers(
                        "/api/v1/payments/mpesa/initiate",
                        "/api/v1/payments/mpesa/status/**",
                        "/api/v1/payments/momo/request",
                        "/api/v1/payments/momo/processOrderDisbursement",
                        "/api/v1/payments/momo/disbursement/status/*",
                        "/api/v1/payments/momo/status/**"
                    ).authenticated()
                    
                    // Restaurant Admin endpoints
                    .requestMatchers(
                        "/api/users/**",
                        "/api/menu-items/createMenuItem/**",
                        "/api/menu-items/updateMenuItem/**",
                        "/api/menu-items/deleteMenuItem/**",
                        "/api/menu-items/updateMenuItemAvailability/**",
                        "/api/menu-category/**",
                        "/api/file-upload/restaurants/**",
                        "/api/restaurants/registerRestaurant/**",
                        "/api/restaurants/updateRestaurant/**",
                        "/api/restaurants/deleteRestaurant/**",
                        "/api/v1/disbursements/restaurant/summary",
                        "/api/v1/disbursements/restaurant/transactions",
                        "/api/v1/branches/create/**",
                        "/api/v1/branches/update/**",
                        "/api/v1/branches/restaurant/**",
                        "/api/v1/branches/*/activate",
                        "/api/v1/branches/*/deactivate",
                        "/api/v1/branch-registration/**",
                        "/api/v1/branches/{branchId}/menu/**",
                        "/api/v1/branches/{branchId}/users/**",
                        "/api/v1/branches/{branchId}/analytics/**"
                    ).hasRole("RESTAURANT_ADMIN")
                    
                    // Branch Manager endpoints
                    .requestMatchers(
                        "/api/v1/branches/my-branches/**",
                        "/api/v1/branches/getBranches/**",
                        "/api/v1/branches/updateBranch/**",
                        "/api/v1/branches/*/menu/**",
                        "/api/v1/branches/*/orders/**",
                        "/api/v1/branches/*/settings/**",
                        "/api/v1/branches/**",
                        "/api/v1/branch-setup/**",
                        "/api/v1/branch-orders/**"
                    ).hasRole("BRANCH_MANAGER")
                    
                    // Shared endpoints
                    .requestMatchers(
                        "/api/orders/**"
                    ).hasAnyRole("RESTAURANT_ADMIN", "CUSTOMER", "CASHIER", "BRANCH_MANAGER", "SUPER_ADMIN")
                    
                    // Biker endpoints
                    .requestMatchers(
                        "/api/bikers/*/availableOrders",
                        "/api/bikers/*/activeOrders",
                        "/api/bikers/*/customerDetails/*",
                        "/api/bikers/*/navigation/*",
                        "/api/bikers/acceptDelivery",
                        "/api/bikers/rejectDelivery",
                        "/api/bikers/confirmPickup",
                        "/api/bikers/confirmDelivery",
                        "/api/bikers/updateLocation",
                        "/api/bikers/getNavigation"
                    ).hasRole("BIKER")
                    
                    // Cashier endpoints
                    .requestMatchers(
                        "/api/cashier/**",
                        "/api/v1/disbursements/restaurant/summary",
                        "/api/v1/disbursements/restaurant/transactions"
                    ).hasRole("CASHIER")
                    
                    // Analytics endpoints
                    .requestMatchers(
                        "/api/analytics/**"
                    ).hasAnyRole("RESTAURANT_ADMIN", "SUPER_ADMIN", "BRANCH_MANAGER")
                    
                    // All other requests require authentication
                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(emailVerificationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://delivery.ivas.rw", "https://delivery.ivas.rw", "http://localhost:3005","http://127.0.0.1:*", "http://localhost:58140"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}