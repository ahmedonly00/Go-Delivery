package com.goDelivery.goDelivery.auth;

import com.goDelivery.goDelivery.dtos.auth.LoginRequest;
import com.goDelivery.goDelivery.dtos.auth.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        // Safely handle null request
        if (request == null) {
            log.error("Login request is null");
            return ResponseEntity.badRequest().body("Login request cannot be null");
        }
        
        // Safely get email for logging
        String email = request.getEmail() != null ? request.getEmail() : "[no email provided]";
        log.info("Received login request for email: {}", email);
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errorMessage = "Invalid request";
            if (bindingResult.getFieldError() != null && bindingResult.getFieldError().getDefaultMessage() != null) {
                errorMessage = bindingResult.getFieldError().getDefaultMessage();
            }
            log.warn("Validation errors in login request: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(errorMessage);
        }
        
        try {
            LoginResponse response = service.authenticate(request);
            log.info("Login successful for user: {}", email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}. Error: {}", email, e.getMessage(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return ResponseEntity.badRequest().body("Authentication failed: " + errorMessage);
        }
    }
}
