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
                    .requestMatchers(
                        "/api/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/error",
                        "/favicon.ico"
                    ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(
                        // Public endpoints
                        "/api/files/**",
                        "/uploads/**",
                        "/api/super-admin/register",
                        "/api/emails/**",
                        "/api/customers/register",
                        "/api/restaurants/registerAdmin",
                        "/api/restaurants/getAllActiveRestaurants",
                        "/api/restaurants/getRestaurantById/**",
                        "/api/menu-items/getAllMenuItem",
                        "/api/menu-items/getMenuItemById/**",
                        "/api/menu-items/getMenuItemByName/**",
                        "/api/menu-items/getMenuItemsByRestaurant/**",
                        "/api/restaurants/approved"
                    ).permitAll()
                    // Customer endpoints - require CUSTOMER role
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
                    // Restaurant Admin endpoints - require RESTAURANT_ADMIN role
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
                        "/api/restaurants/deleteRestaurant/**"
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
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(emailVerificationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
