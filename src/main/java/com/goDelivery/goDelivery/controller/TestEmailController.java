package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.service.EmailServiceInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/test/email")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TestEmailController {

    private final EmailServiceInterface emailService;

    @GetMapping("/welcome")
    public CompletableFuture<ResponseEntity<String>> testWelcomeEmail(@RequestParam String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                emailService.sendRestaurantWelcomeEmail(
                    "Test Restaurant",
                    email,
                    "Test User",
                    "temporary123"
                );
                return ResponseEntity.ok("Welcome email sent successfully to " + email);
            } catch (Exception e) {
                log.error("Failed to send welcome email to {}: {}", email, e.getMessage(), e);
                return ResponseEntity.badRequest()
                    .body("Failed to send email: " + e.getMessage());
            }
        });
    }

    @GetMapping("/rejection")
    public ResponseEntity<String> testRejectionEmail(
            @RequestParam String email,
            @RequestParam(required = false) String reason) {
        try {
            emailService.sendApplicationRejectionEmail(
                "Test Restaurant",
                email,
                reason != null ? reason : "Incomplete documentation"
            );
            return ResponseEntity.ok("Rejection email sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public CompletableFuture<ResponseEntity<String>> testEmail(@RequestParam String email) {
        return emailService.sendTestEmail(email)
            .thenApply(success -> {
                if (success) {
                    return ResponseEntity.ok("Test email sent successfully to " + email);
                } else {
                    return ResponseEntity.badRequest().body("Failed to send test email to " + email);
                }
            })
            .exceptionally(e -> 
                ResponseEntity.badRequest().body("Error: " + e.getMessage())
            );
    }
}
