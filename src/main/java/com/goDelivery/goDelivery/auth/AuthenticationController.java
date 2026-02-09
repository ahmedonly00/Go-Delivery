package com.goDelivery.goDelivery.auth;

import com.goDelivery.goDelivery.dtos.auth.LoginRequest;
import com.goDelivery.goDelivery.dtos.auth.LoginResponse;
import com.goDelivery.goDelivery.dtos.auth.ResetPasswordRequest;
import com.goDelivery.goDelivery.dtos.auth.ForgotPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Login request cannot be null");
        }

        // Safely get email for logging
        String email = request.getEmail() != null ? request.getEmail() : "[no email provided]";
        log.info("Received login request for email: {}", email);

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errorMessage = "Invalid request";
            var fieldError = bindingResult.getFieldError();
            if (fieldError != null && fieldError.getDefaultMessage() != null) {
                errorMessage = fieldError.getDefaultMessage();
            }
            log.warn("Validation errors in login request: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            LoginResponse response = service.authenticate(request);
            log.info("Login successful for user: {}", email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", email, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            service.forgotPassword(request);
            // Always return success message for security (don't reveal if email exists)
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "If your email exists in our system, you will receive a password reset link shortly."));
        } catch (Exception e) {
            log.error("Error processing forgot password request", e);
            // Still return success for security
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "If your email exists in our system, you will receive a password reset link shortly."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            service.resetPassword(request);
            return ResponseEntity.ok("Password has been reset successfully");
        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String token) {
        service.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam String token) {
        service.refreshToken(token);
        return ResponseEntity.ok().build();
    }
}
