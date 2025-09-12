package com.goDelivery.goDelivery.configSecurity;

import io.micrometer.common.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Collection;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && !userEmail.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Get claims from the token
                    Claims claims = jwtService.extractAllClaims(jwt);
                    
                    // Log token claims for debugging
                    System.out.println("JWT Token Claims: " + claims);
                    
                    // Extract authorities from the token
                    @SuppressWarnings("unchecked")
                    List<String> authorities = (List<String>) claims.get("authorities");
                    System.out.println("Extracted authorities from token: " + authorities);
                    System.out.println("UserDetails authorities: " + userDetails.getAuthorities());
                    
                    // Use the authorities from userDetails to ensure consistency
                    // The CustomUserDetailsService already handles the ROLE_ prefix
                    Collection<? extends GrantedAuthority> grantedAuthorities = userDetails.getAuthorities();
                    System.out.println("Using authorities from UserDetails: " + grantedAuthorities);
                    
                    // Log the final granted authorities for debugging
                    System.out.println("Final granted authorities: " + grantedAuthorities);
                    
                    // Create authentication token with the extracted authorities
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            grantedAuthorities
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                System.err.println("JWT Authentication error: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}