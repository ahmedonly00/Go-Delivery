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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
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
                        "/api/restaurant-applications/submit",
                        "/api/super-admin/register",
                        "/api/restaurant-applications/test-email",
                        "/api/customers/registerCustomer"
                    ).permitAll()

                    .requestMatchers(
                        "/api/restaurant-applications/all",
                        "/api/restaurant-applications/status"

                    ).hasRole("SUPER_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/restaurant-applications/*/review")
                    .hasRole("SUPER_ADMIN")

                    .requestMatchers(
                        "api/users/*",
                        "/api/users/*/deactivate",
                        "/api/users/*/activate",
                        "/api/users/*/role/*"
                    ).hasRole("RESTAURANT_ADMIN")
                    
                    // Cart endpoints - only for authenticated customers
                    .requestMatchers(
                        "/api/cart/getCart",
                        "/api/cart/addItem",
                        "/api/cart/updateItem/*",
                        "/api/cart/removeItem/*",
                        "/api/cart/clearCart",
                        "/api/customers/getAllCustomers",
                        "/api/customers/getCustomerByEmail/*",
                        "/api/customers/getCustomerProfile/*",
                        "/api/customers/getCustomerById/*",
                        "/api/restaurants/getRestaurantsByLocation/*",
                        "/api/restaurants/getRestaurantsByCuisineType/*",
                        "/api/restaurants/searchRestaurants",
                        "/api/restaurants/getAllActiveRestaurants",
                        "/api/menu-items/createMenuItem/*",
                        "/api/menu-items/getAllMenuItems/*",
                        "/api/menu-items/getMenuItemById/*",
                        "/api/menu-items/updateMenuItem/*",
                        "/api/menu-items/deleteMenuItem/*"
                    ).hasRole("CUSTOMER")

                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
