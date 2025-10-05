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
                    .requestMatchers(
                        "/api/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/error",
                        "/favicon.ico"
                    ).permitAll()
                    .requestMatchers(
                        // Public endpoints
                        "/api/restaurant-applications/submit",
                        "/api/super-admin/register",
                        "/api/test/email/**",
                        "/api/customers/registerCustomer",
                        
                        // Registration and verification endpoints
                        "/api/restaurant/registration/registerAdmin",
                        "/api/restaurant/registration/verify-email",
                        "/api/restaurant/registration/resend-verification"
                    ).permitAll()
                    .requestMatchers(
                        "/api/restaurant-applications/all",
                        "/api/restaurant-applications/status"
                    ).hasRole("SUPER_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/restaurant-applications/*/review")
                    .hasRole("SUPER_ADMIN")
                    // Customer endpoints - require CUSTOMER role
                    .requestMatchers(
                        "/api/customers/**",
                        "/api/orders/createOrder",
                        "/api/orders/cancelOrder/*",
                        "/api/payments/process",
                        "/api/payments/customer/*"
                    ).hasRole("CUSTOMER")
                    // Restaurant Admin endpoints - require RESTAURANT_ADMIN role
                    .requestMatchers(
                        "/api/users/**",
                        "/api/menu-item/**",
                        "/api/menu-category/**",
                        "/api/file-upload/**",
                        "/api/restaurants/**"
                    ).hasRole("RESTAURANT_ADMIN")
                    // Shared endpoints - both CUSTOMER and RESTAURANT_ADMIN
                    .requestMatchers(
                        "/api/orders/**"
                    ).hasAnyRole("RESTAURANT_ADMIN", "CUSTOMER", "CASHIER")
                    // Public order tracking
                    .requestMatchers(
                        "/api/orders/*/track"
                    ).permitAll()
                    // Biker endpoints - require BIKER role
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
                    // Public tracking endpoint - no authentication required
                    .requestMatchers(
                        "/api/bikers/tracking/*"
                    ).permitAll()
                    // Cashier endpoints
                    .requestMatchers(
                        "/api/cashier/**"
                    ).hasRole("CASHIER")
                    // Analytics endpoints
                    .requestMatchers(
                        "/api/analytics/**"
                    ).hasAnyRole("RESTAURANT_ADMIN", "SUPER_ADMIN")
                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
