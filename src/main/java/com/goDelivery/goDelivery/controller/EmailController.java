package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.auth.EmailVerificationRequest;
import com.goDelivery.goDelivery.dtos.auth.EmailVerificationResponse;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.service.email.EmailService;
import com.goDelivery.goDelivery.service.email.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/test")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendTestEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        if (to == null || to.isBlank()) {
            return CompletableFuture.completedFuture(
                createResponse(false, "Recipient email is required", 400)
            );
        }
        
        return emailService.sendTestEmail(to)
            .thenApply(success -> createResponse(success, 
                success ? "Test email sent successfully" : "Failed to send test email",
                success ? 200 : 500))
            .exceptionally(e -> createResponse(false, 
                e.getCause() != null ? e.getCause().getMessage() : "An unknown error occurred",
                500));
    }
    
    private ResponseEntity<Map<String, Object>> createResponse(boolean success, String message, int status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        if (success) {
            response.put("message", message);
        } else {
            response.put("error", message);
        }
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/verification")
    public ResponseEntity<?> sendVerificationEmail(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String token) {
        try {
            emailService.sendVerificationEmail(email, name, token);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    @PostMapping("/welcome")
    public ResponseEntity<?> sendWelcomeEmail(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam(required = false) String restaurantName) {
        try {
            emailService.sendWelcomeEmail(email, name, restaurantName != null ? restaurantName : "");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Welcome email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    @PostMapping("/restaurant-welcome")
    public ResponseEntity<?> sendRestaurantWelcomeEmail(
            @RequestParam String restaurantName,
            @RequestParam String ownerEmail,
            @RequestParam String ownerName,
            @RequestParam String temporaryPassword) {
        try {
            emailService.sendRestaurantWelcomeEmail(restaurantName, ownerEmail, ownerName, temporaryPassword);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Restaurant welcome email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> sendPasswordResetEmail(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String resetToken) {
        try {
            emailService.sendPasswordResetEmail(email, name, resetToken);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    @PostMapping("/otp")
    public ResponseEntity<?> sendOtpEmail(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String otp) {
        try {
            emailService.sendOtpEmail(email, name, otp);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }

    @PostMapping("/setup-complete")
    public ResponseEntity<?> sendSetupCompletionEmail(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String restaurantName) {
        try {
            emailService.sendSetupCompletionEmail(email, name, restaurantName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Setup completion email sent successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "error", e.getMessage())
            );
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) {
        try {
            boolean verified = emailVerificationService.verifyRestaurantEmail(
                    request.getToken(), 
                    request.getEmail()
            );
            
            if (verified) {
                // Get restaurant details for redirect
                Restaurant restaurant = emailVerificationService.getRestaurantByEmail(request.getEmail());
                
                String dashboardUrl = frontendUrl + "/restaurant/dashboard";
                
                return ResponseEntity.ok(EmailVerificationResponse.builder()
                        .success(true)
                        .message("Email verified successfully! Redirecting to your dashboard...")
                        .redirectUrl(dashboardUrl)
                        .restaurantId(restaurant != null ? restaurant.getRestaurantId() : null)
                        .restaurantName(restaurant != null ? restaurant.getRestaurantName() : null)
                        .build());
            } else {
                return ResponseEntity.badRequest().body(EmailVerificationResponse.builder()
                        .success(false)
                        .message("Invalid or expired verification token, or email mismatch")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(EmailVerificationResponse.builder()
                    .success(false)
                    .message("Verification failed: " + e.getMessage())
                    .build());
        }
    }
}
