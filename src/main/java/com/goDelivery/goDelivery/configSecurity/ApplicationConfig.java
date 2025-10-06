package com.goDelivery.goDelivery.configSecurity;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.repository.SuperAdminRepository;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final SuperAdminRepository superAdminRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(restaurantUsersRepository, superAdminRepository, null);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // In Spring Security 6.x, the recommended approach is to use the constructor with UserDetailsService and PasswordEncoder
        // and then set any additional properties if needed
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}